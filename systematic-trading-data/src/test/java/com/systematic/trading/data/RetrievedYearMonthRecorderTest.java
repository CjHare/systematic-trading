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

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.matcher.RetrievedMonthTradingPricesListMatcher;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Does the RetrievedYearMonthRecorder record the year months correctly?
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RetrievedYearMonthRecorderTest {

	@Mock
	private RetrievedMonthTradingPricesDao retrievedMonthsDao;

	/** Instance being tested.*/
	private RetrievedYearMonthRecorder recorder;

	/** Random string re-generated every run.*/
	private String tickerSymbol;

	@Before
	public void setUp() {
		recorder = new RetrievedYearMonthRecorder(retrievedMonthsDao);
		tickerSymbol = generateTickerSymbol();
	}

	@Test
	public void retrievedNull() {
		recorder.retrieved(null);

		verifyNoMonthsRetrieved();
	}

	@Test
	public void retrievedNothing() {
		recorder.retrieved(new ArrayList<HistoryRetrievalRequest>());

		verifyNoMonthsRetrieved();
	}

	@Test
	public void oneWholeMonth() {
		final LocalDate start = LocalDate.of(2010, 5, 1);
		final LocalDate end = LocalDate.of(2010, 5, 31);
		final List<HistoryRetrievalRequest> fulfilled = create(start, end);

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	private void verifyMonths( final YearMonth... month ) {

		verify(retrievedMonthsDao).create(argThat(new RetrievedMonthTradingPricesListMatcher(create(month))));
		verifyNoMoreInteractions(retrievedMonthsDao);
	}

	private List<RetrievedMonthTradingPrices> create( final YearMonth... yms ) {
		final List<RetrievedMonthTradingPrices> retrieved = new ArrayList<>();

		for (final YearMonth ym : yms) {
			retrieved.add(new RetrievedMonthTradingPrices() {

				@Override
				public YearMonth getYearMonth() {
					return ym;
				}

				@Override
				public String getTickerSymbol() {
					return tickerSymbol;
				}

				@Override
				public String toString() {
					return new StringBuilder().append(tickerSymbol).append(", ").append(ym).toString();
				}
			});
		}

		return retrieved;
	}

	private List<HistoryRetrievalRequest> create( final LocalDate start, final LocalDate end ) {
		final List<HistoryRetrievalRequest> fulfilled = new ArrayList<HistoryRetrievalRequest>();

		fulfilled.add(new HistoryRetrievalRequest() {

			@Override
			public String getTickerSymbol() {
				return tickerSymbol;
			}

			@Override
			public Date getInclusiveStartDate() {
				return Date.valueOf(start);
			}

			@Override
			public Date getExclusiveEndDate() {
				return Date.valueOf(end);
			}
		});

		return fulfilled;
	}

	private void verifyNoMonthsRetrieved() {
		verifyZeroInteractions(retrievedMonthsDao);
	}

	private String generateTickerSymbol() {
		final int leftLimit = 97; // letter 'a'
		final int rightLimit = 122; // letter 'z'
		final int range = rightLimit - leftLimit;
		final int length = 4;

		final StringBuilder symbol = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			symbol.append((char) (ThreadLocalRandom.current().nextInt(range) + leftLimit));
		}

		return symbol.toString();
	}
}