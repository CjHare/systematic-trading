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
package com.systematic.trading.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.model.HibernateRetrievedMonthTradingPrices;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Deals with recording when a full month of price data has been retrieved remotely, and in future will be available from the local data source.
 * 
 * @author CJ Hare
 */
public class RetrievedYearMonthRecorder {
	private final RetrievedMonthTradingPricesDao retrievedMonthsDao;

	public RetrievedYearMonthRecorder( final RetrievedMonthTradingPricesDao retrievedMonthsDao ) {
		this.retrievedMonthsDao = retrievedMonthsDao;
	}

	/**
	 * The given data has been successfully retrieved from the remote source.
	 * 
	 * @param fulfilledRequests days, months and years of price data that now will be available from the local data source.
	 */
	public void retrieved( final List<HistoryRetrievalRequest> fulfilledRequests ) {
		if (fulfilledRequests == null || fulfilledRequests.isEmpty()) {
			return;
		}

		final List<RetrievedMonthTradingPrices> retrieved = new ArrayList<>();

		for (final HistoryRetrievalRequest fulfilled : fulfilledRequests) {

			final String tickerSymbol = fulfilled.getTickerSymbol();
			final LocalDate start = fulfilled.getInclusiveStartDate().toLocalDate();
			final LocalDate end = fulfilled.getExclusiveEndDate().toLocalDate();

			if (isBeginningTradingMonth(start)) {
				retrieved.add(createRetrievedMonth(tickerSymbol, start));
			}

			if (isOverOneMonthBetween(start, end)) {
				LocalDate between = start.withDayOfMonth(1).plusMonths(1);

				while (between.isBefore(end) && hasDifferentYearMonth(between, end)) {
					retrieved.add(createRetrievedMonth(tickerSymbol, between));
					between = between.plusMonths(1);
				}

				if (isEndTradingMonth(end) || isMonthCompletedByOtherRequests(end, fulfilledRequests)) {
					retrieved.add(createRetrievedMonth(tickerSymbol, end));
				}
			}
		}

		retrievedMonthsDao.create(retrieved);
	}

	private boolean isOverOneMonthBetween( final LocalDate start, final LocalDate end ) {
		final LocalDate oneMonthOut = start.plusMonths(1);
		return oneMonthOut.isBefore(end) || oneMonthOut.isEqual(end);
	}

	private boolean isMonthCompletedByOtherRequests( final LocalDate end,
	        final List<HistoryRetrievalRequest> fulfilledRequests ) {
		LocalDate expectedStart = end.plusDays(1);

		// Unordered list :. cannot guarantee where to find following request(s)
		for (int i = 0; i < fulfilledRequests.size(); i++) {
			final LocalDate contender = fulfilledRequests.get(i).getInclusiveStartDate().toLocalDate();

			if (expectedStart.isEqual(contender)) {
				if (isEndTradingMonth(contender)) {
					return true;
				}

				// Start the search again from the beginning with the next expected start
				expectedStart = fulfilledRequests.get(i).getExclusiveEndDate().toLocalDate().plusDays(1);
				i = 0;
			}
		}

		return false;
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
}