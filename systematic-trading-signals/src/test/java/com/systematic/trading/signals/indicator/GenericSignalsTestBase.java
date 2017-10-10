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
package com.systematic.trading.signals.indicator;

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.Indicator;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.model.DatedSignal;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * Generic test functionality for the signal test children.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class GenericSignalsTestBase<T, U extends Indicator<T>> {

	@Mock
	private SignalRangeFilter filter;

	@Mock
	private U indicator;

	@Mock
	private SignalGenerator<T> firstGenerator;

	@Mock
	private SignalGenerator<T> secondGenerator;

	@Mock
	private T line;

	@Mock
	private IndicatorSignalId indicatorId;

	private List<SignalGenerator<T>> signalGenerators;

	private TradingDayPrices[] data;

	/** Indicator instance being tested. */
	private GenericIndicatorSignals<T, U> indicatorSignals;

	@Before
	public void setUp() {
		signalGenerators = new ArrayList<>();
		signalGenerators.add(firstGenerator);
		signalGenerators.add(secondGenerator);

		data = new TradingDayPrices[0];

		when(indicator.calculate(any(TradingDayPrices[].class))).thenReturn(line);
		setUpIndicatorSignals();
	}

	@Test
	public void getSignalType() {
		assertEquals(indicatorId, indicatorSignals.getSignalType());
	}

	@Ignore
	@Test
	public void getRequiredNumberOfTradingDays() {
		assertEquals(requiredNumberOfTradingDays(), indicatorSignals.getRequiredNumberOfTradingDays());
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
		setUpCalculator(firstGenerator, secondSignal);
		setUpCalculator(secondGenerator, firstSignal);
		setUpIndicatorSignals();

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
		setUpCalculator(firstGenerator, firstSignal, secondSignal);
		setUpIndicatorSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals, firstSignal, secondSignal);
		verifyRsiCaclculation();
		verifyFirstCalculatorSignals(2);
		verifySecondCalculatorSignals();
	}

	@Test
	public void noSignalCalculators() {
		removeSignalCalculators();
		setUpIndicatorSignals();

		final List<IndicatorSignal> signals = sma();

		verifySignals(signals);
		verifyRsiCaclculation();
	}

	@Test
	public void secondSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(secondGenerator, firstSignal, secondSignal);
		setUpIndicatorSignals();

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

	protected abstract int requiredNumberOfTradingDays();

	@SuppressWarnings("unchecked")
	private void setUpCalculator( SignalGenerator<T> calculator, final DatedSignal... signals ) {
		final List<DatedSignal> datedSignals = new ArrayList<>();
		for (final DatedSignal signal : signals) {
			datedSignals.add(signal);
		}

		when(calculator.generate((T) any(line.getClass()), any(Predicate.class))).thenReturn(datedSignals);
	}

	private List<IndicatorSignal> sma() {
		return indicatorSignals.calculate(data);
	}

	private void setUpIndicatorSignals() {
		indicatorSignals = new GenericIndicatorSignals<T, U>(indicatorId, indicator, requiredNumberOfTradingDays(),
		        signalGenerators, filter);
	}

	private void verifyRsiCaclculation() {
		verify(indicator).calculate(data);
		verifyNoMoreInteractions(indicator);
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
		verifyCalculatorSignals(firstGenerator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	private void verifySecondCalculatorSignals( final int... typeCount ) {
		verifyCalculatorSignals(secondGenerator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	private void removeSignalCalculators() {
		signalGenerators.clear();
	}

	@SuppressWarnings("unchecked")
	private void verifyCalculatorSignals( final SignalGenerator<T> calculator, final int typeCount ) {
		verify(calculator).generate(eq(line), any(Predicate.class));
		verify(calculator, times(typeCount)).getType();
		verifyNoMoreInteractions(calculator);
	}
}