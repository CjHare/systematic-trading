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
package com.systematic.trading.maths.indicator.sma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorOutputStore;
import com.systematic.trading.maths.indicator.StandardIndicatorOutputStore;

/**
 * Verifying the behaviour for a SimpleMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageCalculatorTest {
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
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( i + 1 ), BigDecimal.valueOf( i ),
					BigDecimal.valueOf( i + 2 ), BigDecimal.valueOf( i + 1 ) );
		}

		return prices;
	}

	@Test(expected = TooFewDataPoints.class)
	public void fewerDataPointsThenLookback() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = new TradingDayPrices[lookback - 1];
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, store,
				MATH_CONTEXT );

		calculator.sma( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedParameterLengths() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( lookback );
		final IndicatorOutputStore store = mock( StandardIndicatorOutputStore.class );
		when( store.getStore( anyInt() ) ).thenReturn( new BigDecimal[lookback - 1] );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, store,
				MATH_CONTEXT );

		calculator.sma( data );
	}

	@Test
	public void smaTwoPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, store,
				MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		assertNotNull( sma );
		assertEquals( numberDataPoints, sma.length );
		assertNull( sma[0] );
		assertEquals( BigDecimal.ONE, sma[1] );
		assertEquals( BigDecimal.ONE, sma[2] );
		assertEquals( BigDecimal.ONE, sma[3] );
		assertEquals( BigDecimal.ONE, sma[4] );
	}

	@Test
	public void smaFirstPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, store,
				MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		assertNotNull( sma );
		assertEquals( numberDataPoints, sma.length );
		assertNull( sma[0] );
		assertNull( sma[1] );
		assertEquals( BigDecimal.ONE, sma[2] );
		assertEquals( BigDecimal.ONE, sma[3] );
		assertEquals( BigDecimal.ONE, sma[4] );
	}

	@Test
	public void smaThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, store,
				MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		assertNotNull( sma );
		assertEquals( numberDataPoints, sma.length );
		assertNull( sma[0] );
		assertEquals( BigDecimal.valueOf( 1.5 ), sma[1] );
		assertEquals( BigDecimal.valueOf( 2.5 ), sma[2] );
		assertEquals( BigDecimal.valueOf( 3.5 ), sma[3] );
		assertEquals( BigDecimal.valueOf( 4.5 ), sma[4] );
		assertEquals( BigDecimal.valueOf( 5.5 ), sma[5] );
	}
}
