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

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValue;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValuesTwoDecimalPlaces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

	@Test
	public void emaOnePoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(lookback);

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final List<BigDecimal> ema = calculator.ema(data);

		assertNotNull(ema);
		assertEquals(1, ema.size());
		assertValue(1, ema);

		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	@Test
	public void emaTwoPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createPrices(numberDataPoints);

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final List<BigDecimal> ema = calculator.ema(data);

		assertNotNull(ema);
		assertEquals(2, ema.size());
		assertValues(new double[] { 1, 1 }, ema);

		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaFirstPointNull() {
		final int lookback = 3;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[0] = null;

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		calculator.ema(data);
	}

	@Test
	public void emaThreePoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final List<BigDecimal> ema = calculator.ema(data);

		assertNotNull(ema);
		assertEquals(3, ema.size());
		assertValuesTwoDecimalPlaces(new double[] { 1.5, 2.5, 3.67 }, ema);

		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaTwoPointsLastNull() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);
		data[data.length - 1] = null;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		calculator.ema(data);
	}

	@Test
	public void emaTwoPointsDecimal() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final List<BigDecimal> data = createDecimalPrices(numberDataPoints);

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final List<BigDecimal> ema = calculator.ema(data);

		assertNotNull(ema);
		assertEquals(3, ema.size());
		assertValues(new double[] { 1, 1, 1 }, ema);

		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	@Test
	public void emaThreePointsDecimal() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final List<BigDecimal> data = createIncreasingDecimalPrices(numberDataPoints);

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final List<BigDecimal> ema = calculator.ema(data);

		assertNotNull(ema);
		assertEquals(3, ema.size());
		assertValuesTwoDecimalPlaces(new double[] { 0.5, 1.5, 2.67 }, ema);

		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPointsDecimal() {
		final int lookback = 2;
		final int numberDataPoints = lookback - 1;
		final List<BigDecimal> data = createIncreasingDecimalPrices(numberDataPoints);

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(anyListOf(BigDecimal.class),
		        anyInt());

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		calculator.ema(data);
	}

	@Test
	public void getMinimumNumberOfPrices() {
		final int lookback = 2;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator(lookback,
		        validator);

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals(lookback, requiredDays);
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
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(i + 1).withLowestPrice(1).withHighestPrice(i + 2)
			        .withClosingPrice(i + 1).build();
		}

		return prices;
	}

	private List<BigDecimal> createIncreasingDecimalPrices( final int count ) {
		final List<BigDecimal> prices = new ArrayList<BigDecimal>(count);

		for (int i = 0; i < count; i++) {
			prices.add(BigDecimal.valueOf(i));
		}

		return prices;
	}

	private List<BigDecimal> createDecimalPrices( final int count ) {
		final List<BigDecimal> prices = new ArrayList<BigDecimal>(count);

		for (int i = 0; i < count; i++) {
			prices.add(BigDecimal.valueOf(1));
		}

		return prices;
	}
}