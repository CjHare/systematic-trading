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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.collections.BlockingEventCountQueue;
import com.systematic.trading.data.concurrent.EventCountCleanUp;
import com.systematic.trading.data.dao.HibernateTradingDayPricesDao;
import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.QuandlAPI;
import com.systematic.trading.signals.data.api.quandl.configuration.QuandlConfigurationLoader;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseFormat;

public class DataServiceUpdaterImpl implements DataServiceUpdater {

	private static final Logger LOG = LogManager.getLogger(DataServiceUpdaterImpl.class);

	/** Invoke the clean operation on the throttler, ten times a second.*/
	private static final Duration THROTTLER_CLEAN_INTERVAL = Duration.of(100, ChronoUnit.MILLIS);

	//TODO this should be a percentage, no?
	/** Average number of data points above which assumes the month already retrieve covered. */
	private static final int MINIMUM_MEAN_DATA_POINTS_PER_MONTH_THRESHOLD = 15;

	private final TradingDayPricesDao dao = new HibernateTradingDayPricesDao();

	private final EquityApi api;

	public DataServiceUpdaterImpl() throws IOException {

		final EquityApiConfiguration configuration = new QuandlConfigurationLoader().load();
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

		if (!outstandingRequests.isEmpty()) {
			processHistoryRetrievalRequests(outstandingRequests);
			ensureAllRetrievalRequestsProcessed(tickerSymbol);
		}
	}

	private void ensureAllRetrievalRequestsProcessed( final String tickerSymbol ) throws CannotRetrieveDataException {
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
		final HistoryRetrievalRequestManager requestManager = HistoryRetrievalRequestManager.getInstance();
		final ExecutorService pool = Executors.newFixedThreadPool(api.getMaximumConcurrentConnections());
		final BlockingEventCount activeConnectionCount = new BlockingEventCountQueue(
		        api.getMaximumConnectionsPerSecond(), THROTTLER_CLEAN_INTERVAL);
		final EventCountCleanUp activeConnectionCountCleaner = startEventCountCleaner(activeConnectionCount);

		for (final HistoryRetrievalRequest request : requests) {

			final String tickerSymbol = request.getTickerSymbol();
			final LocalDate inclusiveStartDate = request.getInclusiveStartDate().toLocalDate();
			final LocalDate exclusiveEndDate = request.getExclusiveEndDate().toLocalDate();

			pool.execute(() -> {
				try {
					// Pull the data from the Stock API
					TradingDayPrices[] tradingData = api.getStockData(tickerSymbol, inclusiveStartDate,
					        exclusiveEndDate, activeConnectionCount);

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

		shutdown(pool, requests);
		activeConnectionCountCleaner.end();
	}

	private void shutdown( final ExecutorService pool, final List<HistoryRetrievalRequest> requests )
	        throws CannotRetrieveDataException {
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
	 * Spawns a daemon thread to perform the periodic (every second) clean of the event count.
	 */
	private EventCountCleanUp startEventCountCleaner( final BlockingEventCount eventCount ) {
		final EventCountCleanUp throttlerCleanUp = new EventCountCleanUp(eventCount, THROTTLER_CLEAN_INTERVAL);
		final Thread cleanUp = new Thread(throttlerCleanUp);
		cleanUp.setDaemon(true);
		cleanUp.start();
		return throttlerCleanUp;
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