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
package com.systematic.trading.strategy.indicator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.SignalCalculator;
import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signal.generator.SignalGenerator;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.signal.range.SignalRangeFilter;

/**
 * Verifying the trading strategy indicator behaviour.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyIndicatorTest {

	@Mock
	private IndicatorId id;

	@Mock
	private SignalCalculator<Object> calculator;

	@Mock
	private SignalGenerator<Object> generator;

	@Mock
	private SignalRangeFilter signalRangeFilter;

	@Mock
	private SignalAnalysisListener signalListner;

	/** Instance being tested. */
	private Indicator indicator;

	@Before
	public void setUp() {
		indicator = new TradingStrategyIndicator<Object, SignalCalculator<Object>>(id, calculator, generator,
		        signalRangeFilter, signalListner);
	}

	@Test
	public void requiredPriceTicks() {
		setUpRequiredPriceTicks(4);

		final int ticks = indicator.numberOfTradingDaysRequired();

		verifyPriceTicks(4, ticks);
		verifyPriceTickDelegation();
	}

	@Test
	public void analyseNoSignal() {
		final TradingDayPrices[] data = new TradingDayPrices[5];
		final List<DatedSignal> expected = new ArrayList<>();
		when(generator.generate(any(Object.class), any(Predicate.class))).thenReturn(expected);

		final Object calculation = mock(Object.class);
		when(calculator.calculate(any(TradingDayPrices[].class))).thenReturn(calculation);

		final List<DatedSignal> signals = indicator.analyse(data);

		assertEquals(expected, signals);

		verify(calculator).calculate(data);
		verify(generator).generate(eq(calculation), any(Predicate.class));
	}

	private void setUpRequiredPriceTicks( final int ticks ) {
		when(calculator.minimumNumberOfPrices()).thenReturn(ticks);
	}

	private void verifyPriceTicks( final int expected, final int actual ) {
		assertEquals(expected, actual);
	}

	private void verifyPriceTickDelegation() {
		verify(calculator).minimumNumberOfPrices();
	}
}