/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.history.HistoryRetrievalRequestSlicer;
import com.systematic.trading.data.model.HibernateHistoryRetrievalRequest;
import com.systematic.trading.data.model.HistoryRetrievalRequest;

/**
 * Responsible for slicing up historical retrieval requests into monthly chuncks.
 * 
 * @author CJ Hare
 */
public class MonthlyHistoryRetrievalRequestSlicer implements HistoryRetrievalRequestSlicer {

	@Override
	/**
	 * Split up the date range into monthly chunks.
	 */
	public List<HistoryRetrievalRequest> slice( final String equityDataset, final String tickerSymbol,
	        final LocalDate startDateInclusive, final LocalDate endDateExclusive ) {

		final List<HistoryRetrievalRequest> requests = new ArrayList<>();
		final YearMonth endYearMonth = YearMonth.of(endDateExclusive.getYear(), endDateExclusive.getMonth());
		LocalDate workingInclusiveStartDate = startDateInclusive;

		// Bring the working start date to the start of the next month (if needed)
		if (isNotBeginningOfMonth(startDateInclusive)) {
			final LocalDate nextMonthStart = beginningOfNextMonth(startDateInclusive);
			requests.add(new HibernateHistoryRetrievalRequest(equityDataset, tickerSymbol, startDateInclusive,
			        nextMonthStart));
			workingInclusiveStartDate = nextMonthStart;
		}

		// Monthly entry for every in between
		while (isNotReached(endYearMonth, workingInclusiveStartDate)) {
			final LocalDate nextMonthStart = beginningOfNextMonth(workingInclusiveStartDate);
			requests.add(new HibernateHistoryRetrievalRequest(equityDataset, tickerSymbol, workingInclusiveStartDate,
			        nextMonthStart));
			workingInclusiveStartDate = nextMonthStart;
		}

		// Add the tail entry, when the end date is not beginning of a month
		if (isNotBeginningOfMonth(endDateExclusive)) {
			requests.add(new HibernateHistoryRetrievalRequest(equityDataset, tickerSymbol, workingInclusiveStartDate,
			        endDateExclusive));
		}

		return requests;
	}

	private boolean isNotReached( final YearMonth end, final LocalDate current ) {

		return end.isAfter(YearMonth.of(current.getYear(), current.getMonth()));
	}

	private boolean isNotBeginningOfMonth( final LocalDate startDateInclusive ) {

		return startDateInclusive.getDayOfMonth() != 1;
	}

	private LocalDate beginningOfNextMonth( final LocalDate date ) {

		return date.plusMonths(1).withDayOfMonth(1);
	}
}