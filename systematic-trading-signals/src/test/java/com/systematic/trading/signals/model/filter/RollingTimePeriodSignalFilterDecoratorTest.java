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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.signal.IndicatorDirectionType;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;

/**
 * TimePeriodSignalFilterDecorator test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RollingTimePeriodSignalFilterDecoratorTest {

	/** Default ordering of signals. */
	private static final BuySignalDateComparator ORDER_BY_DATE = new BuySignalDateComparator();

	@Mock
	private SignalFilter filter;

	@SuppressWarnings("unchecked")
	@Test
	public void lowerRange() {
		final Period withinInclusive = Period.ofDays(3);
		final LocalDate latestTradingDate = LocalDate.of(2015, 10, 16);
		final LocalDate signalDate = latestTradingDate.minus(Period.ofDays(3));
		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final RollingTimePeriodSignalFilterDecorator decorator = new RollingTimePeriodSignalFilterDecorator(filter,
		        withinInclusive);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, latestTradingDate);

		assertNotNull(filteredSignals);
		assertEquals(false, filteredSignals.isEmpty());
		assertEquals(1, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, latestTradingDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void upperRange() {
		final Period withinInclusive = Period.ofDays(3);
		final LocalDate latestTradingDate = LocalDate.of(2015, 10, 16);
		final LocalDate signalDate = latestTradingDate.minus(Period.ofDays(0));
		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final RollingTimePeriodSignalFilterDecorator decorator = new RollingTimePeriodSignalFilterDecorator(filter,
		        withinInclusive);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, latestTradingDate);

		assertNotNull(filteredSignals);
		assertEquals(false, filteredSignals.isEmpty());
		assertEquals(1, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, latestTradingDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void outsideRange() {
		final Period withinInclusive = Period.ofDays(3);
		final LocalDate latestTradingDate = LocalDate.of(2015, 10, 16);
		final LocalDate signalDate = latestTradingDate.minus(Period.ofDays(4));
		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final RollingTimePeriodSignalFilterDecorator decorator = new RollingTimePeriodSignalFilterDecorator(filter,
		        withinInclusive);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, latestTradingDate);

		assertNotNull(filteredSignals);
		assertEquals(true, filteredSignals.isEmpty());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, latestTradingDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void midRange() {
		final Period withinInclusive = Period.ofDays(3);
		final LocalDate latestTradingDate = LocalDate.of(2015, 10, 16);
		final LocalDate signalDate = latestTradingDate.minus(Period.ofDays(2));
		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final RollingTimePeriodSignalFilterDecorator decorator = new RollingTimePeriodSignalFilterDecorator(filter,
		        withinInclusive);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, latestTradingDate);

		assertNotNull(filteredSignals);
		assertEquals(false, filteredSignals.isEmpty());
		assertEquals(1, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, latestTradingDate);
	}

	private SortedSet<BuySignal> createOutputSignals( final LocalDate signalDate ) {
		final SortedSet<BuySignal> signals = new TreeSet<BuySignal>(ORDER_BY_DATE);

		signals.add(new BuySignal(signalDate));

		return signals;
	}

	private Map<IndicatorSignalType, List<IndicatorSignal>> createTriggerSignals( final LocalDate date ) {
		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalType, List<IndicatorSignal>>();

		final List<IndicatorSignal> rsiSignals = new ArrayList<IndicatorSignal>();
		rsiSignals.add(new IndicatorSignal(date, IndicatorSignalType.RSI, IndicatorDirectionType.BULLISH));
		signals.put(IndicatorSignalType.RSI, rsiSignals);

		final List<IndicatorSignal> macdSignals = new ArrayList<IndicatorSignal>();
		macdSignals.add(new IndicatorSignal(date, IndicatorSignalType.MACD, IndicatorDirectionType.BULLISH));
		signals.put(IndicatorSignalType.MACD, macdSignals);

		return signals;
	}
}
