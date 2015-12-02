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
package com.systematic.trading.maths.indicator.atr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * Tests the behaviour of the AverageTrueRangeCalculator.
 * 
 * @author CJ Hare
 */
public class AverageTrueRangeCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 0 ),
					BigDecimal.valueOf( 2 ), BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( count ),
					BigDecimal.valueOf( 0 ), BigDecimal.valueOf( count + i * 5 ), BigDecimal.valueOf( count ) );
		}

		return prices;
	}

	private TradingDayPrices[] createThreeTypesOfVolatility( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		// Biggest swing is between today's high & low
		for (int i = 0; i < count - 2; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( count ),
					BigDecimal.valueOf( 0 ), BigDecimal.valueOf( count ), BigDecimal.valueOf( count ) );
		}

		// Biggest swing is between the highest of today and yesterday's close
		prices[count - 2] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( count ),
				BigDecimal.valueOf( 2 * count ), BigDecimal.valueOf( 5 * count ), BigDecimal.valueOf( 2 * count ) );

		// Biggest swing is between the low of today and yesterday's close
		prices[count - 1] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( count ),
				BigDecimal.valueOf( 0 ), BigDecimal.valueOf( count ), BigDecimal.valueOf( count ) );

		return prices;
	}

	@Test(expected = TooFewDataPoints.class)
	public void fewerDataPointsThenLookback() throws TooFewDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( lookback );
		final BigDecimal[] atrValues = new BigDecimal[lookback];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		calculator.atr( data, atrValues );
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedParameterLengths() throws TooFewDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( lookback );
		final BigDecimal[] atrValues = new BigDecimal[lookback - 1];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		calculator.atr( data, atrValues );
	}

	@Test
	public void atrFlat() throws TooFewDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final BigDecimal[] atrValues = new BigDecimal[numberDataPoints];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		final BigDecimal[] atr = calculator.atr( data, atrValues );

		assertNotNull( atr );
		assertEquals( numberDataPoints, atr.length );
		assertEquals( BigDecimal.valueOf( 2 ), atr[0] );
		assertEquals( BigDecimal.valueOf( 2 ), atr[1] );
		assertEquals( BigDecimal.valueOf( 2 ), atr[2] );
	}

	@Test
	public void atrIncreasing() throws TooFewDataPoints {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final BigDecimal[] atrValues = new BigDecimal[numberDataPoints];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		final BigDecimal[] atr = calculator.atr( data, atrValues );

		assertNotNull( atr );
		assertEquals( numberDataPoints, atr.length );
		assertEquals( BigDecimal.valueOf( 5 ), atr[0] );
		assertEquals( BigDecimal.valueOf( 6.25 ), atr[1] );
		assertEquals( BigDecimal.valueOf( 8.4375 ), atr[2] );
		assertEquals( BigDecimal.valueOf( 11.328125 ), atr[3] );
		assertEquals( BigDecimal.valueOf( 14.74609375 ), atr[4] );
	}

	@Test
	public void atrInitialNullEntry() throws TooFewDataPoints {
		final int lookback = 2;
		final int numberDataPoints = 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final BigDecimal[] atrValues = new BigDecimal[numberDataPoints];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		final BigDecimal[] atr = calculator.atr( data, atrValues );

		assertNotNull( atr );
		assertEquals( numberDataPoints, atr.length );
		assertNull( atr[0] );
		assertEquals( BigDecimal.valueOf( 2 ), atr[1] );
		assertEquals( BigDecimal.valueOf( 2 ), atr[2] );
	}

	@Test(expected = TooFewDataPoints.class)
	public void atrAllNull() throws TooFewDataPoints {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = new TradingDayPrices[numberDataPoints];
		final BigDecimal[] atrValues = new BigDecimal[numberDataPoints];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		calculator.atr( data, atrValues );
	}

	@Test
	public void atrThreeRangeTypes() throws TooFewDataPoints {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createThreeTypesOfVolatility( numberDataPoints );
		final BigDecimal[] atrValues = new BigDecimal[numberDataPoints];

		final AverageTrueRangeCalculator calculator = new AverageTrueRangeCalculator( lookback, MATH_CONTEXT );

		final BigDecimal[] atr = calculator.atr( data, atrValues );

		assertNotNull( atr );
		// assertEquals( numberDataPoints, atr.length );
		// assertEquals( BigDecimal.valueOf( 5 ), atr[0] );
		// assertEquals( BigDecimal.valueOf( 6.25 ), atr[1] );
		// assertEquals( BigDecimal.valueOf( 8.4375 ), atr[2] );
		// assertEquals( BigDecimal.valueOf( 11.328125 ), atr[3] );
		// assertEquals( BigDecimal.valueOf( 14.74609375 ), atr[4] );
	}
}
