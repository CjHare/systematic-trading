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
package com.systematic.trading.signals.indicator.rsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexDataPoint;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexSignalsTest {

	/** Number of days needed to correctly calculate the first RSI value.*/
	private static final int LOOKBACK = 26;

	/** Minimum number of useful days for RSI evaluation, */
	private static final int REQUIRED_TRADING_DAYS = 2 + LOOKBACK;

	@Mock
	private SignalRangeFilter filter;

	@Mock
	private RelativeStrengthIndexCalculator rsi;

	private List<SignalCalculator<List<RelativeStrengthIndexDataPoint>>> signalCalculators;

	private TradingDayPrices[] data;

	@Before
	public void setUp() {
		signalCalculators = new ArrayList<>();

		//TODO signal calcs

		data = new TradingDayPrices[0];
	}

	@Test
	public void getSignalType() {
		RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		assertEquals(IndicatorSignalType.RSI, rsiSignals.getSignalType());
	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		assertEquals(REQUIRED_TRADING_DAYS, rsiSignals.getRequiredNumberOfTradingDays());
	}

	@Test
	public void noSignals() {
		RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

		verifySignals(signals);
		verifyRsiCaclculator();
	}

	private RelativeStrengthIndexSignals setUpRsiSignals() {
		return new RelativeStrengthIndexSignals(LOOKBACK, rsi, signalCalculators, filter, MathContext.DECIMAL64);
	}

	private void verifyRsiCaclculator() {
		verify(rsi).rsi(data);
		verifyNoMoreInteractions(rsi);
	}

	private void verifySignals( final List<IndicatorSignal> indicatorSignals, final DatedSignal... datedSignals ) {
		assertNotNull(indicatorSignals);

		if (datedSignals.length > 0) {
			assertEquals("Expecting the same number of indicator as dated signals", datedSignals.length,
			        indicatorSignals.size());

			for (int i = 0; i < datedSignals.length; i++) {
				assertEquals(datedSignals[i].getDate(), indicatorSignals.get(i).getDate());
			}
		}
	}
}