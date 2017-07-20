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

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.data.dao.HibernateTradingDayPricesDao;
import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.signals.data.api.quandl.QuandlAPI;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseFormat;

public class DataServiceUpdaterImpl implements DataServiceUpdater {

	private static final DataServiceUpdater INSTANCE = new DataServiceUpdaterImpl();

	/** Average number of data points above which assumes the month already retrieve covered. */
	private static final int MINIMUM_MEAN_DATA_POINTS_PER_MONTH_THRESHOLD = 15;

	private final TradingDayPricesDao dao = new HibernateTradingDayPricesDao();

	//TODO inject this - configuration 
	private final EquityApi api = new QuandlAPI(new QuandlDao(), new QuandlResponseFormat());

	private DataServiceUpdaterImpl() {
	}

	public static DataServiceUpdater getInstance() {
		return INSTANCE;
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
	}

	/**
	 * Get the history requests from the stock API.
	 */
	private void processHistoryRetrievalRequests( final List<HistoryRetrievalRequest> requests )
	        throws CannotRetrieveDataException {
		final HistoryRetrievalRequestManager requestManager = HistoryRetrievalRequestManager.getInstance();

		for (final HistoryRetrievalRequest request : requests) {

			final String tickerSymbol = request.getTickerSymbol();
			final LocalDate inclusiveStartDate = request.getInclusiveStartDate().toLocalDate();
			final LocalDate exclusiveEndDate = request.getExclusiveEndDate().toLocalDate();

			// Pull the data from the Stock API
			final TradingDayPrices[] tradingData = api.getStockData(tickerSymbol, inclusiveStartDate, exclusiveEndDate);

			// Push to the data source
			dao.create(tradingData);

			// Remove the request from the queue
			requestManager.delete(request);
		}
	}

	/**
	 * Split up the date range into manageable size pieces.
	 */
	private List<HistoryRetrievalRequest> sliceHistoryRetrievalRequest( final String tickerSymbol,
	        final LocalDate startDate, final LocalDate endDate ) {

		final Period maximum = api.getMaximumDurationInSingleUpdate();
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