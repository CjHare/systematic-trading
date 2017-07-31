/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of [project] nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.data;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.concurrent.ThrottlerCleanUp;
import com.systematic.trading.data.configuration.ConfigurationLoader;
import com.systematic.trading.data.configuration.KeyLoader;
import com.systematic.trading.data.dao.HibernateTradingDayPricesDao;
import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.data.model.BlockingRingBuffer;
import com.systematic.trading.signals.data.api.quandl.QuandlAPI;
import com.systematic.trading.signals.data.api.quandl.configuration.QuandlConfiguration;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseFormat;

public class DataServiceUpdaterImpl implements DataServiceUpdater {

	private static final Logger LOG = LogManager.getLogger(DataServiceUpdaterImpl.class);

	private static final Duration ONE_SECOND = Duration.of(1, ChronoUnit.SECONDS);

	/** Average number of data points above which assumes the month already retrieve covered. */
	private static final int MINIMUM_MEAN_DATA_POINTS_PER_MONTH_THRESHOLD = 15;

	//TODO configuration file location - property injection candidate
	private static final String QUANDL_PROPERTIES_FILE = "quandl.properties";

	//TODO configuration file location - property injection candidate
	private static final String QUANDL_API_KEY_FILE = "quandl.key";

	private final TradingDayPricesDao dao = new HibernateTradingDayPricesDao();

	private final EquityApi api;

	public DataServiceUpdaterImpl() throws IOException {

		//TODO move the quandl specific config into that project

		final String apiKey = new KeyLoader().load(QUANDL_API_KEY_FILE);
		final Properties quandlProperties = new ConfigurationLoader().load(QUANDL_PROPERTIES_FILE);
		final String endpoint = quandlProperties.getProperty("endpoint");
		final int numberOfRetries = Integer.parseInt(quandlProperties.getProperty("number_of_retries"));
		final int retryBackOffMs = Integer.parseInt(quandlProperties.getProperty("retry_backoff_ms"));
		final int maximumRetrievalTimeSeconds = Integer
		        .parseInt(quandlProperties.getProperty("maximum_retrieval_time_seconds"));
		final int maximumConcurrentConnections = Integer
		        .parseInt(quandlProperties.getProperty("maximum_concurrent_connections"));
		final int maximumConnectionsPerSecond = Integer
		        .parseInt(quandlProperties.getProperty("maximum_connections_per_second"));
		final int maximumMonthsPerConnection = Integer
		        .parseInt(quandlProperties.getProperty("maximum_months_retrieved_per_connection"));

		//TODO validation of the required properties needed for each API
		//TODO move the configuration reading elsewhereW

		final QuandlConfiguration configuration = new QuandlConfiguration(endpoint, apiKey, numberOfRetries,
		        retryBackOffMs, maximumRetrievalTimeSeconds, maximumConcurrentConnections, maximumConnectionsPerSecond,
		        maximumMonthsPerConnection);

		this.api = new QuandlAPI(new QuandlDao(configuration), configuration, new QuandlResponseFormat());
	}

	@Override
	public void get( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate )
	        throws CannotRetrieveDataException {

		// Ensure there's a table for the data
		dao.createTableIfAbsent(tickerSymbol);

		// TODO of partial data, retrieve & discard

		final List<HistoryRetrievalRequest> unfilteredRequests = sliceHistoryRetrievalRequest(tickerSymbol, startDate,
		        endDate);
		final List<HistoryRetrievalRequest> filteredRequests = removeRedundantRequests(unfilteredRequests);
		storeHistoryRetrievalRequests(filteredRequests);

		final List<HistoryRetrievalRequest> outstandingRequests = getOutstandingHistoryRetrievalRequests(tickerSymbol);

		processHistoryRetrievalRequests(outstandingRequests);

		//TODO private
		final List<HistoryRetrievalRequest> remainingRequests = getOutstandingHistoryRetrievalRequests(tickerSymbol);
		if (!remainingRequests.isEmpty()) {
			throw new CannotRetrieveDataException("Failed to retrieve all the required data");
		}
	}

	/**
	 * Get the history requests from the stock API.
	 */
	private void processHistoryRetrievalRequests( final List<HistoryRetrievalRequest> requests )
	        throws CannotRetrieveDataException {

		if (requests.isEmpty()) {
			return;
		}

		final HistoryRetrievalRequestManager requestManager = HistoryRetrievalRequestManager.getInstance();
		final ExecutorService pool = Executors.newFixedThreadPool(api.getMaximumConcurrentConnections());

		//TODO make ring buffer abstract, with implementation - better variable name
		final BlockingRingBuffer ringBuffer = new BlockingRingBuffer(api.getMaximumConnectionsPerSecond(), ONE_SECOND);

		//TODO clean up - private method
		final ThrottlerCleanUp throttlerCleanUp = new ThrottlerCleanUp(ringBuffer, ONE_SECOND);
		final Thread cleanUp = new Thread(throttlerCleanUp);
		cleanUp.setDaemon(true);
		cleanUp.start();

		for (final HistoryRetrievalRequest request : requests) {

			final String tickerSymbol = request.getTickerSymbol();
			final LocalDate inclusiveStartDate = request.getInclusiveStartDate().toLocalDate();
			final LocalDate exclusiveEndDate = request.getExclusiveEndDate().toLocalDate();

			pool.execute(() -> {
				try {
					// Pull the data from the Stock API
					TradingDayPrices[] tradingData = api.getStockData(tickerSymbol, inclusiveStartDate,
					        exclusiveEndDate, ringBuffer);

					// Push to the data source
					dao.create(tradingData);

					// Remove the request from the queue
					requestManager.delete(request);

				} catch (CannotRetrieveDataException e) {
					LOG.error(e);

					// Single failure means all remaining and any active attempts must be ceased
					pool.shutdownNow();
				}
			});
		}

		//TODO this hard time out will not cater for throttling!
		//TODO private methods
		final int timeout = requests.size() * api.getMaximumRetrievalTimeSeconds();
		pool.shutdown();

		try {
			if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
				throw new CannotRetrieveDataException(
				        String.format("API calls failed to complete in the expected timeout of %s seconds", timeout));
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		throttlerCleanUp.end();
	}

	/**
	 * Split up the date range into manageable size pieces.
	 */
	private List<HistoryRetrievalRequest> sliceHistoryRetrievalRequest( final String tickerSymbol,
	        final LocalDate startDate, final LocalDate endDate ) {

		final Period maximum = api.getMaximumDurationPerConnection();
		final List<HistoryRetrievalRequest> requests = new ArrayList<>();

		if (isDurationTooLong(maximum, Period.between(startDate, endDate))) {

			// Split up the requests for processing
			LocalDate movedStartDate = startDate;
			LocalDate movedEndDate = startDate.plus(maximum);

			while (endDate.isAfter(movedStartDate)) {

				requests.add(new HistoryRetrievalRequest(tickerSymbol, movedStartDate, movedEndDate));

				movedStartDate = movedEndDate;
				movedEndDate = movedStartDate.plus(maximum);

				//Ensure we end on the correct date
				movedEndDate = endDate.isBefore(movedEndDate) ? endDate : movedEndDate;
			}

		} else {

			requests.add(new HistoryRetrievalRequest(tickerSymbol, startDate, endDate));
		}

		return requests;
	}

	/**
	 * Remove requests where the full set of data already has already been retrieved.
	 */
	private List<HistoryRetrievalRequest> removeRedundantRequests( final List<HistoryRetrievalRequest> requests ) {
		List<HistoryRetrievalRequest> filtered = new ArrayList<>();

		for (final HistoryRetrievalRequest request : requests) {
			filtered = removeIfRedundantRequest(request, filtered);
		}

		return filtered;
	}

	private List<HistoryRetrievalRequest> removeIfRedundantRequest( final HistoryRetrievalRequest request,
	        final List<HistoryRetrievalRequest> filtered ) {

		final String tickerSymbol = request.getTickerSymbol();
		final LocalDate startDate = request.getInclusiveStartDate().toLocalDate();
		final LocalDate endDate = request.getExclusiveEndDate().toLocalDate();

		final long count = dao.count(tickerSymbol, startDate, endDate);

		if (count == 0) {
			// Zero data exists :. we need data
			filtered.add(request);
		} else {
			final Period interval = Period.between(startDate, endDate);

			if (interval.toTotalMonths() > 0) {
				final long meanDataPointsPerMonth = count / interval.toTotalMonths();

				if (meanDataPointsPerMonth < MINIMUM_MEAN_DATA_POINTS_PER_MONTH_THRESHOLD) {
					// Insufficient data exists :. we need data
					filtered.add(request);
				}
			} else {
				// Under one month means we're in days difference
				if (count <= interval.getDays()) {
					filtered.add(request);
				}
			}
		}

		return filtered;
	}

	private boolean isDurationTooLong( final Period maximum, final Period actual ) {
		return actual.toTotalMonths() > maximum.toTotalMonths();
	}

	/**
	 * These we store as a defensive approach in case of partial failure during processing.
	 */
	private void storeHistoryRetrievalRequests( final List<HistoryRetrievalRequest> requests ) {
		HistoryRetrievalRequestManager.getInstance().create(requests);
	}

	private List<HistoryRetrievalRequest> getOutstandingHistoryRetrievalRequests( final String tickerSymbol ) {
		return HistoryRetrievalRequestManager.getInstance().get(tickerSymbol);
	}
}