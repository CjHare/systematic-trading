/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.collections.BlockingEventCountQueue;
import com.systematic.trading.data.concurrent.EventCountCleanUp;
import com.systematic.trading.data.dao.PendingRetrievalRequestDao;
import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.data.dao.impl.HibernatePendingRetrievalRequestDao;
import com.systematic.trading.data.dao.impl.HibernateRetrievedMonthTradingPricesDao;
import com.systematic.trading.data.dao.impl.HibernateTradingDayPricesDao;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.data.history.HistoryRetrievalRequestMerger;
import com.systematic.trading.data.history.HistoryRetrievalRequestSlicer;
import com.systematic.trading.data.history.RetrievedHistoryPeriodRecorder;
import com.systematic.trading.data.history.UnnecessaryHistoryRequestFilter;
import com.systematic.trading.data.history.impl.HistoryRetrievalRequestMergerImpl;
import com.systematic.trading.data.history.impl.MonthlyHistoryRetrievalRequestSlicer;
import com.systematic.trading.data.history.impl.RetrievedYearMonthRecorder;
import com.systematic.trading.data.history.impl.UnnecessaryHistoryRequestFilterImpl;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.builder.impl.HibernateHistoryRetrievalRequestBuilder;
import com.systematic.trading.model.price.TradingDayPrices;

public class DataServiceUpdaterImpl implements DataServiceUpdater {

	private static final Logger LOG = LogManager.getLogger(DataServiceUpdaterImpl.class);

	/** Invoke the clean operation on the throttler, once a second. */
	private static final Duration THROTTLER_CLEAN_INTERVAL = Duration.of(1, ChronoUnit.SECONDS);

	private final EquityApi api;
	private final PendingRetrievalRequestDao pendingRetrievalRequestDao;
	private final RetrievedHistoryPeriodRecorder retrievedHistoryRecorder;
	private final TradingDayPricesDao tradingDayPricesDao;
	private final HistoryRetrievalRequestSlicer historyRetrievalRequestSlicer;
	private final UnnecessaryHistoryRequestFilter unecessaryRequestFilter;
	private final HistoryRetrievalRequestMerger historyRetrievalRequestMerger;

	public DataServiceUpdaterImpl( final EquityApi api ) {

		final RetrievedMonthTradingPricesDao retrievedHistoryDao = new HibernateRetrievedMonthTradingPricesDao();

		this.api = api;
		this.retrievedHistoryRecorder = new RetrievedYearMonthRecorder(retrievedHistoryDao);
		this.pendingRetrievalRequestDao = new HibernatePendingRetrievalRequestDao();
		this.tradingDayPricesDao = new HibernateTradingDayPricesDao();
		this.historyRetrievalRequestSlicer = new MonthlyHistoryRetrievalRequestSlicer();
		this.unecessaryRequestFilter = new UnnecessaryHistoryRequestFilterImpl(retrievedHistoryDao);
		this.historyRetrievalRequestMerger = new HistoryRetrievalRequestMergerImpl(
		        new HibernateHistoryRetrievalRequestBuilder());
	}

	@Override
	public void get(
	        final String equityDataset,
	        final String tickerSymbol,
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate) throws CannotRetrieveDataException {

		// Ensure there's a table for the data
		tradingDayPricesDao.createTableIfAbsent(tickerSymbol);

		lodgeOnlyNeededHistoryRetrievalRequests(equityDataset, tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		final List<HistoryRetrievalRequest> outstandingRequests = outstandingHistoryRetrievalRequests(tickerSymbol);

		if (!outstandingRequests.isEmpty()) {
			processHistoryRetrievalRequests(outstandingRequests);
			ensureAllRetrievalRequestsProcessed(tickerSymbol);
			retrievedHistoryRecorder.retrieved(outstandingRequests);
		}
	}

	private void ensureAllRetrievalRequestsProcessed( final String tickerSymbol ) throws CannotRetrieveDataException {

		final List<HistoryRetrievalRequest> remainingRequests = outstandingHistoryRetrievalRequests(tickerSymbol);
		if (!remainingRequests
		        .isEmpty()) { throw new CannotRetrieveDataException("Failed to retrieve all the required data"); }
	}

	/**
	 * Get the history requests from the stock API.
	 */
	private void processHistoryRetrievalRequests( final List<HistoryRetrievalRequest> requests )
	        throws CannotRetrieveDataException {

		final ExecutorService pool = Executors.newFixedThreadPool(api.maximumConcurrentConnections());
		final BlockingEventCount activeConnectionCount = new BlockingEventCountQueue(
		        api.maximumConnectionsPerSecond(),
		        THROTTLER_CLEAN_INTERVAL);
		final EventCountCleanUp activeConnectionCountCleaner = startEventCountCleaner(activeConnectionCount);

		for (final HistoryRetrievalRequest request : requests) {

			final String equityDataset = request.equityDataset();
			final String tickerSymbol = request.tickerSymbol();
			final LocalDate inclusiveStartDate = request.inclusiveStartDate().toLocalDate();
			final LocalDate exclusiveEndDate = request.exclusiveEndDate().toLocalDate();

			pool.execute(() -> {
				try {
					// Pull the data from the Stock API
					TradingDayPrices[] tradingData = api.stockData(
					        equityDataset,
					        tickerSymbol,
					        inclusiveStartDate,
					        exclusiveEndDate,
					        activeConnectionCount);

					if (tradingData.length == 0) {
						LOG.warn(
						        String.format(
						                "No data has been returned for symbol: %s for %s to %s",
						                tickerSymbol,
						                inclusiveStartDate,
						                exclusiveEndDate));
					}

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

		final int timeout = requests.size() * api.maximumRetrievalTimeSeconds();
		pool.shutdown();

		try {
			if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) { throw new CannotRetrieveDataException(
			        String.format("API calls failed to complete in the expected timeout of %s seconds", timeout)); }
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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

	private void lodgeOnlyNeededHistoryRetrievalRequests(
	        final String equityDataset,
	        final String tickerSymbol,
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {

		lodge(merge(filter(slice(equityDataset, tickerSymbol, inclusiveStartDate, exclusiveEndDate))));
	}

	private void lodge( final List<HistoryRetrievalRequest> requests ) {

		pendingRetrievalRequestDao.create(requests);
	}

	private List<HistoryRetrievalRequest> outstandingHistoryRetrievalRequests( final String tickerSymbol ) {

		return pendingRetrievalRequestDao.requests(tickerSymbol);
	}

	private List<HistoryRetrievalRequest> slice(
	        final String equityDataset,
	        final String tickerSymbol,
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {

		return historyRetrievalRequestSlicer.slice(equityDataset, tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private List<HistoryRetrievalRequest> filter( final List<HistoryRetrievalRequest> requests ) {

		return unecessaryRequestFilter.filter(requests);
	}

	private List<HistoryRetrievalRequest> merge( final List<HistoryRetrievalRequest> requests ) {

		return historyRetrievalRequestMerger.merge(requests, api.maximumDurationPerConnection());
	}
}
