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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
import com.systematic.trading.data.history.UnnecessaryHistoryRequestFilter;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.util.HistoryRetrievalRequestUtil;
import com.systematic.trading.data.util.RandomStringGenerator;
import com.systematic.trading.data.util.RetrievedMonthTradingPricesUtil;

/**
 * Are unnecessary requests being identified correctly?
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class UnnecessaryHistoryRequestFilterImplTest {

	@Mock
	private RetrievedMonthTradingPricesDao retrievedHistoryDao;

	private final HistoryRetrievalRequestUtil historyRetrievalRequestUtil = new HistoryRetrievalRequestUtil();
	private final RetrievedMonthTradingPricesUtil retrievedMonthTradingPricesUtil = new RetrievedMonthTradingPricesUtil();

	/** Instance being tested.*/
	private UnnecessaryHistoryRequestFilter filter;

	/** Random string re-generated every run.*/
	private String tickerSymbol;

	/** Random string re-generated every run.*/
	private String dataset;

	@Before
	public void setUp() {

		filter = new UnnecessaryHistoryRequestFilterImpl(retrievedHistoryDao);
		tickerSymbol = RandomStringGenerator.generate();
		dataset = RandomStringGenerator.generate();
	}

	@Test
	public void filterNull() {

		final List<HistoryRetrievalRequest> filtered = filter(null);

		verifyNoRequests(filtered);
		verfiyNoLoclaHistoryRequest(filtered);
	}

	@Test
	public void filterNone() {

		final List<HistoryRetrievalRequest> filtered = filter(new ArrayList<>());

		verifyNoRequests(filtered);
		verfiyNoLoclaHistoryRequest(filtered);
	}

	@Test
	public void filterMonthRequestNoHistory() {

		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 31)));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyRetrievalRequests(unfilteredRequests, filtered);
		verifyLocalHistoryRequest(2010, 2010);
	}

	@Test
	public void filterUnnecessaryMonthRequest() {

		final int startYear = 2010;
		final int endYear = 2010;
		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(startYear, 5, 1), LocalDate.of(endYear, 5, 31)));
		setUpLocalHistory(YearMonth.of(startYear, 5));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyNoRequests(filtered);
		verifyLocalHistoryRequest(startYear, endYear);
	}

	@Test
	public void filterUnnecessaryTwoMonthRequest() {

		final int startYear = 2010;
		final int endYear = 2010;
		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(startYear, 5, 1), LocalDate.of(endYear, 7, 1)));
		setUpLocalHistory(YearMonth.of(startYear, 5), YearMonth.of(startYear, 6));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyNoRequests(filtered);
		verifyLocalHistoryRequest(startYear, endYear);
	}

	@Test
	public void filterPartiallyCoveredRequest() {

		final int startYear = 2010;
		final int endYear = 2010;
		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(startYear, 6, 1), LocalDate.of(endYear, 12, 31)));
		setUpLocalHistory(YearMonth.of(startYear, 7), YearMonth.of(startYear, 8), YearMonth.of(startYear, 10));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyRetrievalRequests(unfilteredRequests, filtered);
		verifyLocalHistoryRequest(startYear, endYear);
	}

	@Test
	public void filterSomeUnnecessaryRequests() {

		final int startYear = 2010;
		final int endYear = 2010;
		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(startYear, 6, 1), LocalDate.of(endYear, 7, 1)),
		        create(LocalDate.of(startYear, 7, 1), LocalDate.of(endYear, 8, 1)),
		        create(LocalDate.of(startYear, 8, 1), LocalDate.of(endYear, 9, 1)),
		        create(LocalDate.of(startYear, 9, 1), LocalDate.of(endYear, 10, 1)),
		        create(LocalDate.of(startYear, 10, 1), LocalDate.of(endYear, 11, 1)),
		        create(LocalDate.of(startYear, 11, 1), LocalDate.of(endYear, 12, 1)));
		setUpLocalHistory(YearMonth.of(startYear, 7), YearMonth.of(startYear, 8), YearMonth.of(startYear, 10));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyRetrievalRequests(asList(create(LocalDate.of(startYear, 6, 1), LocalDate.of(endYear, 7, 1)),
		        create(LocalDate.of(startYear, 9, 1), LocalDate.of(endYear, 10, 1)),
		        create(LocalDate.of(startYear, 11, 1), LocalDate.of(endYear, 12, 1))), filtered);
		verifyLocalHistoryRequest(startYear, endYear);
	}

	@Test
	public void filterSomeUnnecessaryRequestsDisordered() {

		final int startYear = 2010;
		final int endYear = 2010;
		final List<HistoryRetrievalRequest> unfilteredRequests = asList(
		        create(LocalDate.of(startYear, 10, 1), LocalDate.of(endYear, 11, 1)),
		        create(LocalDate.of(startYear, 9, 1), LocalDate.of(endYear, 10, 1)),
		        create(LocalDate.of(startYear, 7, 1), LocalDate.of(endYear, 8, 1)),
		        create(LocalDate.of(startYear, 8, 1), LocalDate.of(endYear, 9, 1)),
		        create(LocalDate.of(startYear, 6, 1), LocalDate.of(endYear, 7, 1)),
		        create(LocalDate.of(startYear, 11, 1), LocalDate.of(endYear, 12, 1)));
		setUpLocalHistory(YearMonth.of(startYear, 7), YearMonth.of(startYear, 8), YearMonth.of(startYear, 10));

		final List<HistoryRetrievalRequest> filtered = filter(unfilteredRequests);

		verifyRetrievalRequests(asList(create(LocalDate.of(startYear, 6, 1), LocalDate.of(endYear, 7, 1)),
		        create(LocalDate.of(startYear, 9, 1), LocalDate.of(endYear, 10, 1)),
		        create(LocalDate.of(startYear, 11, 1), LocalDate.of(endYear, 12, 1))), filtered);
		verifyLocalHistoryRequest(startYear, endYear);
	}

	private List<HistoryRetrievalRequest> filter( final List<HistoryRetrievalRequest> unfilteredRequests ) {

		return filter.filter(unfilteredRequests);
	}

	private void setUpLocalHistory( final YearMonth... ym ) {

		when(retrievedHistoryDao.requests(anyString(), anyInt(), anyInt()))
		        .thenReturn(retrievedMonthTradingPricesUtil.create(tickerSymbol, ym));
	}

	private void verifyLocalHistoryRequest( final int startYear, final int endYear ) {

		verify(retrievedHistoryDao).requests(tickerSymbol, startYear, endYear);
		verifyNoMoreInteractions(retrievedHistoryDao);
	}

	private void verifyRetrievalRequests( final List<HistoryRetrievalRequest> expected,
	        final List<HistoryRetrievalRequest> actual ) {

		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
		for (final HistoryRetrievalRequest expectedRequest : expected) {
			historyRetrievalRequestUtil.contains(expectedRequest, actual);
		}
	}

	private List<HistoryRetrievalRequest> asList( final HistoryRetrievalRequest... requests ) {

		return historyRetrievalRequestUtil.asList(requests);
	}

	private HistoryRetrievalRequest create( final LocalDate start, final LocalDate end ) {

		return historyRetrievalRequestUtil.create(dataset, tickerSymbol, start, end);
	}

	private void verifyNoRequests( final List<HistoryRetrievalRequest> filtered ) {

		assertNotNull(filtered);
		assertTrue("Expecing no requests left after filtering", filtered.isEmpty());
	}

	private void verfiyNoLoclaHistoryRequest( final List<HistoryRetrievalRequest> filtered ) {

		verifyZeroInteractions(retrievedHistoryDao);
	}
}