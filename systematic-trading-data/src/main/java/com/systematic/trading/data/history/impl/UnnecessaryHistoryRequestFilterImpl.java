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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.history.UnnecessaryHistoryRequestFilter;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Filters out unnecessary remote history requests.
 * 
 * @author CJ Hare
 */
public class UnnecessaryHistoryRequestFilterImpl implements UnnecessaryHistoryRequestFilter {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(UnnecessaryHistoryRequestFilterImpl.class);

	private final RetrievedMonthTradingPricesDao retrievedHistoryDao;

	public UnnecessaryHistoryRequestFilterImpl( final RetrievedMonthTradingPricesDao retrievedHistoryDao ) {

		this.retrievedHistoryDao = retrievedHistoryDao;
	}

	@Override
	public List<HistoryRetrievalRequest> filter( final List<HistoryRetrievalRequest> unfilteredRequests ) {

		if (unfilteredRequests == null) { return new ArrayList<>(0); }

		final Map<String, List<
		        HistoryRetrievalRequest>> tickerSymbolRequests = splitByTickerSymbolSortByStartDate(unfilteredRequests);

		final List<HistoryRetrievalRequest> filtered = new ArrayList<>(unfilteredRequests.size());

		for (final Map.Entry<String, List<HistoryRetrievalRequest>> entry : tickerSymbolRequests.entrySet()) {
			final List<HistoryRetrievalRequest> requests = entry.getValue();

			filtered.addAll(
			        keepNecessaryRequests(
			                requests,
			                retrievedMonths(entry.getKey(), earliestStartDate(requests), latestEndDate(requests))));
		}

		return filtered;
	}

	private LocalDate earliestStartDate( final List<HistoryRetrievalRequest> requests ) {

		return requests.get(0).startDateInclusive().toLocalDate();
	}

	private LocalDate latestEndDate( final List<HistoryRetrievalRequest> requests ) {

		return requests.get(requests.size() - 1).endDateExclusive().toLocalDate();
	}

	private List<RetrievedMonthTradingPrices> retrievedMonths(
	        final String tickerSymbol,
	        final LocalDate startDateInclusive,
	        final LocalDate endDateExclusive ) {

		return retrievedHistoryDao.requests(tickerSymbol, startDateInclusive.getYear(), endDateExclusive.getYear());
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

		Collections.sort(
		        requests,
		        ( HistoryRetrievalRequest a, HistoryRetrievalRequest b ) -> a.startDateInclusive()
		                .compareTo(b.startDateInclusive()));
		return requests;
	}

	private List<HistoryRetrievalRequest> keepNecessaryRequests(
	        final List<HistoryRetrievalRequest> requests,
	        final List<RetrievedMonthTradingPrices> alreadyRetrieved ) {

		logAlreadyRetrieved(alreadyRetrieved);

		List<HistoryRetrievalRequest> filtered = new ArrayList<>(requests.size());

		for (final HistoryRetrievalRequest request : requests) {
			logCandidateRequest(request);

			if (isNecessaryRequest(request, alreadyRetrieved)) {
				logNecessaryRequest(request);

				filtered.add(request);
			}
		}

		return filtered;
	}

	/**
	 * The date range in the request is not stored in the local data source.
	 */
	private boolean isNecessaryRequest(
	        final HistoryRetrievalRequest request,
	        List<RetrievedMonthTradingPrices> alreadyRetrieved ) {

		YearMonth unknown = yearMonth(request.startDateInclusive().toLocalDate());
		final YearMonth end = yearMonth(request.endDateExclusive().toLocalDate().minusDays(1));

		final Set<YearMonth> retrieved = new HashSet<>();

		for (final RetrievedMonthTradingPrices prices : alreadyRetrieved) {
			retrieved.add(prices.yearMonth());
		}

		while (!unknown.isAfter(end)) {

			if (!retrieved.contains(unknown)) { return true; }

			// Progress to the next month in the range
			unknown = unknown.plusMonths(1);
		}

		return false;
	}

	private YearMonth yearMonth( final LocalDate date ) {

		return YearMonth.of(date.getYear(), date.getMonthValue());
	}

	private void logAlreadyRetrieved( final List<RetrievedMonthTradingPrices> alreadyRetrieved ) {

		if (alreadyRetrieved.isEmpty()) {
			LOG.debug("No months in the requested range are available locally");
		} else {
			LOG.debug(
			        "Available locally: {}, {}",
			        () -> alreadyRetrieved.stream().map(retrieved -> retrieved.tickerSymbol())
			                .collect(Collectors.toSet()).stream().collect(Collectors.joining(", ")),
			        () -> alreadyRetrieved.stream().map(retrieved -> retrieved.yearMonth().toString())
			                .collect(Collectors.joining(", ")));
		}
	}

	private void logNecessaryRequest( final HistoryRetrievalRequest request ) {

		log("Necessary", request);
	}

	private void logCandidateRequest( final HistoryRetrievalRequest request ) {

		log("Candidate", request);
	}

	private void log( final String prefix, final HistoryRetrievalRequest request ) {

		LOG.debug(
		        "{}: {}, {}, {} (inclusive) - {} (exclusive)",
		        prefix,
		        request.tickerSymbol(),
		        request.equityDataset(),
		        request.startDateInclusive().toLocalDate(),
		        request.endDateExclusive().toLocalDate());
	}
}
