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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
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
		final int daysOfSmaValues = numberDataPoints - lookback;

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, MATH_CONTEXT );

		final List<BigDecimal> sma = calculator.sma( data );

		assertNotNull( sma );
		assertEquals( 4, sma.size() );
		assertEquals( BigDecimal.ONE, sma.get( 0 ) );
		assertEquals( BigDecimal.ONE, sma.get( 1 ) );
		assertEquals( BigDecimal.ONE, sma.get( 2 ) );
		assertEquals( BigDecimal.ONE, sma.get( 3 ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFirstDataPoint() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final int daysOfSmaValues = numberDataPoints - lookback - 1;

		doThrow( new IllegalArgumentException() ).when( validator )
				.verifyZeroNullEntries( any( TradingDayPrices[].class ) );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, MATH_CONTEXT );

		calculator.sma( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullLastDataPoint() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[lookback + 2] = null;
		final int daysOfSmaValues = numberDataPoints - lookback - 2;

		doThrow( new IllegalArgumentException() ).when( validator )
				.verifyZeroNullEntries( any( TradingDayPrices[].class ) );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, MATH_CONTEXT );

		calculator.sma( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final int daysOfSmaValues = numberDataPoints - lookback + 1;

		doThrow( new IllegalArgumentException() ).when( validator ).verifyEnoughValues( any( TradingDayPrices[].class ),
				anyInt() );

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, MATH_CONTEXT );

		calculator.sma( data );
	}

	@Test
	public void smaThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final int daysOfSmaValues = numberDataPoints - lookback;

		final SimpleMovingAverageCalculator calculator = new SimpleMovingAverageCalculator( lookback, daysOfSmaValues,
				validator, MATH_CONTEXT );

		final List<BigDecimal> sma = calculator.sma( data );

		assertNotNull( sma );
		assertEquals( 5, sma.size() );
		assertEquals( BigDecimal.valueOf( 1.5 ), sma.get( 0 ) );
		assertEquals( BigDecimal.valueOf( 2.5 ), sma.get( 1 ) );
		assertEquals( BigDecimal.valueOf( 3.5 ), sma.get( 2 ) );
		assertEquals( BigDecimal.valueOf( 4.5 ), sma.get( 3 ) );
		assertEquals( BigDecimal.valueOf( 5.5 ), sma.get( 4 ) );

		verify( validator ).verifyZeroNullEntries( data );
		verify( validator ).verifyEnoughValues( data, numberDataPoints );
	}
}
