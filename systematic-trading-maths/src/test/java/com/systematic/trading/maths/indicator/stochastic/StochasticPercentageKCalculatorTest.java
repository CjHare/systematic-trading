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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.indicator.Validator;

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

	private TradingDayPrices[] createFlatPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.valueOf(1), BigDecimal.valueOf(1),
			        BigDecimal.valueOf(1), BigDecimal.valueOf(1));
		}

		return prices;
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.valueOf(1), BigDecimal.valueOf(0),
			        BigDecimal.valueOf(2), BigDecimal.valueOf(1));
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.valueOf(i + 1), BigDecimal.valueOf(i / 2),
			        BigDecimal.valueOf(2 * i), BigDecimal.valueOf(i + 1));
		}

		return prices;
	}

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
		assertEquals(BigDecimal.valueOf(50.0), pk.get(0));
		assertEquals(BigDecimal.valueOf(50.0), pk.get(1));
		assertEquals(BigDecimal.valueOf(50.0), pk.get(2));

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
		assertEquals(BigDecimal.valueOf(0.0), pk.get(0).setScale(1, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(0.0), pk.get(1).setScale(1, RoundingMode.HALF_EVEN));

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
		assertEquals(BigDecimal.valueOf(100.0), pk.get(0).setScale(1, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(100.0), pk.get(1).setScale(1, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(80.0), pk.get(2).setScale(1, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(71.43), pk.get(3).setScale(2, RoundingMode.HALF_EVEN));

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
		assertEquals(BigDecimal.valueOf(83.33), pk.get(0).setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(75.0), pk.get(1).setScale(1, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(66.67), pk.get(2).setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(63.64), pk.get(3).setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(58.33), pk.get(4).setScale(2, RoundingMode.HALF_EVEN));
		assertEquals(BigDecimal.valueOf(57.14), pk.get(5).setScale(2, RoundingMode.HALF_EVEN));

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

}
