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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * IndicatorsOnSameDaySignalFilter Test
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class IndicatorOnSameDaySignalFilterTest {

	private static final LocalDate LAST_TRADING_DATE = null;
	private static final LocalDate TODAY = LocalDate.now();

	@Mock
	private IndicatorId macdId;

	@Mock
	private IndicatorId rsiId;

	@Mock
	private IndicatorId smaId;

	@Mock
	private Comparator<BuySignal> ordering;

	/** List of generated signals. */
	private Map<IndicatorId, List<IndicatorSignal>> signals;

	/** Filter instance being tested. */
	private IndicatorsOnSameDaySignalFilter filter;

	@Before
	public void setUp() {
		signals = new HashMap<IndicatorId, List<IndicatorSignal>>();
	}

	@Test
	public void rsiMacdSmaSameDay() {
		setUpSignals(macdId, rsiId, smaId);
		setUpFilter(macdId, rsiId, smaId);

		final SortedSet<BuySignal> results = applyFilter();

		verifyFilteredSignals(1, results);
	}

	@Test
	public void rsiMacdSameDay() {
		setUpSignals(macdId, rsiId);
		setUpFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = applyFilter();

		verifyFilteredSignals(1, results);
	}

	@Test
	public void rsiOnly() {
		setUpSignals(rsiId);
		setUpNoSignals(macdId);
		setUpFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = applyFilter();

		verifyFilteredSignals(0, results);
	}

	@Test
	public void macdOnly() {
		setUpSignals(macdId);
		setUpNoSignals(rsiId);
		setUpFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = applyFilter();

		verifyFilteredSignals(0, results);
	}

	@Test
	public void noSignals() {
		setUpNoSignals(macdId, rsiId);
		setUpFilter(macdId, rsiId);

		final SortedSet<BuySignal> results = applyFilter();

		verifyFilteredSignals(0, results);
	}

	@Test(expected = IllegalArgumentException.class)
	public void twoOutOfThree() {
		setUpSignals(macdId, rsiId);
		setUpFilter(macdId, rsiId, smaId);

		applyFilter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void noSignalsNullMacdList() {
		setUpNoSignals(macdId);
		setUpFilter(macdId, rsiId);

		applyFilter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void noSignalsNullRsiList() {
		setUpNoSignals(rsiId);
		setUpFilter(macdId, rsiId);

		applyFilter();
	}

	private void verifyFilteredSignals( final int expected, final SortedSet<BuySignal> results ) {
		assertNotNull("Expecting a non-null result set", results);
		assertEquals("Expecting no filtered signals", expected, results.size());

		if (expected > 0) {
			final BuySignal result = results.first();
			assertNotNull("Expecting a non-null indicator", result);
			assertEquals(TODAY, result.getDate());
		}
	}

	private SortedSet<BuySignal> applyFilter() {
		return filter.apply(signals, LAST_TRADING_DATE);
	}

	private void setUpFilter( final IndicatorId... ids ) {
		filter = new IndicatorsOnSameDaySignalFilter(ordering, ids);
	}

	private void setUpNoSignals( final IndicatorId... ids ) {
		for (final IndicatorId id : ids) {
			signals.put(id, new ArrayList<IndicatorSignal>());
		}
	}

	private void setUpSignals( final IndicatorId... ids ) {
		for (final IndicatorId id : ids) {
			final List<IndicatorSignal> signal = new ArrayList<IndicatorSignal>();
			signal.add(new IndicatorSignal(TODAY, id, SignalType.BULLISH));
			signals.put(id, signal);
		}
	}
}