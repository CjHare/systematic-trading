/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * TimePeriodSignalFilterDecorator test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TimePeriodSignalFilterDecoratorTest {

	/** Default ordering of signals. */
	private static final BuySignalDateComparator ORDER_BY_DATE = new BuySignalDateComparator();

	@Mock
	private SignalFilter filter;

	@SuppressWarnings("unchecked")
	@Test
	public void lowerRangeAccepted() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2010, 1, 1);

		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final TimePeriodSignalFilterDecorator decorator = new TimePeriodSignalFilterDecorator(filter, startDate,
		        endDate);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, signalDate);

		assertNotNull(filteredSignals);
		assertEquals(false, filteredSignals.isEmpty());
		assertEquals(1, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, signalDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void lowerRangeRejected() {
		final LocalDate startDate = LocalDate.of(2010, 1, 2);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2010, 1, 1);

		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final TimePeriodSignalFilterDecorator decorator = new TimePeriodSignalFilterDecorator(filter, startDate,
		        endDate);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, signalDate);

		assertNotNull(filteredSignals);
		assertEquals(true, filteredSignals.isEmpty());
		verify(filter).apply(signals, ORDER_BY_DATE, signalDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void upperRangeAccepted() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 2);
		final LocalDate signalDate = LocalDate.of(2015, 1, 1);

		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final TimePeriodSignalFilterDecorator decorator = new TimePeriodSignalFilterDecorator(filter, startDate,
		        endDate);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, signalDate);

		assertNotNull(filteredSignals);
		assertEquals(false, filteredSignals.isEmpty());
		assertEquals(1, filteredSignals.size());
		assertEquals(expectedSignals, filteredSignals);
		verify(filter).apply(signals, ORDER_BY_DATE, signalDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void upperRangeRejected() {
		final LocalDate startDate = LocalDate.of(2010, 1, 1);
		final LocalDate endDate = LocalDate.of(2015, 1, 1);
		final LocalDate signalDate = LocalDate.of(2015, 1, 2);

		final SortedSet<BuySignal> expectedSignals = createOutputSignals(signalDate);
		when(filter.apply(anyMap(), any(Comparator.class), any(LocalDate.class))).thenReturn(expectedSignals);

		final TimePeriodSignalFilterDecorator decorator = new TimePeriodSignalFilterDecorator(filter, startDate,
		        endDate);

		final Map<IndicatorSignalType, List<IndicatorSignal>> signals = createTriggerSignals(signalDate);

		final SortedSet<BuySignal> filteredSignals = decorator.apply(signals, ORDER_BY_DATE, signalDate);

		assertNotNull(filteredSignals);
		assertEquals(true, filteredSignals.isEmpty());
		verify(filter).apply(signals, ORDER_BY_DATE, signalDate);
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
