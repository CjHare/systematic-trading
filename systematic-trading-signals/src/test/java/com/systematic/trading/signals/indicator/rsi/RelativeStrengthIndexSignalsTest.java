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
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Verify the behaviour of the RelativeStrengthIndexSignals.
 * 
 * @author CJ Hare
 */
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

	@Mock
	private SignalCalculator<RelativeStrengthIndexLine> firstCalculator;

	@Mock
	private SignalCalculator<RelativeStrengthIndexLine> secondCalculator;

	private List<SignalCalculator<RelativeStrengthIndexLine>> signalCalculators;

	private TradingDayPrices[] data;

	@Mock
	private RelativeStrengthIndexLine line;

	@Mock
	private IndicatorSignalId rsiId;

	@Before
	public void setUp() {
		signalCalculators = new ArrayList<>();
		signalCalculators.add(firstCalculator);
		signalCalculators.add(secondCalculator);

		data = new TradingDayPrices[0];

		when(rsi.rsi(any(TradingDayPrices[].class))).thenReturn(line);
	}

	@Test
	public void getSignalType() {
		RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		assertEquals(rsiId, rsiSignals.getSignalType());
	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		assertEquals(REQUIRED_TRADING_DAYS, rsiSignals.getRequiredNumberOfTradingDays());
	}

	@Test
	public void noSignals() {
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

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
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

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
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

		verifySignals(signals, firstSignal, secondSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals(2);
		verifySecondCalculatorSignals();
	}

	@Test
	public void noSignalCalculators() {
		removeSignalCalculators();
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

		verifySignals(signals);
		verifyRsiCaclculation();
	}

	@Test
	public void secondSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(secondCalculator, firstSignal, secondSignal);
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

		verifySignals(signals, firstSignal, secondSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals(2);
	}

	@Test
	public void twoSignalCalculatorsNoSignals() {
		final RelativeStrengthIndexSignals rsiSignals = setUpRsiSignals();

		final List<IndicatorSignal> signals = rsiSignals.calculateSignals(data);

		verifySignals(signals);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals();
	}

	@SuppressWarnings("unchecked")
	private void setUpCalculator( SignalCalculator<RelativeStrengthIndexLine> calculator,
	        final DatedSignal... signals ) {
		final List<DatedSignal> datedSignals = new ArrayList<>();
		for (final DatedSignal signal : signals) {
			datedSignals.add(signal);
		}

		when(calculator.calculateSignals(any(RelativeStrengthIndexLine.class), any(Predicate.class)))
		        .thenReturn(datedSignals);
	}

	private RelativeStrengthIndexSignals setUpRsiSignals() {
		return new RelativeStrengthIndexSignals(rsiId, LOOKBACK, rsi, signalCalculators, filter);
	}

	private void verifyRsiCaclculation() {
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
	private void verifyCalculatorSignals( final SignalCalculator<RelativeStrengthIndexLine> calculator,
	        final int typeCount ) {
		verify(calculator).calculateSignals(eq(line), any(Predicate.class));
		verify(calculator, times(typeCount)).getType();
		verifyNoMoreInteractions(calculator);
	}
}