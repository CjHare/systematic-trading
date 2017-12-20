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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import com.systematic.trading.data.history.HistoryRetrievalRequestMerger;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.builder.HistoryRetrievalRequestBuilder;
import com.systematic.trading.data.util.HistoryRetrievalRequestUtil;
import com.systematic.trading.data.util.RandomStringGenerator;

/**
 * HistoryRetrievalRequestMergerImpl behaviour verification.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryRetrievalRequestMergerImplTest {

	private static final Period MAXIMUM = Period.ofMonths(3);

	@Mock
	private HistoryRetrievalRequestBuilder requestBuilder;

	private final HistoryRetrievalRequestUtil historyRetrievalRequestUtil = new HistoryRetrievalRequestUtil();

	/** Instance being tested.*/
	private HistoryRetrievalRequestMerger merger;

	/** Random string re-generated every run.*/
	private String tickerSymbol;

	/** Random string re-generated every run.*/
	private String dataset;

	@Before
	public void setUp() {

		merger = new HistoryRetrievalRequestMergerImpl(requestBuilder);
		tickerSymbol = RandomStringGenerator.generate();
		dataset = RandomStringGenerator.generate();
	}

	@Test
	public void nullRequests() {

		final List<HistoryRetrievalRequest> merged = merge(null, null);

		assertNull(merged);
	}

	@Test
	public void noRequestsNoMaximum() {

		final List<HistoryRetrievalRequest> merged = merge(new ArrayList<HistoryRetrievalRequest>(), null);

		verifyRetrievalRequests(Collections.emptyList(), merged);
	}

	@Test
	public void oneRequestNoMaximum() {

		final List<HistoryRetrievalRequest> toMerge = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 31)));
		setUpBuilder(toMerge);

		final List<HistoryRetrievalRequest> merged = merge(toMerge, null);

		verifyRetrievalRequests(toMerge, merged);
	}

	@Test
	public void twoNonConsercutiveRequestsNoMaximum() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1)));
		setUpBuilder(toMerge);

		final List<HistoryRetrievalRequest> merged = merge(toMerge, null);

		verifyRetrievalRequests(toMerge, merged);
	}

	@Test
	public void noRequests() {

		final List<HistoryRetrievalRequest> merged = merge(new ArrayList<HistoryRetrievalRequest>());

		verifyRetrievalRequests(Collections.emptyList(), merged);
	}

	@Test
	public void oneRequest() {

		final List<HistoryRetrievalRequest> toMerge = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 5, 31)));
		setUpBuilder(toMerge);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(toMerge, merged);
	}

	@Test
	public void twoNonConsercutiveRequests() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1)));
		setUpBuilder(toMerge);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(toMerge, merged);
	}

	@Test
	public void twoConsercutiveRequests() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1)));
		final List<HistoryRetrievalRequest> expected = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 7, 1)));
		setUpBuilder(expected);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(expected, merged);
	}

	@Test
	public void threeConsercutiveRequestsSpanningMaximum() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 3, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 9, 1)),
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 12, 1)));
		final List<HistoryRetrievalRequest> expected = asList(
		        create(LocalDate.of(2010, 3, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 9, 1)),
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 12, 1)));
		setUpBuilder(expected);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(expected, merged);
	}

	@Test
	public void longRequest() {

		final List<HistoryRetrievalRequest> toMerge = asList(
		        create(LocalDate.of(2010, 1, 1), LocalDate.of(2010, 12, 1)));

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(toMerge, merged);
	}

	@Test
	public void overTwoMaximumPeriodsOfRequests() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 3, 1), LocalDate.of(2010, 4, 1)),
		        create(LocalDate.of(2010, 4, 1), LocalDate.of(2010, 5, 1)),
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1)),
		        create(LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1)),
		        create(LocalDate.of(2010, 8, 1), LocalDate.of(2010, 9, 1)),
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1)),
		        create(LocalDate.of(2010, 10, 1), LocalDate.of(2010, 11, 1)));
		final List<HistoryRetrievalRequest> expected = asList(
		        create(LocalDate.of(2010, 3, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 9, 1)),
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 11, 1)));
		setUpBuilder(expected);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(expected, merged);
	}

	@Test
	public void twoConsercutiveWithLoosePrefixedRequest() {

		final List<HistoryRetrievalRequest> toMerge = asList(create(LocalDate.of(2010, 2, 1), LocalDate.of(2010, 3, 1)),
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1)));
		final List<HistoryRetrievalRequest> expected = asList(
		        create(LocalDate.of(2010, 2, 1), LocalDate.of(2010, 3, 1)),
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 7, 1)));
		setUpBuilder(expected);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(expected, merged);
	}

	@Test
	public void twoConsercutiveWithLooseSuffixedRequest() {

		final List<HistoryRetrievalRequest> toMerge = asList(
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1)),
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 6, 1)),
		        create(LocalDate.of(2010, 6, 1), LocalDate.of(2010, 7, 1)));
		final List<HistoryRetrievalRequest> expected = asList(
		        create(LocalDate.of(2010, 5, 1), LocalDate.of(2010, 7, 1)),
		        create(LocalDate.of(2010, 9, 1), LocalDate.of(2010, 10, 1)));
		setUpBuilder(expected);

		final List<HistoryRetrievalRequest> merged = merge(toMerge);

		verifyRetrievalRequests(expected, merged);
	}

	private List<HistoryRetrievalRequest> merge( List<HistoryRetrievalRequest> requests, final Period maximum ) {

		return merger.merge(requests, maximum);
	}

	/**
	 * Default to MAXIMUM.
	 */
	private List<HistoryRetrievalRequest> merge( List<HistoryRetrievalRequest> requests ) {

		return merger.merge(requests, MAXIMUM);
	}

	private void setUpBuilder( final List<HistoryRetrievalRequest> requests ) {

		when(requestBuilder.withTickerSymbol(anyString())).thenReturn(requestBuilder);
		when(requestBuilder.withExclusiveEndDate(any(Date.class))).thenReturn(requestBuilder);
		when(requestBuilder.withExclusiveEndDate(any(LocalDate.class))).thenReturn(requestBuilder);
		when(requestBuilder.withInclusiveStartDate(any(Date.class))).thenReturn(requestBuilder);
		when(requestBuilder.withInclusiveStartDate(any(LocalDate.class))).thenReturn(requestBuilder);
		when(requestBuilder.withDataset(anyString())).thenReturn(requestBuilder);

		OngoingStubbing<HistoryRetrievalRequest> stubbing = when(requestBuilder.build());
		for (final HistoryRetrievalRequest request : requests) {
			stubbing = stubbing.thenReturn(request);
		}
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
}