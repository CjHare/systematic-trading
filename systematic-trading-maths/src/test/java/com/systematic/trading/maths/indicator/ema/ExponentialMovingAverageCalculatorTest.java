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
package com.systematic.trading.maths.indicator.ema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
 * Test the ExponentialMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ExponentialMovingAverageCalculatorTest {
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

	private BigDecimal[] createIncreasingDecimalPrices( final int count ) {
		final BigDecimal[] prices = new BigDecimal[count];

		for (int i = 0; i < count; i++) {
			prices[i] = BigDecimal.valueOf( i );
		}

		return prices;
	}

	private BigDecimal[] createDecimalPrices( final int count ) {
		final BigDecimal[] prices = new BigDecimal[count];

		for (int i = 0; i < count; i++) {
			prices[i] = BigDecimal.valueOf( 1 );
		}

		return prices;
	}

	@Test
	public void emaOnePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfEmaValues = numberDataPoints - lookback;

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getStartingNonNullIndex( data, numberDataPoints, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.ONE, ema[2].setScale( 0, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaTwoPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfEmaValues = numberDataPoints - lookback;

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getStartingNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.ONE, ema[2].setScale( 0, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.ONE, ema[3].setScale( 0, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaFirstPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 1 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getStartingNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNull( ema[2] );
		assertNotNull( ema[3] );
		assertEquals( BigDecimal.ONE, ema[3].setScale( 0, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getStartingNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.valueOf( 2.5 ), ema[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 3.67 ), ema[3].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 4.67 ), ema[4].setScale( 2, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaTwoPointsLastNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		data[data.length - 1] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 2 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getStartingNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.valueOf( 2.5 ), ema[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 3.67 ), ema[3].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertNull( ema[4] );
	}

	@Test
	public void emaTwoPointsDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final BigDecimal[] data = createDecimalPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfEmaValues = numberDataPoints - lookback;

		when( validator.getLastNonNullIndex( any( BigDecimal[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getFirstNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.ONE, ema[2].setScale( 0, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.ONE, ema[3].setScale( 0, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaFirstPointNullDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final BigDecimal[] data = createDecimalPrices( numberDataPoints );
		data[0] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getFirstNonNullIndex( any( BigDecimal[].class ), anyInt(), anyInt() ) ).thenReturn( 1 );
		when( validator.getLastNonNullIndex( any( BigDecimal[].class ) ) ).thenReturn( numberDataPoints - 1 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getFirstNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNull( ema[2] );
		assertNotNull( ema[3] );
		assertEquals( BigDecimal.ONE, ema[3].setScale( 0, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaThreePointsDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final BigDecimal[] data = createIncreasingDecimalPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getLastNonNullIndex( any( BigDecimal[].class ) ) ).thenReturn( numberDataPoints - 1 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getFirstNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.valueOf( 1.5 ), ema[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 2.67 ), ema[3].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 3.67 ), ema[4].setScale( 2, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void emaTwoPointsLastNullDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final BigDecimal[] data = createIncreasingDecimalPrices( numberDataPoints );
		data[data.length - 1] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		when( validator.getLastNonNullIndex( any( BigDecimal[].class ) ) ).thenReturn( numberDataPoints - 2 );
		final int daysOfEmaValues = numberDataPoints - lookback;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final BigDecimal[] ema = calculator.ema( data );

		verify( validator ).getFirstNonNullIndex( data, numberDataPoints, lookback + daysOfEmaValues );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( ema );
		assertEquals( numberDataPoints, ema.length );
		assertNull( ema[0] );
		assertNull( ema[1] );
		assertNotNull( ema[2] );
		assertEquals( BigDecimal.valueOf( 1.5 ), ema[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 2.67 ), ema[3].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertNull( ema[4] );
	}

	@Test
	public void getMinimumNumberOfPrices() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();
		final int daysOfEmaValues = 1;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				daysOfEmaValues, validator, store, MATH_CONTEXT );

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals( lookback + 1, requiredDays );
	}
}
