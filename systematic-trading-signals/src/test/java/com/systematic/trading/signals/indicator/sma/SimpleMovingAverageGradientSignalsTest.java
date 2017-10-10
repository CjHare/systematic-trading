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
package com.systematic.trading.signals.indicator.sma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalsBase;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Verify the SimpleMovingAverageGradientSignals interacts correctly with it's aggregated components.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleMovingAverageGradientSignalsTest {

	/** Number of days needed to correctly calculate the first RSI value.*/
	private static final int LOOKBACK = 26;

	/** Number of days needed to correctly calculate the first RSI value.*/
	private static final int DAYS_OF_GRADIENT = 5;

	/** Minimum number of useful days for RSI evaluation, */
	private static final int REQUIRED_TRADING_DAYS = DAYS_OF_GRADIENT + LOOKBACK;

	@Mock
	private SignalRangeFilter filter;

	@Mock
	private SimpleMovingAverage sma;

	@Mock
	private SignalCalculator<SimpleMovingAverageLine> firstCalculator;

	@Mock
	private SignalCalculator<SimpleMovingAverageLine> secondCalculator;

	private List<SignalCalculator<SimpleMovingAverageLine>> signalCalculators;

	private TradingDayPrices[] data;

	@Mock
	private SimpleMovingAverageLine line;

	@Mock
	private IndicatorSignalId smaId;

	/** Indicator instance being tested. */
	private IndicatorSignalsBase<SimpleMovingAverageLine, SimpleMovingAverage> indicator;

	@Before
	public void setUp() {
		signalCalculators = new ArrayList<>();
		signalCalculators.add(firstCalculator);
		signalCalculators.add(secondCalculator);

		data = new TradingDayPrices[0];

		when(sma.calculate(any(TradingDayPrices[].class))).thenReturn(line);
		setUpSmaSignals();
	}

	@Test
	public void getSignalType() {
		assertEquals(smaId, indicator.getSignalType());
	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		assertEquals(REQUIRED_TRADING_DAYS, indicator.getRequiredNumberOfTradingDays());
	}

	@Test
	public void noSignals() {

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals();
	}

	@Test
	public void eachSignalCalculatorOneSignal() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(firstCalculator, secondSignal);
		setUpCalculator(secondCalculator, firstSignal);
		setUpSmaSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals, secondSignal, firstSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals(1);
		verifySecondCalculatorSignals(1);
	}

	@Test
	public void firstSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(firstCalculator, firstSignal, secondSignal);
		setUpSmaSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals, firstSignal, secondSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals(2);
		verifySecondCalculatorSignals();
	}

	@Test
	public void noSignalCalculators() {
		removeSignalCalculators();
		setUpSmaSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals);
		verifyRsiCaclculation();
	}

	@Test
	public void secondSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(secondCalculator, firstSignal, secondSignal);
		setUpSmaSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals, firstSignal, secondSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals(2);
	}

	@Test
	public void twoSignalCalculatorsNoSignals() {

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals();
	}

	@SuppressWarnings("unchecked")
	private void setUpCalculator( SignalCalculator<SimpleMovingAverageLine> calculator, final DatedSignal... signals ) {
		final List<DatedSignal> datedSignals = new ArrayList<>();
		for (final DatedSignal signal : signals) {
			datedSignals.add(signal);
		}

		when(calculator.calculateSignals(any(SimpleMovingAverageLine.class), any(Predicate.class)))
		        .thenReturn(datedSignals);
	}

	private List<IndicatorSignal> sma() {
		return indicator.calculateSignals(data);
	}

	private void setUpSmaSignals() {
		indicator = new IndicatorSignalsBase<SimpleMovingAverageLine, SimpleMovingAverage>(smaId, sma,
		        LOOKBACK + DAYS_OF_GRADIENT, signalCalculators, filter);
	}

	private void verifyRsiCaclculation() {
		verify(sma).calculate(data);
		verifyNoMoreInteractions(sma);
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

	private void verifyFirstCalculatorSignals( final int... typeCount ) {
		verifyCalculatorSignals(firstCalculator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	private void verifySecondCalculatorSignals( final int... typeCount ) {
		verifyCalculatorSignals(secondCalculator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	private void removeSignalCalculators() {
		signalCalculators.clear();
	}

	@SuppressWarnings("unchecked")
	private void verifyCalculatorSignals( final SignalCalculator<SimpleMovingAverageLine> calculator,
	        final int typeCount ) {
		verify(calculator).calculateSignals(eq(line), any(Predicate.class));
		verify(calculator, times(typeCount)).getType();
		verifyNoMoreInteractions(calculator);
	}
}