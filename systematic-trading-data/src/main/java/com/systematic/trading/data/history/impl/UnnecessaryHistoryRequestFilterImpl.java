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
package com.systematic.trading.data.history.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		final Map<String, List<HistoryRetrievalRequest>> tickerSymbolRequests = splitByTickerSymbolSortByStartDate(
		        unfilteredRequests);

		for (final Map.Entry<String, List<HistoryRetrievalRequest>> entry : tickerSymbolRequests.entrySet()) {
			final List<HistoryRetrievalRequest> requests = entry.getValue();

			filtered.addAll(keepRelevantRequests(requests,
			        retrievedMonths(entry.getKey(), earliestStartDate(requests), latestEndDate(requests))));
		}

		return filtered;
	}

	private LocalDate earliestStartDate( final List<HistoryRetrievalRequest> requests ) {

		return requests.get(0).inclusiveStartDate().toLocalDate();
	}

	private LocalDate latestEndDate( final List<HistoryRetrievalRequest> requests ) {

		return requests.get(requests.size() - 1).exclusiveEndDate().toLocalDate();
	}

	private List<RetrievedMonthTradingPrices> retrievedMonths( final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate ) {

		return retrievedHistoryDao.requests(tickerSymbol, inclusiveStartDate.getYear(), exclusiveEndDate.getYear());
	}

	private Map<String, List<HistoryRetrievalRequest>> splitByTickerSymbolSortByStartDate(
	        final List<HistoryRetrievalRequest> unfilteredRequests ) {

		final HashMap<String, List<HistoryRetrievalRequest>> split = new HashMap<>();

		for (final HistoryRetrievalRequest unfilteredRequest : unfilteredRequests) {
			final String key = unfilteredRequest.tickerSymbol();
			final List<HistoryRetrievalRequest> requests = split.containsKey(key) ? split.get(key) : new ArrayList<>();
			requests.add(unfilteredRequest);
			split.put(unfilteredRequest.tickerSymbol(), sortByStartDate(requests));
		}

		return split;
	}

	private List<HistoryRetrievalRequest> sortByStartDate( final List<HistoryRetrievalRequest> requests ) {

		Collections.sort(requests, ( HistoryRetrievalRequest a, HistoryRetrievalRequest b ) -> a.inclusiveStartDate()
		        .compareTo(b.inclusiveStartDate()));
		return requests;
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

		final LocalDate startDate = request.inclusiveStartDate().toLocalDate();
		final LocalDate endDate = request.exclusiveEndDate().toLocalDate().minusDays(1);

		YearMonth unknown = YearMonth.of(startDate.getYear(), startDate.getMonthValue());
		final YearMonth end = YearMonth.of(endDate.getYear(), endDate.getMonthValue());

		final Set<YearMonth> retrieved = new HashSet<>();

		for (final RetrievedMonthTradingPrices prices : alreadyRetrieved) {
			retrieved.add(prices.yearMonth());
		}

		while (!unknown.isAfter(end)) {

			if (!retrieved.contains(unknown)) {
				return true;
			}

			// Progress to the next year month in the range
			unknown = unknown.plusMonths(1);
		}

		return false;
	}
}