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
package com.systematic.trading.maths.indicator.ema;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Test the ExponentialMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ExponentialMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private ExponentialMovingAverageCalculator calculator;

	@Test
	public void emaOnePoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(lookback);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1);
		verifyValidation(data, lookback);
	}

	@Test
	public void emaTwoPoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(3);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1, 1);
		verifyValidation(data, lookback);
	}

	@Test
	public void emaThreePoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createIncreasingPrices(4);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1.5, 2.5, 3.67);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaTwoPointsLastNull() {
		final TradingDayPrices[] data = createIncreasingPrices(4);
		data[data.length - 1] = null;
		setUpValidationErrorZeroEntries();
		setUpCalculator(2);

		ema(data);
	}

	@Test
	public void emaTwoPointsDecimal() {
		final int lookback = 2;
		final SortedMap<LocalDate, BigDecimal> data = createIncreasingDecimalPrices(4);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 0.5, 1.5, 2.67);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPointsDecimal() {
		final SortedMap<LocalDate, BigDecimal> data = createIncreasingDecimalPrices(1);
		setUpValidationErrorEnoughValues();
		setUpCalculator(2);

		ema(data);
	}

	@Test
	public void getMinimumNumberOfPrices() {
		setUpCalculator(4);

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals(9, requiredDays);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInputArray() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);
		final TradingDayPrices[] input = null;

		ema(input);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);
		final SortedMap<LocalDate, BigDecimal> input = null;

		ema(input);
	}

	private ExponentialMovingAverageLine ema( final TradingDayPrices[] data ) {
		return calculator.calculate(data);
	}

	private ExponentialMovingAverageLine ema( final SortedMap<LocalDate, BigDecimal> data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void setUpValidationErrorEnoughValues() {
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(anyListOf(BigDecimal.class),
		        anyInt());
	}

	private void setUpValidationErrorZeroEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void verifyEma( final ExponentialMovingAverageLine actual, final double... expected ) {
		assertNotNull(actual);
		assertNotNull(actual.getEma());
		assertEquals(expected.length, actual.getEma().size());
		assertValues(expected, actual.getEma());
	}

	private void setUpCalculator( final int lookback ) {
		calculator = new ExponentialMovingAverageCalculator(lookback, 1, validator);
	}

	private void verifyValidation( final SortedMap<LocalDate, BigDecimal> data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data.values(), lookback);
		verify(validator).verifyZeroNullEntries(data.values());
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i))
			        .withOpeningPrice(i + 1).withLowestPrice(1).withHighestPrice(i + 2).withClosingPrice(i + 1).build();
		}

		return prices;
	}

	private SortedMap<LocalDate, BigDecimal> createIncreasingDecimalPrices( final int count ) {
		final SortedMap<LocalDate, BigDecimal> prices = new TreeMap<>();

		for (int i = 0; i < count; i++) {
			prices.put(LocalDate.now().plusDays(i), BigDecimal.valueOf(i));
		}

		return prices;
	}
}