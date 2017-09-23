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
package com.systematic.trading.maths.indicator.atr;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Tests the behaviour of the AverageTrueRangeCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class AverageTrueRangeCalculatorTest {

	@Mock
	private Validator validator;

	@Test
	public void atrFlat() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 1;

		final TradingDayPrices[] data = createPrices(numberDataPoints);

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator(lookback, validator);

		final List<BigDecimal> atr = calculator.atr(data);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);

		assertNotNull(atr);
		assertEquals(numberDataPoints, atr.size());
		assertValues(new double[] { 2, 2, 2 }, atr);
	}

	@Test
	public void atrIncreasing() {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator(lookback, validator);

		final List<BigDecimal> atr = calculator.atr(data);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);

		assertNotNull(atr);
		assertEquals(numberDataPoints, atr.size());
		assertValues(new double[] { 5, 6.25, 8.4375, 11.32812, 14.74609 }, atr);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrInitialNullEntry() {
		final int lookback = 2;
		final int numberDataPoints = 4;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[0] = null;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator(lookback, validator);

		calculator.atr(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrLastNullEntry() {
		final int lookback = 2;
		final int numberDataPoints = 4;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[data.length - 1] = null;

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator(lookback, validator);

		calculator.atr(data);
	}

	@Test
	public void atrThreeRangeTypes() {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createThreeTypesOfVolatility(numberDataPoints);

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator(lookback, validator);

		final List<BigDecimal> atr = calculator.atr(data);

		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);

		assertNotNull(atr);
		assertEquals(numberDataPoints, atr.size());
		assertValues(new double[] { 5, 5, 5, 8.75, 9.0625 }, atr);
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
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(count).withLowestPrice(0)
			        .withHighestPrice(count + i * 5).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createThreeTypesOfVolatility( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		// Biggest swing is between today's high & low
		for (int i = 0; i < count - 2; i++) {
			prices[i] = new TradingDayPricesBuilder().withOpeningPrice(count).withLowestPrice(0).withHighestPrice(count)
			        .withClosingPrice(count).build();
		}

		// Biggest swing is between the highest of today and yesterday's close
		prices[count - 2] = new TradingDayPricesBuilder().withOpeningPrice(count).withLowestPrice(count)
		        .withHighestPrice(5 * count).withClosingPrice(2 * count).build();

		// Biggest swing is between the low of today and yesterday's close
		prices[count - 1] = new TradingDayPricesBuilder().withOpeningPrice(count).withLowestPrice(0)
		        .withHighestPrice(count).withClosingPrice(count).build();

		return prices;
	}
}