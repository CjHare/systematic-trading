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
package com.systematic.trading.maths.indicator.macd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.model.SignalType;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifies the behaviour of the MovingAverageConvergenceDivergenceCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class MovingAverageConvergenceDivergenceCalculatorTest {

	@Mock
	private ExponentialMovingAverage fastEma;

	@Mock
	private ExponentialMovingAverage slowEma;

	@Mock
	private ExponentialMovingAverage signalEma;

	@Mock
	private Validator validator;

	@Test(expected = IllegalArgumentException.class)
	public void noResults() {
		final int lookback = 0;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		calculator.macd(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void singleNullResult() {
		final int lookback = 1;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		calculator.macd(data);
	}

	@Test
	public void signalLineInput() {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices(lookback);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(createFlatValuesList(lookback, 1));
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(createIncreasingValuesList(lookback, 2));
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(new ArrayList<BigDecimal>(lookback));

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> signals = calculator.macd(data);

		assertNotNull(signals);
		assertEquals(0, signals.size());

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(fastEma).ema(data);
		verify(slowEma).ema(data);
		verify(signalEma, never()).ema(any(TradingDayPrices[].class));
		verify(signalEma).ema(isBigDecimalListOf(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
		        BigDecimal.valueOf(4), BigDecimal.valueOf(5)));
	}

	@Test
	public void noCrossover() {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices(lookback);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(createFlatValuesList(lookback, 1));
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(createIncreasingValuesList(lookback, 2));

		final List<BigDecimal> signalEmaValues = createFlatValuesList(5, 1);
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(signalEmaValues);

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> signals = calculator.macd(data);

		assertNotNull(signals);
		assertEquals(0, signals.size());

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(fastEma).ema(data);
		verify(slowEma).ema(data);
		verify(signalEma, never()).ema(any(TradingDayPrices[].class));
	}

	@Test
	public void bullishSignalLineCrossover() {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices(lookback);

		final List<BigDecimal> slowEmaValues = createFlatValuesList(lookback, 1);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(slowEmaValues);

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList(lookback, 2);
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(fastEmaValues);

		final List<BigDecimal> signalEmaValues = createFlatValuesList(5, 2);
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(signalEmaValues);

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> signals = calculator.macd(data);

		assertNotNull(signals);
		assertEquals(1, signals.size());
		assertEquals(SignalType.BULLISH, signals.get(0).getType());
		assertEquals(LocalDate.now().plusDays(1), signals.get(0).getDate());

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(fastEma).ema(data);
		verify(slowEma).ema(data);
		verify(signalEma, never()).ema(any(TradingDayPrices[].class));
	}

	@Test
	public void bullishSignalLineCrossoverTwoDataSets() {

		final int firstLookback = 4;
		final TradingDayPrices[] firstData = createPrices(firstLookback);

		final int secondLookback = 10;
		final TradingDayPrices[] secondData = createPrices(secondLookback);

		final List<BigDecimal> firstSlowEmaValues = createFlatValuesList(firstLookback, 1);
		final List<BigDecimal> secondSlowEmaValues = createFlatValuesList(secondLookback, 1);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(firstSlowEmaValues).thenReturn(secondSlowEmaValues);

		final List<BigDecimal> firstFastEmaValues = createIncreasingValuesList(firstLookback, 2);
		final List<BigDecimal> secondFastEmaValues = createIncreasingValuesList(secondLookback, 2);
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(firstFastEmaValues).thenReturn(secondFastEmaValues);

		final List<BigDecimal> firstSignalEmaValues = createFlatValuesList(firstLookback, 4.1);
		final List<BigDecimal> secondSignalEmaValues = createFlatValuesList(secondLookback, 4.1);
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(firstSignalEmaValues)
		        .thenReturn(secondSignalEmaValues);

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> firstSignals = calculator.macd(firstData);

		assertNotNull(firstSignals);
		assertEquals(0, firstSignals.size());

		final List<DatedSignal> secondSignals = calculator.macd(secondData);

		assertNotNull(secondSignals);
		assertEquals(1, secondSignals.size());
		assertEquals(SignalType.BULLISH, secondSignals.get(0).getType());
		assertEquals(LocalDate.now().plusDays(4), secondSignals.get(0).getDate());
	}

	@Test
	public void bullishOriginCrossover() {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices(lookback);

		final List<BigDecimal> slowEmaValues = createFlatValuesList(lookback, 0);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(slowEmaValues);

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList(lookback, -1);
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(fastEmaValues);

		final List<BigDecimal> signalEmaValues = createFlatValuesList(5, 8);
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(signalEmaValues);

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> signals = calculator.macd(data);

		assertNotNull(signals);
		assertEquals(1, signals.size());
		assertEquals(SignalType.BULLISH, signals.get(0).getType());
		assertEquals(LocalDate.now().plusDays(1), signals.get(0).getDate());

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(fastEma).ema(data);
		verify(slowEma).ema(data);
		verify(signalEma, never()).ema(any(TradingDayPrices[].class));
	}

	@Test
	public void bullishSignalLineAndOriginCrossover() {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices(lookback);

		final List<BigDecimal> slowEmaValues = createFlatValuesList(lookback, 0);
		when(slowEma.ema(any(TradingDayPrices[].class))).thenReturn(slowEmaValues);

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList(lookback, -1);
		when(fastEma.ema(any(TradingDayPrices[].class))).thenReturn(fastEmaValues);

		final List<BigDecimal> signalEmaValues = createFlatValuesList(5, 1);
		when(signalEma.ema(anyListOf(BigDecimal.class))).thenReturn(signalEmaValues);

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, validator);

		final List<DatedSignal> signals = calculator.macd(data);

		assertNotNull(signals);
		assertEquals(2, signals.size());
		assertEquals(SignalType.BULLISH, signals.get(0).getType());
		assertEquals(LocalDate.now().plusDays(1), signals.get(0).getDate());

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(fastEma).ema(data);
		verify(slowEma).ema(data);
		verify(signalEma, never()).ema(any(TradingDayPrices[].class));
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private List<BigDecimal> createFlatValuesList( final int size, final double value ) {
		final List<BigDecimal> values = new ArrayList<BigDecimal>(size);

		for (int i = 0; i < size; i++) {
			values.add(BigDecimal.valueOf(value));
		}

		return values;
	}

	private List<BigDecimal> createIncreasingValuesList( final int size, final int startingValue ) {
		final List<BigDecimal> values = new ArrayList<BigDecimal>(size);

		for (int i = 0; i < size; i++) {
			values.add(BigDecimal.valueOf(startingValue + i));
		}

		return values;
	}

	private List<BigDecimal> isBigDecimalListOf( final BigDecimal... bigDecimals ) {
		return argThat(new IsBigDecimalList(bigDecimals));
	}

	class IsBigDecimalList extends ArgumentMatcher<List<BigDecimal>> {

		final BigDecimal[] expected;

		public IsBigDecimalList( final BigDecimal... bigDecimals ) {
			this.expected = bigDecimals;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof List<?>) {

				@SuppressWarnings("unchecked")
				final List<BigDecimal> given = (List<BigDecimal>) argument;

				for (int i = 0; i < expected.length; i++) {
					if (given.get(i) != null && given.get(i).compareTo(expected[i]) != 0)
						return false;
				}

				return true;
			}

			return false;
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText("[");
			for (int i = 0; i < expected.length; i++) {
				description.appendText(String.valueOf(expected[i]));

				if (i + 1 < expected.length) {
					description.appendText(",");
				}
			}
			description.appendText("]");
		}
	}
}
