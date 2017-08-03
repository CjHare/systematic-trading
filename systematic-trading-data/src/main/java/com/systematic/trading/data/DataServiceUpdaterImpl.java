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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
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
import com.systematic.trading.data.dao.PendingRetrievalRequestDao;
import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.data.dao.impl.HibernatePendingRetrievalRequestDao;
import com.systematic.trading.data.dao.impl.HibernateRetrievedMonthTradingPricesDao;
import com.systematic.trading.data.dao.impl.HibernateTradingDayPricesDao;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.data.exception.ConfigurationValidationException;
import com.systematic.trading.data.model.HibernateHistoryRetrievalRequest;
import com.systematic.trading.data.model.HibernateRetrievedMonthTradingPrices;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;
import com.systematic.trading.signals.data.api.quandl.QuandlAPI;
import com.systematic.trading.signals.data.api.quandl.dao.impl.FileValidatedQuandlConfigurationDao;
import com.systematic.trading.signals.data.api.quandl.dao.impl.HttpQuandlApiDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseFormat;

public class DataServiceUpdaterImpl implements DataServiceUpdater {

	private static final Logger LOG = LogManager.getLogger(DataServiceUpdaterImpl.class);

	/** Invoke the clean operation on the throttler, ten times a second.*/
	private static final Duration THROTTLER_CLEAN_INTERVAL = Duration.of(100, ChronoUnit.MILLIS);

	//TODO this should be a percentage, no?
	/** Average number of data points above which assumes the month already retrieve covered. */
	private static final int MINIMUM_MEAN_DATA_POINTS_PER_MONTH_THRESHOLD = 15;

	private final TradingDayPricesDao tradingDayPricesDao = new HibernateTradingDayPricesDao();
	private final PendingRetrievalRequestDao pendingRetrievalRequestDao = new HibernatePendingRetrievalRequestDao();
	private final RetrievedMonthTradingPricesDao retrievedMonthsDao = new HibernateRetrievedMonthTradingPricesDao();

	private final EquityApi api;

	public DataServiceUpdaterImpl() throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final EquityApiConfiguration configuration = new FileValidatedQuandlConfigurationDao().get();
		this.api = new QuandlAPI(new HttpQuandlApiDao(configuration), configuration, new QuandlResponseFormat());
	}

	@Override
	public void get( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate )
	        throws CannotRetrieveDataException {

		// Ensure there's a table for the data
		tradingDayPricesDao.createTableIfAbsent(tickerSymbol);

		//TODO split the requests into months - check DB whether full month is stored locally (add table / marker)

		final List<HistoryRetrievalRequest> unfilteredRequests = sliceHistoryRetrievalRequest(tickerSymbol, startDate,
		        endDate);
		final List<HistoryRetrievalRequest> filteredRequests = removeRedundantRequests(unfilteredRequests);
		storeHistoryRetrievalRequests(filteredRequests);

		//TODO refactor, into another class, candidate to put into a job
		//TODO leave a todo for later implementation / description

		final List<HistoryRetrievalRequest> outstandingRequests = getOutstandingHistoryRetrievalRequests(tickerSymbol);

		if (!outstandingRequests.isEmpty()) {
			processHistoryRetrievalRequests(outstandingRequests);
			ensureAllRetrievalRequestsProcessed(tickerSymbol);
			storeRetrievedMonths(outstandingRequests);
		}
	}

	private void ensureAllRetrievalRequestsProcessed( final String tickerSymbol ) throws CannotRetrieveDataException {
		final List<HistoryRetrievalRequest> remainingRequests = getOutstandingHistoryRetrievalRequests(tickerSymbol);
		if (!remainingRequests.isEmpty()) {
			throw new CannotRetrieveDataException("Failed to retrieve all the required data");
		}
	}

	private void storeRetrievedMonths( final List<HistoryRetrievalRequest> fulfilledRequests ) {
		final List<RetrievedMonthTradingPrices> retrieved = new ArrayList<>();

		//TODO slice the requests on whole months, i.e. not half way through ...it'll make like easier

		for (final HistoryRetrievalRequest fulfilled : fulfilledRequests) {

			final String tickerSymbol = fulfilled.getTickerSymbol();
			final LocalDate start = fulfilled.getInclusiveStartDate().toLocalDate();
			final LocalDate end = fulfilled.getExclusiveEndDate().toLocalDate();

			if (isBeginningTradingMonth(start)) {
				retrieved.add(createRetrievedMonth(tickerSymbol, start));
			}

			LocalDate between = start.plusMonths(1);

			while (between.isBefore(end) && hasDifferentYearMonth(between, end)) {
				retrieved.add(createRetrievedMonth(tickerSymbol, between));
				between = between.plusMonths(1);
			}

			if (isEndTradingMonth(end)) {
				retrieved.add(createRetrievedMonth(tickerSymbol, end));
			}
		}

		retrievedMonthsDao.create(retrieved);
	}

	private boolean hasDifferentYearMonth( final LocalDate a, final LocalDate b ) {
		return a.getYear() != b.getYear() || a.getMonthValue() != b.getMonthValue();
	}

	private RetrievedMonthTradingPrices createRetrievedMonth( final String tickerSymbol, final LocalDate yearMonth ) {
		return new HibernateRetrievedMonthTradingPrices(tickerSymbol,
		        YearMonth.of(yearMonth.getYear(), yearMonth.getMonth().getValue()));
	}

	private boolean isEndTradingMonth( final LocalDate contender ) {
		final YearMonth ym = YearMonth.of(contender.getYear(), contender.getMonth());
		return isLastDayOfMonth(contender, ym) || isLastFridayOfMonth(contender, ym);
	}

	private boolean isBeginningTradingMonth( final LocalDate contender ) {
		return isFirstDayOfMonth(contender) || isFirstMondayOfMonth(contender);
	}

	private boolean isFirstMondayOfMonth( final LocalDate contender ) {
		return contender.getDayOfWeek() == DayOfWeek.MONDAY && contender.getDayOfMonth() < DayOfWeek.values().length;
	}

	private boolean isFirstDayOfMonth( final LocalDate contender ) {
		return contender.getDayOfMonth() == 1;
	}

	private boolean isLastDayOfMonth( final LocalDate contender, final YearMonth ym ) {
		return contender.getDayOfMonth() == ym.lengthOfMonth();
	}

	private boolean isLastFridayOfMonth( final LocalDate contender, final YearMonth ym ) {
		return contender.getDayOfMonth() > ym.lengthOfMonth() - 3 && contender.getDayOfWeek() == DayOfWeek.FRIDAY;
	}

	/**
	 * Get the history requests from the stock API.
	 */
	private void processHistoryRetrievalRequests( final List<HistoryRetrievalRequest> requests )
	        throws CannotRetrieveDataException {
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
					tradingDayPricesDao.create(tradingData);

					// Remove the request from the queue
					pendingRetrievalRequestDao.delete(request);

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

				requests.add(new HibernateHistoryRetrievalRequest(tickerSymbol, movedStartDate, movedEndDate));

				movedStartDate = movedEndDate;
				movedEndDate = movedStartDate.plus(maximum);

				//Ensure we end on the correct date
				movedEndDate = endDate.isBefore(movedEndDate) ? endDate : movedEndDate;
			}
		} else {
			requests.add(new HibernateHistoryRetrievalRequest(tickerSymbol, startDate, endDate));
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
			filtered = addOnlyRelevantRequest(request, filtered);
		}

		return filtered;
	}

	private List<HistoryRetrievalRequest> addOnlyRelevantRequest( final HistoryRetrievalRequest request,
	        final List<HistoryRetrievalRequest> filtered ) {

		//TODO change this, decision on when request is relevant

		final String tickerSymbol = request.getTickerSymbol();
		final LocalDate startDate = request.getInclusiveStartDate().toLocalDate();
		final LocalDate endDate = request.getExclusiveEndDate().toLocalDate();

		final long count = tradingDayPricesDao.count(tickerSymbol, startDate, endDate);

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
		pendingRetrievalRequestDao.create(requests);
	}

	private List<HistoryRetrievalRequest> getOutstandingHistoryRetrievalRequests( final String tickerSymbol ) {

		//TODO validate data retrieved, if it's complete -> fail (put into the retrieval manager0

		return pendingRetrievalRequestDao.get(tickerSymbol);
	}
}