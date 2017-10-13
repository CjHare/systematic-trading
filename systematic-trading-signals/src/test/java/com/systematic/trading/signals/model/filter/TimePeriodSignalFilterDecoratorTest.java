/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.signals.model.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * TimePeriodSignalFilterDecorator test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TimePeriodSignalFilterDecoratorTest {

	@Mock
	private SignalFilter filter;

	@Mock
	private Map<IndicatorSignalId, List<IndicatorSignal>> signals;

	/** Filtered signals expected from the decorator. */
	private SortedSet<BuySignal> expectedSignals;

	/** Decorator instance being tested. */
	private TimePeriodSignalFilterDecorator decorator;

	@Test
	public void lowerRangeAccepted() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2010, 1, 1);
		setUpFilter(signalDate);
		setUpDecorator(startDate, endDate);

		final SortedSet<BuySignal> filteredSignals = applyFilter(signalDate);

		verifyFilteredSignals(1, filteredSignals, signalDate);
	}

	@Test
	public void lowerRangeRejected() {
		final LocalDate startDate = LocalDate.of(2010, 1, 2);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2010, 1, 1);
		setUpFilter(signalDate);
		setUpDecorator(startDate, endDate);

		final SortedSet<BuySignal> filteredSignals = applyFilter(signalDate);

		verifyFilteredSignals(0, filteredSignals, signalDate);
	}

	@Test
	public void upperRangeAccepted() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 2);
		final LocalDate signalDate = LocalDate.of(2015, 1, 1);
		setUpFilter(signalDate);
		setUpDecorator(startDate, endDate);

		final SortedSet<BuySignal> filteredSignals = applyFilter(signalDate);

		verifyFilteredSignals(1, filteredSignals, signalDate);
	}

	@Test
	public void upperRangeRejected() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2015, 1, 2);
		setUpFilter(signalDate);
		setUpDecorator(startDate, endDate);

		final SortedSet<BuySignal> filteredSignals = applyFilter(signalDate);

		verifyFilteredSignals(0, filteredSignals, signalDate);
	}

	@SuppressWarnings("unchecked")
	private void setUpFilter( final LocalDate signalDate ) {
		expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(LocalDate.class))).thenReturn(expectedSignals);
	}

	private void verifyFilteredSignals( final int expected, final SortedSet<BuySignal> filteredSignals,
	        final LocalDate signalDate ) {
		assertNotNull(filteredSignals);
		assertEquals(expected == 0, filteredSignals.isEmpty());
		assertEquals(expected, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, signalDate);
	}

	private SortedSet<BuySignal> applyFilter( final LocalDate signalDate ) {
		return decorator.apply(signals, signalDate);
	}

	private SortedSet<BuySignal> createOutputSignals( final LocalDate signalDate ) {
		final SortedSet<BuySignal> signals = new TreeSet<BuySignal>(new BuySignalDateComparator());

		signals.add(new BuySignal(signalDate));

		return signals;
	}

	private void setUpDecorator( final LocalDate startDate, final LocalDate endDate ) {
		decorator = new TimePeriodSignalFilterDecorator(filter, startDate, endDate);
	}
}