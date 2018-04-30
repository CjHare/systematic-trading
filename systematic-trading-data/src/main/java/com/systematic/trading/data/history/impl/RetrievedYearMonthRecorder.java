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
import java.util.Optional;
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

		final List<RetrievedMonthTradingPrices> retrieved = new ArrayList<>();
		final Map<String, List<HistoryRetrievalRequest>> byTickeSymbol = splitByTickerSymbol(fulfilledRequests);

		for (final Map.Entry<String, List<HistoryRetrievalRequest>> entry : byTickeSymbol.entrySet()) {

			final Set<DateRange> ranges = extractDateRanges(entry.getValue());
			final Set<YearMonth> fulfilledMonths = onlyFullMonths(ranges);

			for (final YearMonth fulfilledMonth : fulfilledMonths) {
				retrieved.add(tradningPrices(entry.getKey(), fulfilledMonth));
			}
		}

		log(retrieved);
		store(retrieved);
	}

	private RetrievedMonthTradingPrices tradningPrices( final String tickerSymbol, final YearMonth fulfilledMonth ) {

		return new HibernateRetrievedMonthTradingPrices(tickerSymbol, fulfilledMonth);
	}

	/**
	 * Creates a YearMonth entry for each of the full months covered by the given date ranges.
	 */
	private Set<YearMonth> onlyFullMonths( final Set<DateRange> dateRanges ) {

		final Set<YearMonth> fullMonths = new HashSet<>();

		for (final DateRange range : dateRanges) {

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
	private YearMonth beginsFirstOfMonth( final DateRange range ) {

		final LocalDate startDateInclusive = range.startDateInclusive();

		if (startDateInclusive.getDayOfMonth() == 1) {
			return YearMonth.of(startDateInclusive.getYear(), startDateInclusive.getMonthValue());
		} else {
			final LocalDate nextMonth = startDateInclusive.plusMonths(1);
			return YearMonth.of(nextMonth.getYear(), nextMonth.getMonthValue());
		}
	}

	private YearMonth endsLastOfMonth( final DateRange range ) {

		final LocalDate previousMonth = range.endDateExclusive().minusMonths(1);
		return YearMonth.of(previousMonth.getYear(), previousMonth.getMonthValue());
	}

	private Set<DateRange> extractDateRanges( final List<HistoryRetrievalRequest> requests ) {

		final Set<DateRange> ranges = new HashSet<>();
		final List<HistoryRetrievalRequest> unspent = new ArrayList<>(requests);

		while (!unspent.isEmpty()) {

			final HistoryRetrievalRequest earliest = earliestStartDate(unspent);
			unspent.remove(earliest);

			// TODO tidy up this code
			Optional<HistoryRetrievalRequest> crossOver = startDateCrossOver(earliest.exclusiveEndDate(), unspent);
			Optional<HistoryRetrievalRequest> nextCrossOver = crossOver;

			while (nextCrossOver.isPresent()) {
				unspent.remove(crossOver.get());

				nextCrossOver = startDateCrossOver(crossOver.get().exclusiveEndDate(), unspent);

				if (nextCrossOver.isPresent()) {
					crossOver = nextCrossOver;
				}
			}

			if (crossOver.isPresent()) {
				ranges.add(dateRange(earliest, crossOver.get()));
			} else {
				ranges.add(dateRange(earliest));
			}
		}

		return ranges;
	}

	// TODO multiple cross overs (not duplicates, similar problem)

	private DateRange dateRange( final HistoryRetrievalRequest range ) {

		return new DateRange(range.inclusiveStartDate().toLocalDate(), range.exclusiveEndDate().toLocalDate());
	}

	private DateRange dateRange( final HistoryRetrievalRequest start, final HistoryRetrievalRequest end ) {

		return new DateRange(start.inclusiveStartDate().toLocalDate(), end.exclusiveEndDate().toLocalDate());
	}

	/**
	 * Finds the request whose with the latest end date, whose start date is before the given end
	 * date.
	 */
	private Optional<HistoryRetrievalRequest> startDateCrossOver(
	        final Date exclusiveEndDate,
	        final List<HistoryRetrievalRequest> candidates ) {

		Optional<HistoryRetrievalRequest> crossOver = Optional.empty();

		for (final HistoryRetrievalRequest candidate : candidates) {

			if (candidate.inclusiveStartDate().getTime() <= exclusiveEndDate.getTime()) {

				if (!crossOver.isPresent() || (crossOver.isPresent()
				        && candidate.exclusiveEndDate().getTime() > crossOver.get().exclusiveEndDate().getTime())) {

					crossOver = Optional.of(candidate);
				}
			}
		}

		return crossOver;
	}

	private HistoryRetrievalRequest earliestStartDate( final List<HistoryRetrievalRequest> requests ) {

		HistoryRetrievalRequest earliest = requests.get(0);

		for (final HistoryRetrievalRequest candidate : requests) {
			if (candidate.inclusiveStartDate().getTime() < earliest.inclusiveStartDate().getTime()) {
				earliest = candidate;
			}
		}

		return earliest;
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

	private void store( final List<RetrievedMonthTradingPrices> retrieved ) {

		if (!retrieved.isEmpty()) {
			retrievedMonthsDao.create(retrieved);
		}
	}

	private void log( final List<RetrievedMonthTradingPrices> prices ) {

		LOG.debug(
		        "Retrieved: {}, {}",
		        () -> prices.stream().map(price -> price.tickerSymbol()).collect(Collectors.toSet()).stream()
		                .collect(Collectors.joining(", ")),
		        () -> prices.stream().map(price -> price.yearMonth().toString()).collect(Collectors.joining(", ")));
	}
}

// TODO this class may already exist in model - look!!!!
// TODO new class file somewhere
// TODO inclusive / exclusive dates
class DateRange {

	private final LocalDate startDateInclusive;
	private final LocalDate endDateExclusive;

	public DateRange( final LocalDate startDateInclusive, final LocalDate endDateExclusive ) {

		this.startDateInclusive = startDateInclusive;
		this.endDateExclusive = endDateExclusive;
	}

	public LocalDate startDateInclusive() {

		return startDateInclusive;
	}

	public LocalDate endDateExclusive() {

		return endDateExclusive;
	}
}
