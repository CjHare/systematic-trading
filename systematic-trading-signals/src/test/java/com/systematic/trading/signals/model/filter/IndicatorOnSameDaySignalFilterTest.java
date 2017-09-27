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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;

/**
 * IndicatorsOnSameDaySignalFilter Test
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class IndicatorOnSameDaySignalFilterTest {

	@Mock
	private IndicatorSignalId macdId;

	@Mock
	private IndicatorSignalId rsiId;

	@Mock
	private IndicatorSignalId smaId;

	private final BuySignalDateComparator ordering = new BuySignalDateComparator();
	private final LocalDate latestTradingDate = null;

	@Test
	public void rsiMacdSmaSameDay() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		final LocalDate today = LocalDate.now();

		final List<IndicatorSignal> macd = new ArrayList<IndicatorSignal>();
		macd.add(new IndicatorSignal(today, macdId, SignalType.BULLISH));

		final List<IndicatorSignal> rsi = new ArrayList<IndicatorSignal>();
		rsi.add(new IndicatorSignal(today, rsiId, SignalType.BULLISH));

		final List<IndicatorSignal> sma = new ArrayList<IndicatorSignal>();
		sma.add(new IndicatorSignal(today, smaId, SignalType.BULLISH));

		signals.put(macdId, macd);
		signals.put(rsiId, rsi);
		signals.put(smaId, sma);

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId, smaId);

		final SortedSet<BuySignal> results = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting a single filtered signal", 1, results.size());

		final BuySignal result = results.first();
		assertNotNull("Expecting a non-null indicator", result);
		assertEquals(today, result.getDate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void twoOutOfThree() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		final LocalDate today = LocalDate.now();

		final List<IndicatorSignal> macd = new ArrayList<IndicatorSignal>();
		macd.add(new IndicatorSignal(today, macdId, SignalType.BULLISH));

		final List<IndicatorSignal> rsi = new ArrayList<IndicatorSignal>();
		rsi.add(new IndicatorSignal(today, rsiId, SignalType.BULLISH));

		signals.put(macdId, macd);
		signals.put(rsiId, rsi);

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId, smaId);

		filter.apply(signals, ordering, latestTradingDate);
	}

	@Test
	public void rsiMacdSameDay() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		final LocalDate today = LocalDate.now();

		final List<IndicatorSignal> macd = new ArrayList<IndicatorSignal>();
		macd.add(new IndicatorSignal(today, macdId, SignalType.BULLISH));

		final List<IndicatorSignal> rsi = new ArrayList<IndicatorSignal>();
		rsi.add(new IndicatorSignal(today, rsiId, SignalType.BULLISH));

		signals.put(macdId, macd);
		signals.put(rsiId, rsi);

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting a single filtered signal", 1, results.size());

		final BuySignal result = results.first();
		assertNotNull("Expecting a non-null indicator", result);
		assertEquals(today, result.getDate());
	}

	@Test
	public void rsiOnly() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		final LocalDate today = LocalDate.now();

		final List<IndicatorSignal> macd = new ArrayList<IndicatorSignal>();
		final List<IndicatorSignal> rsi = new ArrayList<IndicatorSignal>();
		rsi.add(new IndicatorSignal(today, rsiId, SignalType.BULLISH));

		signals.put(macdId, macd);
		signals.put(rsiId, rsi);

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting no filtered signal", 0, results.size());
	}

	@Test
	public void macdOnly() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		final LocalDate today = LocalDate.now();

		final List<IndicatorSignal> macd = new ArrayList<IndicatorSignal>();
		final List<IndicatorSignal> rsi = new ArrayList<IndicatorSignal>();
		rsi.add(new IndicatorSignal(today, macdId, SignalType.BULLISH));

		signals.put(macdId, macd);
		signals.put(rsiId, rsi);

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting no filtered signal", 0, results.size());
	}

	@Test
	public void noSignals() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		signals.put(macdId, new ArrayList<IndicatorSignal>());
		signals.put(rsiId, new ArrayList<IndicatorSignal>());

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting no filtered signals", 0, results.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void noSignalsNullMacdList() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		signals.put(rsiId, new ArrayList<IndicatorSignal>());

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		filter.apply(signals, ordering, latestTradingDate);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noSignalsNullRsiList() {
		final Map<IndicatorSignalId, List<IndicatorSignal>> signals = new HashMap<IndicatorSignalId, List<IndicatorSignal>>();
		signals.put(rsiId, new ArrayList<IndicatorSignal>());

		final IndicatorsOnSameDaySignalFilter filter = new IndicatorsOnSameDaySignalFilter(macdId, rsiId);

		filter.apply(signals, ordering, latestTradingDate);
	}

}
