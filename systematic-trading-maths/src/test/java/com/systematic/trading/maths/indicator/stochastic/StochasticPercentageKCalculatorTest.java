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
package com.systematic.trading.maths.indicator.stochastic;

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
 * Verifies the StochasticPercentageKCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class StochasticPercentageKCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private IndicatorInputValidator validator;

	private TradingDayPrices[] createFlatPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 1 ),
					BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

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
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( i + 1 ),
					BigDecimal.valueOf( i / 2 ), BigDecimal.valueOf( 2 * i ), BigDecimal.valueOf( i + 1 ) );
		}

		return prices;
	}

	@Test
	public void percentageKThreePoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 1 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[2] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[3] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[4] );
	}

	@Test
	public void percentageKThreeFlatPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 2;
		final TradingDayPrices[] data = createFlatPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 1 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertEquals( BigDecimal.valueOf( 0.0 ), pk[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 0.0 ), pk[3].setScale( 1, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void percentageKFirstPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getFirstNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) ).thenReturn( 1 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 1 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertNull( pk[2] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[3] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[4] );
	}

	@Test
	public void percentageKLastPointNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[data.length - 1] = null;
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getFirstNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) ).thenReturn( 0 );
		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 2 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[2] );
		assertEquals( BigDecimal.valueOf( 50.0 ), pk[3] );
		assertNull( pk[4] );
	}

	@Test
	public void percentageKFourPoints() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 1 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertEquals( BigDecimal.valueOf( 100.0 ), pk[2].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 100.0 ), pk[3].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 80.0 ), pk[4].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 71.43 ), pk[5].setScale( 2, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void percentageKIncreasingLargerLookback() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final int numberDataPoints = lookback + 6;
		final TradingDayPrices[] data = createIncreasingPrices( numberDataPoints );
		final IndicatorOutputStore store = new StandardIndicatorOutputStore();

		when( validator.getLastNonNullIndex( any( TradingDayPrices[].class ) ) ).thenReturn( data.length - 1 );

		final StochasticPercentageKCalculator calculator = new StochasticPercentageKCalculator( lookback, validator,
				store, MATH_CONTEXT );

		final BigDecimal[] pk = calculator.percentageK( data );

		verify( validator ).getFirstNonNullIndex( data, data.length, lookback + 1 );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( pk );
		assertEquals( numberDataPoints, pk.length );
		assertNull( pk[0] );
		assertNull( pk[1] );
		assertNull( pk[2] );
		assertNull( pk[3] );
		assertEquals( BigDecimal.valueOf( 83.33 ), pk[4].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 75.0 ), pk[5].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 66.67 ), pk[6].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 63.64 ), pk[7].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 58.33 ), pk[8].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 57.14 ), pk[9].setScale( 2, RoundingMode.HALF_EVEN ) );
	}

}
