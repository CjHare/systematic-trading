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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.history.impl.RetrievedYearMonthRecorder;
import com.systematic.trading.data.matcher.RetrievedMonthTradingPricesListMatcher;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;
import com.systematic.trading.data.util.TickerSymbolGenerator;

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
		tickerSymbol = TickerSymbolGenerator.generate();
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
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusEndEdge() {
		final LocalDate start = LocalDate.of(2010, 5, 1);
		final LocalDate end = LocalDate.of(2010, 6, 20);
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusStartEdge() {
		final LocalDate start = LocalDate.of(2010, 4, 7);
		final LocalDate end = LocalDate.of(2010, 5, 31);
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusBothEdges() {
		final LocalDate start = LocalDate.of(2010, 4, 7);
		final LocalDate end = LocalDate.of(2010, 6, 19);
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void twoWholeMonths() {
		final LocalDate start = LocalDate.of(2010, 5, 1);
		final LocalDate end = LocalDate.of(2010, 6, 30);
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6));
	}

	@Test
	public void oneYearOneMonth() {
		final LocalDate start = LocalDate.of(2010, 5, 1);
		final LocalDate end = LocalDate.of(2011, 6, 30);
		final List<HistoryRetrievalRequest> fulfilled = asList(create(start, end));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6), YearMonth.of(2010, 7), YearMonth.of(2010, 8),
		        YearMonth.of(2010, 9), YearMonth.of(2010, 10), YearMonth.of(2010, 11), YearMonth.of(2010, 12),
		        YearMonth.of(2011, 1), YearMonth.of(2011, 2), YearMonth.of(2011, 3), YearMonth.of(2011, 4),
		        YearMonth.of(2011, 5), YearMonth.of(2011, 6));
	}

	@Test
	public void twoRequestsOneWholeMonth() {
		final List<HistoryRetrievalRequest> fulfilled = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 31)),
		        create(LocalDate.of(2010, 5, 31), LocalDate.of(2010, 6, 30)));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6));
	}

	@Test
	public void twoRequestsSplitMonths() {
		final List<HistoryRetrievalRequest> fulfilled = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 15)),
		        create(LocalDate.of(2010, 6, 15), LocalDate.of(2010, 7, 31)));

		recorder.retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6), YearMonth.of(2010, 7));
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
					return new StringBuilder("[RetrievedMonthTradingPrices, tickerSymbol=").append(tickerSymbol)
					        .append(", ").append("YearMonth=").append(ym).append("]").toString();
				}
			});
		}

		return retrieved;
	}

	private List<HistoryRetrievalRequest> asList( final HistoryRetrievalRequest... requests ) {
		final List<HistoryRetrievalRequest> fulfilled = new ArrayList<HistoryRetrievalRequest>();

		for (final HistoryRetrievalRequest request : requests) {
			fulfilled.add(request);
		}

		return fulfilled;
	}

	private HistoryRetrievalRequest create( final LocalDate start, final LocalDate end ) {
		return new HistoryRetrievalRequest() {
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
		};
	}

	private void verifyNoMonthsRetrieved() {
		verifyZeroInteractions(retrievedMonthsDao);
	}
}