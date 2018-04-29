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
package com.systematic.trading.data;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

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
import com.systematic.trading.data.util.HistoryRetrievalRequestUtil;
import com.systematic.trading.data.util.RetrievedMonthTradingPricesUtil;
import com.systematic.trading.data.util.RandomStringGenerator;

/**
 * Does the RetrievedYearMonthRecorder record the year months correctly?
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RetrievedYearMonthRecorderTest {

	@Mock
	private RetrievedMonthTradingPricesDao retrievedMonthsDao;

	private final HistoryRetrievalRequestUtil historyRetrievalRequestUtil = new HistoryRetrievalRequestUtil();

	private final RetrievedMonthTradingPricesUtil retrievedMonthTradingPricesUtil = new RetrievedMonthTradingPricesUtil();

	/** Instance being tested. */
	private RetrievedYearMonthRecorder recorder;

	/** Random string re-generated every run. */
	private String datasetId;

	/** Random string re-generated every run. */
	private String tickerSymbol;

	// TODO multiple ticker symbols

	// TODO requests that cross-over

	// TODO multiple cross overs (not duplicates, similar problem)

	@Before
	public void setUp() {

		recorder = new RetrievedYearMonthRecorder(retrievedMonthsDao);
		tickerSymbol = RandomStringGenerator.generate();
		datasetId = RandomStringGenerator.generate();
	}

	@Test
	public void retrievedNull() {

		final List<HistoryRetrievalRequest> fulfilled = null;

		retrieved(fulfilled);

		verifyNoMonthsRetrieved();
	}

	@Test
	public void retrievedNothing() {

		final List<HistoryRetrievalRequest> fulfilled = new ArrayList<HistoryRetrievalRequest>();

		retrieved(fulfilled);

		verifyNoMonthsRetrieved();
	}

	@Test
	public void oneWholeMonth() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 5, 1);
		final LocalDate endDateExclusive = LocalDate.of(2010, 6, 1);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusEndEdge() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 5, 1);
		final LocalDate endDateExclusive = LocalDate.of(2010, 6, 20);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusStartEdge() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 4, 7);
		final LocalDate endDateExclusive = LocalDate.of(2010, 6, 1);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void oneWholeMonthPlusBothEdges() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 4, 7);
		final LocalDate endDateExclusive = LocalDate.of(2010, 6, 19);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5));
	}

	@Test
	public void twoWholeMonths() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 5, 1);
		final LocalDate endDateExclusive = LocalDate.of(2010, 7, 1);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6));
	}

	@Test
	public void oneYearOneMonth() {

		final LocalDate startDateInclusive = LocalDate.of(2010, 5, 1);
		final LocalDate endDateExclusive = LocalDate.of(2011, 7, 5);
		final List<HistoryRetrievalRequest> fulfilled = asList(request(startDateInclusive, endDateExclusive));

		retrieved(fulfilled);

		verifyMonths(
		        YearMonth.of(2010, 5),
		        YearMonth.of(2010, 6),
		        YearMonth.of(2010, 7),
		        YearMonth.of(2010, 8),
		        YearMonth.of(2010, 9),
		        YearMonth.of(2010, 10),
		        YearMonth.of(2010, 11),
		        YearMonth.of(2010, 12),
		        YearMonth.of(2011, 1),
		        YearMonth.of(2011, 2),
		        YearMonth.of(2011, 3),
		        YearMonth.of(2011, 4),
		        YearMonth.of(2011, 5),
		        YearMonth.of(2011, 6));
	}

	@Test
	public void twoRequestsTwoWholeMonths() {

		final List<HistoryRetrievalRequest> fulfilled = asList(
		        request(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 31)),
		        request(LocalDate.of(2010, 5, 31), LocalDate.of(2010, 7, 1)));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6));
	}

	@Test
	public void twoRequestsSplitMonths() {

		final List<HistoryRetrievalRequest> fulfilled = asList(
		        request(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 15)),
		        request(LocalDate.of(2010, 6, 15), LocalDate.of(2010, 8, 1)));

		retrieved(fulfilled);

		verifyMonths(YearMonth.of(2010, 5), YearMonth.of(2010, 6), YearMonth.of(2010, 7));
	}

	private void retrieved( final List<HistoryRetrievalRequest> fulfilled ) {

		recorder.retrieved(fulfilled);
	}

	private void verifyMonths( final YearMonth... month ) {

		verify(retrievedMonthsDao).create(
		        argThat(
		                new RetrievedMonthTradingPricesListMatcher(
		                        retrievedMonthTradingPricesUtil.create(tickerSymbol, month))));
		verifyNoMoreInteractions(retrievedMonthsDao);
	}

	private List<HistoryRetrievalRequest> asList( final HistoryRetrievalRequest... requests ) {

		return historyRetrievalRequestUtil.asList(requests);
	}

	private HistoryRetrievalRequest request( final LocalDate startDateInclusive, final LocalDate endDateExclusive ) {

		return historyRetrievalRequestUtil.create(datasetId, tickerSymbol, startDateInclusive, endDateExclusive);
	}

	private void verifyNoMonthsRetrieved() {

		verifyZeroInteractions(retrievedMonthsDao);
	}
}
