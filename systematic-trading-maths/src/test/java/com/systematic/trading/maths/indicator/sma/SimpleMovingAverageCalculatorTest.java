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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.store.IndicatorOutputStore;
import com.systematic.trading.maths.store.StandardIndicatorOutputStore;

/**
 * Verifying the behaviour for a SimpleMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleMovingAverageCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private IndicatorInputValidator validator;

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

	@Test
	public void smaTwoPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfSmaValues = numberDataPoints - lookback;

		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 0 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, store, MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		verify( validator ).getStartingNonNullIndex( data, data.length, lookback + daysOfSmaValues );
		verify( validator ).getLastNonNullIndex( data );

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
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfSmaValues = numberDataPoints - lookback - 1;

		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 1 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, store, MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		verify( validator ).getStartingNonNullIndex( data, data.length, lookback + daysOfSmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( sma );
		assertEquals( numberDataPoints, sma.length );
		assertNull( sma[0] );
		assertNull( sma[1] );
		assertEquals( BigDecimal.ONE, sma[2] );
		assertEquals( BigDecimal.ONE, sma[3] );
		assertEquals( BigDecimal.ONE, sma[4] );
	}

	@Test
	public void smaLastPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[lookback + 2] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfSmaValues = numberDataPoints - lookback - 2;

		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 1 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 3 );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, store, MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		verify( validator ).getStartingNonNullIndex( data, data.length, lookback + daysOfSmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( sma );
		assertEquals( numberDataPoints, sma.length );
		assertNull( sma[0] );
		assertEquals( BigDecimal.ONE, sma[1] );
		assertEquals( BigDecimal.ONE, sma[2] );
		assertEquals( BigDecimal.ONE, sma[3] );
		assertNull( sma[4] );
	}

	@Test
	public void smaThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfSmaValues = numberDataPoints - lookback;

		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 0 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, store, MATH_CONTEXT );

		final BigDecimal[] sma = calculator.sma( data );

		verify( validator ).getStartingNonNullIndex( data, data.length, lookback + daysOfSmaValues );
		verify( validator ).getLastNonNullIndex( data );

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
