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
package com.systematic.trading.maths.indicator.stochastic;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValuesTwoDecimalPlaces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifies the StochasticPercentageKCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class StochasticPercentageKCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private Validator validator;

	@Test
	public void percentageKThreePoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		final List<BigDecimal> pk = calculator.percentageK(data);

		assertNotNull(pk);
		assertEquals(numberDataPoints - lookback, pk.size());
		assertValues(new double[] { 50, 50, 50 }, pk);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	@Test
	public void percentageKThreeFlatPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createFlatPrices(numberDataPoints);
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		final List<BigDecimal> pk = calculator.percentageK(data);

		assertNotNull(pk);
		assertEquals(numberDataPoints - lookback, pk.size());
		assertValuesTwoDecimalPlaces(new double[] { 0, 0 }, pk);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFirstDataPoint() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[0] = null;
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		calculator.percentageK(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullLastDataPoint() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[data.length - 1] = null;
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		calculator.percentageK(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		final int daysOfPercentageKValues = numberDataPoints - lookback + 1;

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		calculator.percentageK(data);
	}

	@Test
	public void percentageKFourPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		final List<BigDecimal> pk = calculator.percentageK(data);

		assertNotNull(pk);
		assertEquals(numberDataPoints - lookback, pk.size());
		assertValuesTwoDecimalPlaces(new double[] { 100, 100, 80, 71.43 }, pk);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	@Test
	public void percentageKIncreasingLargerLookback() {
		final int lookback = 4;
		final int numberDataPoints = lookback + 6;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);
		final int daysOfPercentageKValues = numberDataPoints - lookback;

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator(lookback,
		        daysOfPercentageKValues, validator, MATH_CONTEXT);

		final List<BigDecimal> pk = calculator.percentageK(data);

		assertNotNull(pk);
		assertEquals(numberDataPoints - lookback, pk.size());
		assertValuesTwoDecimalPlaces(new double[] { 83.33, 75, 66.67, 63.64, 58.33, 57.14 }, pk);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	private TradingDayPrices[] createFlatPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(1).withLowestPrice(1).withHighestPrice(1)
			        .withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(1).withLowestPrice(0).withHighestPrice(2)
			        .withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(i + 1).withLowestPrice(i / 2)
			        .withHighestPrice(2 * i).withClosingPrice(i + 1).build();
		}

		return prices;
	}
}