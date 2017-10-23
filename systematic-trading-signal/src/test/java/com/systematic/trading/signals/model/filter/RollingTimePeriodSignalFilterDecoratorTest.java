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
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * TimePeriodSignalFilterDecorator test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RollingTimePeriodSignalFilterDecoratorTest {

	private static final LocalDate LATEST_TRADING_DATE = LocalDate.of(2015, 10, 16);

	@Mock
	private SignalFilter filter;

	@Mock
	private Map<IndicatorId, List<IndicatorSignal>> signals;

	/** Expected filtered signals. */
	private SortedSet<BuySignal> expectedSignals;

	/** Decorator instance being tested. */
	private RollingTimePeriodSignalFilterDecorator decorator;

	@Test
	public void lowerRange() {
		setUpFilter(3);
		setUpDecorator();

		final SortedSet<BuySignal> filteredSignals = applyFilter();

		verifyFilteredSignals(1, filteredSignals);
	}

	@Test
	public void upperRange() {
		setUpFilter(0);
		setUpDecorator();

		final SortedSet<BuySignal> filteredSignals = applyFilter();

		verifyFilteredSignals(1, filteredSignals);
	}

	@Test
	public void outsideRange() {
		setUpFilter(4);
		setUpDecorator();

		final SortedSet<BuySignal> filteredSignals = applyFilter();

		verifyFilteredSignals(0, filteredSignals);
	}

	@Test
	public void midRange() {
		setUpFilter(2);
		setUpDecorator();

		final SortedSet<BuySignal> filteredSignals = applyFilter();

		verifyFilteredSignals(1, filteredSignals);
	}

	@SuppressWarnings("unchecked")
	private void setUpFilter( final int daysAfter ) {
		final LocalDate signalDate = LATEST_TRADING_DATE.minus(Period.ofDays(daysAfter));
		expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(LocalDate.class))).thenReturn(expectedSignals);
	}

	private void verifyFilteredSignals( final int expected, final SortedSet<BuySignal> filteredSignals ) {
		assertNotNull(filteredSignals);
		assertEquals(expected == 0, filteredSignals.isEmpty());
		assertEquals(expected, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, LATEST_TRADING_DATE);
	}

	private SortedSet<BuySignal> applyFilter() {
		return decorator.apply(signals, LATEST_TRADING_DATE);
	}

	private SortedSet<BuySignal> createOutputSignals( final LocalDate signalDate ) {
		final SortedSet<BuySignal> signals = new TreeSet<BuySignal>(new BuySignalDateComparator());
		signals.add(new BuySignal(signalDate));
		return signals;
	}

	private void setUpDecorator() {
		decorator = new RollingTimePeriodSignalFilterDecorator(filter, Period.ofDays(3));
	}
}