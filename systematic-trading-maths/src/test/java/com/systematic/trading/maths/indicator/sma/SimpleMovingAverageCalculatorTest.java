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
package com.systematic.trading.maths.indicator.sma;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifying the behaviour for a SimpleMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private SimpleMovingAverageCalculator calculator;

	private void setUpCalculator( final int lookback, final int daysOfSmaValues ) {
		calculator = new SimpleMovingAverageCalculator(lookback, daysOfSmaValues, validator);
	}

	@Test
	public void smaTwoPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final int daysOfSmaValues = numberDataPoints - lookback;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		setUpCalculator(lookback, daysOfSmaValues);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(sma, new double[] { 1, 1, 1, 1 });
		verifyValidation(data, numberDataPoints, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFirstDataPoint() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback - 1;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[0] = null;
		setUpValidationErrorZeroNullEntries();
		setUpCalculator(lookback, daysOfSmaValues);

		sma(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooFewDaysOfSmaValues() {
		setUpValidationErrorTooFewsDaysOfSmaValues();
		setUpCalculator(6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullLastDataPoint() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback - 2;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[lookback + 2] = null;
		setUpValidationErrorZeroNullEntries();
		setUpCalculator(lookback, daysOfSmaValues);

		sma(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback + 1;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		setUValidationErrorNotEnoughValues();
		setUpCalculator(lookback, daysOfSmaValues);

		sma(data);
	}

	@Test
	public void smaThreePoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);
		setUpCalculator(lookback, daysOfSmaValues);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(sma, new double[] { 1.5, 2.5, 3.5, 4.5, 5.5 });
		verifyValidation(data, numberDataPoints, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1, 1);

		sma(null);
	}

	private void verifySma( final SimpleMovingAverageLine sma, final double... expected ) {
		assertNotNull(sma);
		assertNotNull(sma.getSma());
		assertValues(expected, sma.getSma());
	}

	private void verifyValidation( final TradingDayPrices[] data, final int numberDataPoints, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	private void setUpValidationErrorZeroNullEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void setUValidationErrorNotEnoughValues() {
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());
	}

	private void setUpValidationErrorTooFewsDaysOfSmaValues() {
		doThrow(new IllegalArgumentException()).when(validator).verifyGreaterThan(anyInt(), anyInt());
	}

	private SimpleMovingAverageLine sma( final TradingDayPrices[] data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.ofEpochDay(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.ofEpochDay(i)).withOpeningPrice(i + 1)
			        .withLowestPrice(1).withHighestPrice(i + 2).withClosingPrice(i + 1).build();
		}

		return prices;
	}
}