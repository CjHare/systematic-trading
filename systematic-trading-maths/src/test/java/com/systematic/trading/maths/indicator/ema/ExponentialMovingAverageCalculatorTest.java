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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;

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

	private List<BigDecimal> createIncreasingDecimalPrices( final int count ) {
		final List<BigDecimal> prices = new ArrayList<BigDecimal>( count );

		for (int i = 0; i < count; i++) {
			prices.add( BigDecimal.valueOf( i ) );
		}

		return prices;
	}

	private List<BigDecimal> createDecimalPrices( final int count ) {
		final List<BigDecimal> prices = new ArrayList<BigDecimal>( count );

		for (int i = 0; i < count; i++) {
			prices.add( BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

	@Test
	public void emaOnePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices( lookback );

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( lookback - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final List<BigDecimal> ema = calculator.ema( data );

		assertNotNull( ema );
		assertEquals( 1, ema.size() );
		assertEquals( BigDecimal.ONE, ema.get( 0 ).setScale( 0, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyEnoughValues( data, lookback );
		verify( validator ).verifyZeroNullEntries( data );
	}

	@Test
	public void emaTwoPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = createPrices( numberDataPoints );

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final List<BigDecimal> ema = calculator.ema( data );

		assertNotNull( ema );
		assertEquals( 2, ema.size() );
		assertEquals( BigDecimal.ONE, ema.get( 0 ).setScale( 0, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.ONE, ema.get( 1 ).setScale( 0, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyEnoughValues( data, lookback );
		verify( validator ).verifyZeroNullEntries( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaFirstPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 3;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;

		doThrow( new IllegalArgumentException() ).when( validator ).verifyEnoughValues( any( TradingDayPrices[].class ),
				anyInt() );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		calculator.ema( data );
	}

	@Test
	public void emaThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final List<BigDecimal> ema = calculator.ema( data );

		assertNotNull( ema );
		assertEquals( 3, ema.size() );
		assertEquals( BigDecimal.valueOf( 1.5 ), ema.get( 0 ).setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 2.5 ), ema.get( 1 ).setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 3.67 ), ema.get( 2 ).setScale( 2, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyEnoughValues( data, lookback );
		verify( validator ).verifyZeroNullEntries( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaTwoPointsLastNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		data[data.length - 1] = null;

		doThrow( new IllegalArgumentException() ).when( validator )
				.verifyZeroNullEntries( any( TradingDayPrices[].class ) );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		calculator.ema( data );
	}

	@Test
	public void emaTwoPointsDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final List<BigDecimal> data = createDecimalPrices( numberDataPoints );

		when( validator.getLastNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final List<BigDecimal> ema = calculator.ema( data );

		assertNotNull( ema );
		assertEquals( 3, ema.size() );
		assertEquals( BigDecimal.ONE, ema.get( 0 ).setScale( 0, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.ONE, ema.get( 1 ).setScale( 0, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.ONE, ema.get( 2 ).setScale( 0, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyEnoughValues( data, lookback );
		verify( validator ).verifyZeroNullEntries( data );
	}

	@Test
	public void emaThreePointsDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final List<BigDecimal> data = createIncreasingDecimalPrices( numberDataPoints );

		when( validator.getLastNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( numberDataPoints - 1 );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final List<BigDecimal> ema = calculator.ema( data );

		assertNotNull( ema );
		assertEquals( 3, ema.size() );
		assertEquals( BigDecimal.valueOf( 0.5 ), ema.get( 0 ).setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 1.5 ), ema.get( 1 ).setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 2.67 ), ema.get( 2 ).setScale( 2, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyEnoughValues( data, lookback );
		verify( validator ).verifyZeroNullEntries( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPointsDecimal() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback - 1;
		final List<BigDecimal> data = createIncreasingDecimalPrices( numberDataPoints );

		doThrow( new IllegalArgumentException() ).when( validator ).verifyEnoughValues( anyListOf( BigDecimal.class ),
				anyInt() );

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		calculator.ema( data );
	}

	@Test
	public void getMinimumNumberOfPrices() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;

		final ExponentialMovingAverageCalculator calculator = new ExponentialMovingAverageCalculator( lookback,
				validator, MATH_CONTEXT );

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals( lookback, requiredDays );
	}
}
