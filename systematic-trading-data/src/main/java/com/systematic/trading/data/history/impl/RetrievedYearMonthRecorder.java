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

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.history.RetrievedHistoryPeriodRecorder;
import com.systematic.trading.data.model.HibernateRetrievedMonthTradingPrices;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Deals with recording when a full month of price data has been retrieved remotely, and in future
 * will be available from the local data source.
 * 
 * @author CJ Hare
 */
public class RetrievedYearMonthRecorder implements RetrievedHistoryPeriodRecorder {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(RetrievedYearMonthRecorder.class);

	private final RetrievedMonthTradingPricesDao retrievedMonthsDao;

	public RetrievedYearMonthRecorder( final RetrievedMonthTradingPricesDao retrievedMonthsDao ) {

		this.retrievedMonthsDao = retrievedMonthsDao;
	}

	@Override
	public void retrieved( final List<HistoryRetrievalRequest> fulfilledRequests ) {

		if (hasFulfilledNothing(fulfilledRequests)) { return; }

		final List<RetrievedMonthTradingPrices> persist = new ArrayList<>();
		final Map<String, List<HistoryRetrievalRequest>> byTickeSymbol = splitByTickerSymbol(fulfilledRequests);

		for (final Map.Entry<String, List<HistoryRetrievalRequest>> entry : byTickeSymbol.entrySet()) {

			final Set<DateRange<LocalDate>> ranges = extractDateRanges(entry.getValue());
			final Set<YearMonth> fulfilledMonths = onlyFullMonths(ranges);

			for (final YearMonth fulfilledMonth : fulfilledMonths) {
				persist.add(tradningPrices(entry.getKey(), fulfilledMonth));
			}
		}

		logRequests(fulfilledRequests);
		log(persist);
		store(persist);
	}

	private RetrievedMonthTradingPrices tradningPrices( final String tickerSymbol, final YearMonth fulfilledMonth ) {

		return new HibernateRetrievedMonthTradingPrices(tickerSymbol, fulfilledMonth);
	}

	/**
	 * Creates a YearMonth entry for each of the full months covered by the given date ranges.
	 */
	private Set<YearMonth> onlyFullMonths( final Set<DateRange<LocalDate>> dateRanges ) {

		final Set<YearMonth> fullMonths = new HashSet<>();

		for (final DateRange<LocalDate> range : dateRanges) {

			final YearMonth start = beginsFirstOfMonth(range);
			final YearMonth end = endsLastOfMonth(range);
			YearMonth fullMonth = start;

			if (atLeastOneMonthBetween(start, end)) {
				fullMonths.add(start);

				while (fullMonth.isBefore(end)) {

					fullMonth = fullMonth.plusMonths(1);
					fullMonths.add(fullMonth);
				}

				fullMonths.add(end);
			}
		}

		return fullMonths;
	}

	private boolean atLeastOneMonthBetween( final YearMonth start, final YearMonth end ) {

		return !start.isAfter(end);
	}

	/**
	 * The first month after or on the given date that is the first day of the month.
	 */
	private YearMonth beginsFirstOfMonth( final DateRange<LocalDate> range ) {

		final LocalDate startDateInclusive = range.startDateInclusive();

		if (startDateInclusive.getDayOfMonth() == 1) {
			return YearMonth.of(startDateInclusive.getYear(), startDateInclusive.getMonthValue());
		} else {
			final LocalDate nextMonth = startDateInclusive.plusMonths(1);
			return YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue());
		}
	}

	private YearMonth endsLastOfMonth( final DateRange<LocalDate> range ) {

		final LocalDate previousMonth = range.endDateExclusive().minusMonths(1);
		return YearMonth.of(previousMonth.getYear(), previousMonth.getMonthValue());
	}

	private Set<DateRange<LocalDate>> extractDateRanges( final List<HistoryRetrievalRequest> requests ) {

		final Set<DateRange<LocalDate>> ranges = new HashSet<>();
		final List<HistoryRetrievalRequest> unspent = new ArrayList<>(requests);

		while (!unspent.isEmpty()) {

			final HistoryRetrievalRequest earliest = earliestStartDate(unspent);
			unspent.remove(earliest);

			final List<HistoryRetrievalRequest> chained = chainedRequests(earliest.endDateExclusive(), unspent);
			unspent.removeAll(chained);

			if (chained.isEmpty()) {
				ranges.add(dateRange(earliest));
			} else {

				final HistoryRetrievalRequest latest = latestEndDate(chained);
				ranges.add(dateRange(earliest, latest));
			}
		}

		return ranges;
	}

	/**
	 * Retrieve the set of requests that form a chain, with the start date before or on the given
	 * end date.
	 */
	private List<HistoryRetrievalRequest> chainedRequests(
	        final Date earliestEndDateExclusive,
	        final List<HistoryRetrievalRequest> candidates ) {

		final List<HistoryRetrievalRequest> chain = new ArrayList<>(candidates.size());
		Date latest = earliestEndDateExclusive;

		for (final HistoryRetrievalRequest candidate : candidates) {

			if (candidate.startDateInclusive().getTime() <= latest.getTime()) {
				chain.add(candidate);

				if (candidate.endDateExclusive().getTime() > latest.getTime()) {
					latest = candidate.endDateExclusive();
				}
			}
		}

		return chain;
	}

	private DateRange<LocalDate> dateRange( final HistoryRetrievalRequest range ) {

		return new DateRange<>(range.startDateInclusive().toLocalDate(), range.endDateExclusive().toLocalDate());
	}

	private DateRange<LocalDate> dateRange( final HistoryRetrievalRequest start, final HistoryRetrievalRequest end ) {

		return new DateRange<>(start.startDateInclusive().toLocalDate(), end.endDateExclusive().toLocalDate());
	}

	private HistoryRetrievalRequest earliestStartDate( final List<HistoryRetrievalRequest> requests ) {

		HistoryRetrievalRequest earliest = requests.get(0);

		for (final HistoryRetrievalRequest candidate : requests) {
			if (candidate.startDateInclusive().getTime() < earliest.startDateInclusive().getTime()) {
				earliest = candidate;
			}
		}

		return earliest;
	}

	private HistoryRetrievalRequest latestEndDate( final List<HistoryRetrievalRequest> requests ) {

		HistoryRetrievalRequest latest = requests.get(0);

		for (final HistoryRetrievalRequest candidate : requests) {
			if (candidate.endDateExclusive().getTime() > latest.endDateExclusive().getTime()) {
				latest = candidate;
			}
		}

		return latest;
	}

	private Map<String, List<HistoryRetrievalRequest>> splitByTickerSymbol(
	        final List<HistoryRetrievalRequest> requests ) {

		final Map<String, List<HistoryRetrievalRequest>> split = new HashMap<>();

		for (final HistoryRetrievalRequest request : requests) {

			ensureListExists(split, request.tickerSymbol());

			final List<HistoryRetrievalRequest> alreadySplit = split.get(request.tickerSymbol());
			alreadySplit.add(request);
			split.put(request.tickerSymbol(), alreadySplit);
		}

		return split;
	}

	private void ensureListExists( final Map<String, List<HistoryRetrievalRequest>> split, String key ) {

		if (!split.containsKey(key)) {
			split.put(key, new ArrayList<>());
		}
	}

	private boolean hasFulfilledNothing( final List<HistoryRetrievalRequest> fulfilledRequests ) {

		return fulfilledRequests == null || fulfilledRequests.isEmpty();
	}

	private void store( final List<RetrievedMonthTradingPrices> additionalPriceData ) {

		if (!additionalPriceData.isEmpty()) {
			retrievedMonthsDao.create(additionalPriceData);
		}
	}

	private void logRequests( final List<HistoryRetrievalRequest> fulfilledRequests ) {

		LOG.debug(
		        "Retrieved price data: [{}], [{}]",
		        () -> fulfilledRequests.stream().map(request -> request.tickerSymbol()).collect(Collectors.toSet())
		                .stream().collect(Collectors.joining(", ")),
		        () -> fulfilledRequests.stream()
		                .map(
		                        request -> request.startDateInclusive().toString() + " - "
		                                + request.endDateExclusive().toString())
		                .collect(Collectors.joining(", ")));
	}

	private void log( final List<RetrievedMonthTradingPrices> persist ) {

		if (persist.isEmpty()) {
			LOG.debug("No months to persist. Every whole month is already persisted");
		} else {
			LOG.debug(
			        "Month to persist: [{}], [{}]",
			        () -> persist.stream().map(price -> price.tickerSymbol()).collect(Collectors.toSet()).stream()
			                .collect(Collectors.joining(", ")),
			        () -> persist.stream().map(price -> price.yearMonth().toString())
			                .collect(Collectors.joining(", ")));
		}
	}

}
