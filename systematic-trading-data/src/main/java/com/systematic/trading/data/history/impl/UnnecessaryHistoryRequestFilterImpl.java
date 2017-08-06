/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.data.history.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.history.UnnecessaryHistoryRequestFilter;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Filter for unnecessary remote history requests.
 * 
 * @author CJ Hare
 */
public class UnnecessaryHistoryRequestFilterImpl implements UnnecessaryHistoryRequestFilter {

	private final RetrievedMonthTradingPricesDao retrievedHistoryDao;

	public UnnecessaryHistoryRequestFilterImpl( final RetrievedMonthTradingPricesDao retrievedHistoryDao ) {
		this.retrievedHistoryDao = retrievedHistoryDao;
	}

	@Override
	public List<HistoryRetrievalRequest> filter( final List<HistoryRetrievalRequest> unfilteredRequests ) {
		if (unfilteredRequests == null) {
			return new ArrayList<>(0);
		}

		final List<HistoryRetrievalRequest> filtered = new ArrayList<>(unfilteredRequests.size());
		final Map<String, List<HistoryRetrievalRequest>> tickerSymbolRequests = splitByTickerSymbol(unfilteredRequests);

		for (final String tickerSymbol : tickerSymbolRequests.keySet()) {
			final List<HistoryRetrievalRequest> requests = tickerSymbolRequests.get(tickerSymbol);

			filtered.addAll(keepRelevantRequests(requests,
			        getRetrievedMonths(tickerSymbol, getEarliestStartDate(requests), getLatestEndDate(requests))));
		}

		return filtered;
	}

	private LocalDate getEarliestStartDate( final List<HistoryRetrievalRequest> requests ) {
		Date earliest = requests.get(0).getInclusiveStartDate();

		for (int i = 1; i < requests.size(); i++) {
			if (requests.get(i).getInclusiveStartDate().getTime() < earliest.getTime()) {
				earliest = requests.get(i).getInclusiveStartDate();
			}
		}

		return earliest.toLocalDate();
	}

	private LocalDate getLatestEndDate( final List<HistoryRetrievalRequest> requests ) {
		Date latest = requests.get(0).getExclusiveEndDate();

		for (int i = 1; i < requests.size(); i++) {
			if (requests.get(i).getExclusiveEndDate().getTime() > latest.getTime()) {
				latest = requests.get(i).getExclusiveEndDate();
			}
		}

		return latest.toLocalDate();
	}

	private List<RetrievedMonthTradingPrices> getRetrievedMonths( final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate ) {
		return retrievedHistoryDao.get(tickerSymbol, inclusiveStartDate.getYear(), exclusiveEndDate.getYear());
	}

	private Map<String, List<HistoryRetrievalRequest>> splitByTickerSymbol(
	        final List<HistoryRetrievalRequest> unfilteredRequests ) {
		final HashMap<String, List<HistoryRetrievalRequest>> split = new HashMap<>();

		for (final HistoryRetrievalRequest unfilteredRequest : unfilteredRequests) {
			final String key = unfilteredRequest.getTickerSymbol();
			final List<HistoryRetrievalRequest> requests = split.containsKey(key) ? split.get(key) : new ArrayList<>();
			requests.add(unfilteredRequest);
			split.put(unfilteredRequest.getTickerSymbol(), requests);
		}

		return split;
	}

	private List<HistoryRetrievalRequest> keepRelevantRequests( final List<HistoryRetrievalRequest> requests,
	        List<RetrievedMonthTradingPrices> alreadyRetrieved ) {
		List<HistoryRetrievalRequest> filtered = new ArrayList<>(requests.size());

		for (final HistoryRetrievalRequest request : requests) {
			if (isRelevantRequest(request, alreadyRetrieved)) {
				filtered.add(request);
			}
		}

		return filtered;
	}

	/**
	 * The price date range in the request is not stored in the local data source.
	 */
	private boolean isRelevantRequest( final HistoryRetrievalRequest request,
	        List<RetrievedMonthTradingPrices> alreadyRetrieved ) {
		final LocalDate startDate = request.getInclusiveStartDate().toLocalDate();
		final LocalDate endDate = request.getExclusiveEndDate().toLocalDate().minusDays(1);

		YearMonth unknown = YearMonth.of(startDate.getYear(), startDate.getMonthValue());
		final YearMonth end = YearMonth.of(endDate.getYear(), endDate.getMonthValue());

		for (int i = 0; i < alreadyRetrieved.size(); i++) {
			final RetrievedMonthTradingPrices retrieved = alreadyRetrieved.get(i);

			if (unknown.equals(retrieved.getYearMonth())) {
				if (unknown.equals(end)) {
					// Entire range is covered by already retrieved data
					return false;
				}

				// Progress to the next year month in the range
				unknown = unknown.plusMonths(1);
				i = 0;
			}
		}

		return true;
	}
}