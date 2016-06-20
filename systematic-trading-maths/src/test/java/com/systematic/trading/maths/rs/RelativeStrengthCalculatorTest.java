/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.maths.rs;

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
import com.systematic.trading.maths.formula.rs.RelativeStrengthCalculator;
import com.systematic.trading.maths.formula.rs.RelativeStrengthDataPoint;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Verifies the behaviour of RelativeStrengthCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private Validator validator;

	@Test
	public void rsiExample() {
		final TradingDayPrices[] data = createExamplePrices();
		final int lookback = 14;
		final int daysOfRsiValues = data.length - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(daysOfRsiValues, rsi.size());
		assertValueEquals(2.39, rsi.get(0));
		assertValueEquals(1.94, rsi.get(1));
		assertValueEquals(1.96, rsi.get(2));
		assertValueEquals(2.26, rsi.get(3));
		assertValueEquals(1.95, rsi.get(4));
		assertValueEquals(1.34, rsi.get(5));
		assertValueEquals(1.67, rsi.get(6));
		assertValueEquals(1.70, rsi.get(7));
		assertValueEquals(1.25, rsi.get(8));
		assertValueEquals(1.64, rsi.get(9));
		assertValueEquals(1.18, rsi.get(10));
		assertValueEquals(0.99, rsi.get(11));
		assertValueEquals(0.65, rsi.get(12));
		assertValueEquals(0.69, rsi.get(13));
		assertValueEquals(0.7, rsi.get(14));
		assertValueEquals(0.82, rsi.get(15));
		assertValueEquals(0.58, rsi.get(16));
		assertValueEquals(0.48, rsi.get(17));
		assertValueEquals(0.6, rsi.get(18));
	}

	private void assertValueEquals( final double expected, final RelativeStrengthDataPoint actual ) {
		assertEquals(BigDecimal.valueOf(expected).setScale(2, RoundingMode.HALF_EVEN),
		        actual.getValue().setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	public void rsiFlat() {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices(dataSize);
		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(daysOfRsiValues, rsi.size());
		assertValueEquals(0.0, rsi.get(0));
		assertValueEquals(0.0, rsi.get(1));
		assertValueEquals(0.0, rsi.get(2));
		assertValueEquals(0.0, rsi.get(3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices(dataSize);
		data[0] = null;

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		calculator.rs(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices(dataSize);
		data[data.length - 1] = null;

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		calculator.rs(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {
		final int dataSize = 8;
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices(dataSize);
		final int daysOfRsiValues = dataSize - lookback + 1;

		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		calculator.rs(data);
	}

	@Test
	public void rsiIncreasing() {
		final int dataSize = 8;
		final TradingDayPrices[] data = createIncreasingPrices(dataSize);

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(daysOfRsiValues, rsi.size());
		assertValueEquals(0.81, rsi.get(0));
		assertValueEquals(0.86, rsi.get(1));
		assertValueEquals(0.89, rsi.get(2));
		assertValueEquals(0.92, rsi.get(3));

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, dataSize);
	}

	@Test
	public void rsiDecreasing() {
		final int dataSize = 8;
		final TradingDayPrices[] data = createDecreasingPrices(dataSize);

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(daysOfRsiValues, rsi.size());
		assertValueEquals(0, rsi.get(0));
		assertValueEquals(0, rsi.get(1));
		assertValueEquals(0, rsi.get(2));
		assertValueEquals(0, rsi.get(3));

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, dataSize);
	}

	@Test
	public void rsiIncreasingThenDecreasing() {
		final int dataSize = 8;

		final TradingDayPrices[] dataIncreasing = createIncreasingPrices(dataSize);
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices(dataSize);

		final TradingDayPrices[] data = new TradingDayPrices[dataSize * 2];
		for (int i = 0; i < dataIncreasing.length; i++) {
			data[i] = dataIncreasing[i];
		}
		for (int i = 0; i < dataDecreasing.length; i++) {
			data[dataSize + i] = dataDecreasing[i];
		}

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(2 * dataSize - lookback, rsi.size());
		assertValueEquals(0.81, rsi.get(0));
		assertValueEquals(0.86, rsi.get(1));
		assertValueEquals(0.89, rsi.get(2));
		assertValueEquals(0.92, rsi.get(3));
		assertValueEquals(2.94, rsi.get(4));
		assertValueEquals(8.82, rsi.get(5));
		assertValueEquals(3.78, rsi.get(6));
		assertValueEquals(2.15, rsi.get(7));
		assertValueEquals(1.36, rsi.get(8));
		assertValueEquals(0.91, rsi.get(9));
		assertValueEquals(0.64, rsi.get(10));
		assertValueEquals(0.45, rsi.get(11));

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, dataSize);
	}

	@Test
	public void rsiDecreasingThenIncreasing() {
		final int dataSize = 8;

		final TradingDayPrices[] dataIncreasing = createIncreasingPrices(dataSize);
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices(dataSize);

		final TradingDayPrices[] data = new TradingDayPrices[dataSize * 2];
		for (int i = 0; i < dataDecreasing.length; i++) {
			data[i] = dataDecreasing[i];
		}
		for (int i = 0; i < dataIncreasing.length; i++) {
			data[dataSize + i] = dataIncreasing[i];
		}

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthCalculator calculator = new RelativeStrengthCalculator(lookback, daysOfRsiValues,
		        validator, MATH_CONTEXT);

		final List<RelativeStrengthDataPoint> rsi = calculator.rs(data);

		assertNotNull(rsi);
		assertEquals(2 * dataSize - lookback, rsi.size());
		assertValueEquals(0, rsi.get(0));
		assertValueEquals(0, rsi.get(1));
		assertValueEquals(0, rsi.get(2));
		assertValueEquals(0, rsi.get(3));
		assertValueEquals(0, rsi.get(4));
		assertValueEquals(0.11, rsi.get(5));
		assertValueEquals(0.26, rsi.get(6));
		assertValueEquals(0.47, rsi.get(7));
		assertValueEquals(0.73, rsi.get(8));
		assertValueEquals(1.09, rsi.get(9));
		assertValueEquals(1.57, rsi.get(10));
		assertValueEquals(2.21, rsi.get(11));

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, dataSize);
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
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.valueOf(i + 1), BigDecimal.valueOf(i),
			        BigDecimal.valueOf(i + 2), BigDecimal.valueOf(i + 1));
		}

		return prices;
	}

	private TradingDayPrices[] createDecreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		final int base = count * 2;

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.valueOf(base - i + 1),
			        BigDecimal.valueOf(base + i), BigDecimal.valueOf(base + i + 2), BigDecimal.valueOf(base - i + 1));
		}

		return prices;
	}

	/**
	 * Prices from the chart school example.
	 */
	private TradingDayPrices[] createExamplePrices() {
		final double[] exampleCloseValues = { 44.3389, 44.0902, 44.1497, 43.6124, 44.3278, 44.8264, 45.0955, 45.4245,
		        45.8433, 46.0826, 45.8931, 46.0328, 45.6140, 46.2820, 46.2820, 46.0028, 46.0328, 46.4116, 46.2222,
		        45.6439, 46.2122, 46.2521, 45.7137, 46.4515, 45.7835, 45.3548, 44.0288, 44.1783, 44.2181, 44.5672,
		        43.4205, 42.6628, 43.1314 };
		final TradingDayPrices[] prices = new TradingDayPrices[exampleCloseValues.length];

		for (int i = 0; i < prices.length; i++) {
			prices[i] = new TradingDayPricesImpl(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
			        BigDecimal.valueOf(exampleCloseValues[i]));
		}

		return prices;
	}
}
