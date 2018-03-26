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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.util.RandomStringGenerator;

/**
 * Monthly slicing UT.
 * 
 * @author CJ Hare
 */
public class MonthlyHistoryRetrievalRequestSlicerTest {

	/** Instance being tested. */
	private MonthlyHistoryRetrievalRequestSlicer slicer;

	/** Random string re-generated every run. */
	private String tickerSymbol;

	/** Random string re-generated every run. */
	private String dataset;

	@Before
	public void setUp() {

		slicer = new MonthlyHistoryRetrievalRequestSlicer();
		tickerSymbol = RandomStringGenerator.generate();
		dataset = RandomStringGenerator.generate();
	}

	@Test
	public void oneHalfMonth() {

		final LocalDate inclusiveStartDate = LocalDate.of(2011, 5, 1);
		final LocalDate exclusiveEndDate = LocalDate.of(2011, 5, 18);

		final List<HistoryRetrievalRequest> requests = slice(inclusiveStartDate, exclusiveEndDate);

		assertHistoryRetrievalRequest(requests, expectedDates(inclusiveStartDate, exclusiveEndDate));
	}

	@Test
	public void oneWholeMonth() {

		final LocalDate inclusiveStartDate = LocalDate.of(2011, 5, 1);
		final LocalDate exclusiveEndDate = LocalDate.of(2011, 6, 1);

		final List<HistoryRetrievalRequest> requests = slice(inclusiveStartDate, exclusiveEndDate);

		assertHistoryRetrievalRequest(requests, expectedDates(inclusiveStartDate, exclusiveEndDate));
	}

	@Test
	public void oneHalfOneWholeMonth() {

		final LocalDate inclusiveStartDate = LocalDate.of(2011, 4, 14);
		final LocalDate exclusiveEndDate = LocalDate.of(2011, 6, 1);

		final List<HistoryRetrievalRequest> requests = slice(inclusiveStartDate, exclusiveEndDate);

		assertHistoryRetrievalRequest(
		        requests,
		        expectedDates(inclusiveStartDate, LocalDate.of(2011, 5, 1)),
		        expectedDates(LocalDate.of(2011, 5, 1), exclusiveEndDate));
	}

	@Test
	public void twoHalfThreeWholeMonth() {

		final LocalDate inclusiveStartDate = LocalDate.of(2011, 4, 14);
		final LocalDate exclusiveEndDate = LocalDate.of(2011, 8, 19);

		final List<HistoryRetrievalRequest> requests = slice(inclusiveStartDate, exclusiveEndDate);

		assertHistoryRetrievalRequest(
		        requests,
		        expectedDates(inclusiveStartDate, LocalDate.of(2011, 5, 1)),
		        expectedDates(LocalDate.of(2011, 5, 1), LocalDate.of(2011, 6, 1)),
		        expectedDates(LocalDate.of(2011, 6, 1), LocalDate.of(2011, 7, 1)),
		        expectedDates(LocalDate.of(2011, 7, 1), LocalDate.of(2011, 8, 1)),
		        expectedDates(LocalDate.of(2011, 8, 1), exclusiveEndDate));
	}

	private List<HistoryRetrievalRequest> slice(
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {

		return slicer.slice(dataset, tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private Pair<LocalDate, LocalDate> expectedDates(
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {

		return new ImmutablePair<LocalDate, LocalDate>(inclusiveStartDate, exclusiveEndDate);
	}

	@SafeVarargs
	private final void assertHistoryRetrievalRequest(
	        final List<HistoryRetrievalRequest> actualValues,
	        final Pair<LocalDate, LocalDate>... startEndDates ) {

		assertNotNull(startEndDates);
		assertEquals(startEndDates.length, actualValues.size());

		for (int i = 0; i < startEndDates.length; i++) {
			assertEquals(tickerSymbol, actualValues.get(i).tickerSymbol());
			assertEquals(startEndDates[i].getLeft(), actualValues.get(i).inclusiveStartDate().toLocalDate());
			assertEquals(startEndDates[i].getRight(), actualValues.get(i).exclusiveEndDate().toLocalDate());
		}
	}
}
