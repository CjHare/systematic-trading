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
package com.systematic.trading.maths.indicator.rsi;

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
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;

/**
 * Verifies the behaviour of RelativeStrengthIndexCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexCalculatorTest {
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

	private TradingDayPrices[] createDecreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		final int base = count * 2;

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( base - i + 1 ),
					BigDecimal.valueOf( base + i ), BigDecimal.valueOf( base + i + 2 ),
					BigDecimal.valueOf( base - i + 1 ) );
		}

		return prices;
	}

	@Test
	public void rsiFlat() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices( dataSize );
		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		final List<BigDecimal> rsi = calculator.rsi( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.size() );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 0 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 1 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 2 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 3 ).setScale( 2, RoundingMode.HALF_EVEN ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices( dataSize );
		data[0] = null;

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		doThrow( new IllegalArgumentException() ).when( validator )
				.verifyZeroNullEntries( any( TradingDayPrices[].class ) );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		calculator.rsi( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices( dataSize );
		data[data.length - 1] = null;

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		doThrow( new IllegalArgumentException() ).when( validator )
				.verifyZeroNullEntries( any( TradingDayPrices[].class ) );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		calculator.rsi( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( dataSize );
		final int daysOfRsiValues = dataSize - lookback + 1;

		doThrow( new IllegalArgumentException() ).when( validator ).verifyEnoughValues( any( TradingDayPrices[].class ),
				anyInt() );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		calculator.rsi( data );
	}

	@Test
	public void rsiIncreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createIncreasingPrices( dataSize );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		final List<BigDecimal> rsi = calculator.rsi( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.size() );
		// RS of 50 == RSI 49.02
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 0 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 1 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 2 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 3 ).setScale( 2, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, dataSize );
	}

	@Test
	public void rsiDecreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createDecreasingPrices( dataSize );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		final List<BigDecimal> rsi = calculator.rsi( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.size() );
		assertEquals( BigDecimal.ZERO, rsi.get( 0 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 1 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 2 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 3 ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, dataSize );
	}

	@Test
	public void rsiIncreasingThenDecreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;

		final TradingDayPrices[] dataIncreasing = createIncreasingPrices( dataSize );
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices( dataSize );

		final TradingDayPrices[] data = new TradingDayPrices[dataSize * 2];
		for (int i = 0; i < dataIncreasing.length; i++) {
			data[i] = dataIncreasing[i];
		}
		for (int i = 0; i < dataDecreasing.length; i++) {
			data[dataSize + i] = dataDecreasing[i];
		}

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		final List<BigDecimal> rsi = calculator.rsi( data );

		assertNotNull( rsi );
		assertEquals( 2 * dataSize - lookback, rsi.size() );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 0 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 1 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 2 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 3 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 49.02 ), rsi.get( 4 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 43.15 ), rsi.get( 5 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 35.13 ), rsi.get( 6 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 26.82 ), rsi.get( 7 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 19.24 ), rsi.get( 8 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 13.08 ), rsi.get( 9 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 8.52 ), rsi.get( 10 ).setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 5.4 ), rsi.get( 11 ).setScale( 1, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, dataSize );
	}

	@Test
	public void rsiDecreasingThenIncreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;

		final TradingDayPrices[] dataIncreasing = createIncreasingPrices( dataSize );
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices( dataSize );

		final TradingDayPrices[] data = new TradingDayPrices[dataSize * 2];
		for (int i = 0; i < dataDecreasing.length; i++) {
			data[i] = dataDecreasing[i];
		}
		for (int i = 0; i < dataIncreasing.length; i++) {
			data[dataSize + i] = dataIncreasing[i];
		}

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, MATH_CONTEXT );

		final List<BigDecimal> rsi = calculator.rsi( data );

		assertNotNull( rsi );
		assertEquals( 2 * dataSize - lookback, rsi.size() );
		assertEquals( BigDecimal.ZERO, rsi.get( 0 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 1 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 2 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 3 ) );
		assertEquals( BigDecimal.ZERO, rsi.get( 4 ) );
		assertEquals( BigDecimal.valueOf( 6.8493 ), rsi.get( 5 ).setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 14.8699 ), rsi.get( 6 ).setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 23.1788 ), rsi.get( 7 ).setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 30.7623 ), rsi.get( 8 ).setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 36.9241 ), rsi.get( 9 ).setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 41.475 ), rsi.get( 10 ).setScale( 3, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 44.6047 ), rsi.get( 11 ).setScale( 4, RoundingMode.HALF_EVEN ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, dataSize );
	}
}
