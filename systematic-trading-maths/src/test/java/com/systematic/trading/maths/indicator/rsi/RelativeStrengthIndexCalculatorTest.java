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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
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
		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[daysOfRsiValues];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[daysOfRsiValues] );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( daysOfRsiValues - 1 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( dataSize - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.length );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[0].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[1].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[2].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[3].setScale( 2, RoundingMode.HALF_EVEN ) );
	}

	@Test
	public void rsiFlatStartingNull() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices( dataSize );
		data[0] = null;
		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[daysOfRsiValues];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[daysOfRsiValues] );
		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 1 );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( daysOfRsiValues - 2 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( dataSize - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getStartingNonNullIndex( data, daysOfRsiValues, lookback + daysOfRsiValues );
		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.length );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[0].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[1].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[2].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertNull( rsi[3] );
	}

	@Test
	public void rsiFlatEndingNull() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createPrices( dataSize );
		data[data.length - 1] = null;
		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[daysOfRsiValues];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[daysOfRsiValues] );
		when( validator.getStartingNonNullIndex( any( TradingDayPrices[].class ), anyInt(), anyInt() ) )
				.thenReturn( 0 );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( daysOfRsiValues - 2 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( dataSize - 2 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getStartingNonNullIndex( data, daysOfRsiValues, lookback + daysOfRsiValues );
		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.length );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[0].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[1].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[2].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertNull( rsi[3] );
	}

	@Test
	public void rsiIncreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createIncreasingPrices( dataSize );
		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[daysOfRsiValues];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[daysOfRsiValues] );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( daysOfRsiValues - 1 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( dataSize - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.length );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[0].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[1].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[2].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[3].setScale( 2, RoundingMode.HALF_EVEN ) );

	}

	@Test
	public void rsiDecreasing() throws TooFewDataPoints, TooManyDataPoints {
		final int dataSize = 8;
		final TradingDayPrices[] data = createDecreasingPrices( dataSize );
		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[daysOfRsiValues];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[daysOfRsiValues] );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( daysOfRsiValues - 1 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( dataSize - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( daysOfRsiValues, rsi.length );
		assertEquals( BigDecimal.ZERO, rsi[0] );
		assertEquals( BigDecimal.ZERO, rsi[1] );
		assertEquals( BigDecimal.ZERO, rsi[2] );
		assertEquals( BigDecimal.ZERO, rsi[3] );
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

		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[2 * dataSize - lookback];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[2 * dataSize - lookback] );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( rsArray.length - 1 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( data.length - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( 2 * dataSize - lookback, rsi.length );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[0].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[1].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[2].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[3].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 99.01 ), rsi[4].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 86.3 ), rsi[5].setScale( 1, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 70.26 ), rsi[6].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 53.64 ), rsi[7].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 38.48 ), rsi[8].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 26.15 ), rsi[9].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 17.05 ), rsi[10].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 10.79 ), rsi[11].setScale( 2, RoundingMode.HALF_EVEN ) );
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

		final IndicatorOutputStore rsStore = mock( StandardIndicatorOutputStore.class );
		final IndicatorOutputStore rsiStore = mock( StandardIndicatorOutputStore.class );

		final int lookback = 4;
		final int daysOfRsiValues = dataSize - lookback;

		final BigDecimal[] rsArray = new BigDecimal[2 * dataSize - lookback];
		when( rsStore.getStore( anyInt() ) ).thenReturn( rsArray );
		when( rsiStore.getStore( anyInt() ) ).thenReturn( new BigDecimal[2 * dataSize - lookback] );
		when( validator.getLastNonNullIndex( rsArray ) ).thenReturn( rsArray.length - 1 );
		when( validator.getLastNonNullIndex( data ) ).thenReturn( data.length - 1 );

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator( lookback,
				daysOfRsiValues, validator, rsStore, rsiStore, MATH_CONTEXT );

		final BigDecimal[] rsi = calculator.rsi( data );

		verify( validator ).getLastNonNullIndex( rsArray );
		verify( validator ).getLastNonNullIndex( data );

		assertNotNull( rsi );
		assertEquals( 2 * dataSize - lookback, rsi.length );
		assertEquals( BigDecimal.ZERO, rsi[0] );
		assertEquals( BigDecimal.ZERO, rsi[1] );
		assertEquals( BigDecimal.ZERO, rsi[2] );
		assertEquals( BigDecimal.ZERO, rsi[3] );
		assertEquals( BigDecimal.ZERO, rsi[4] );
		assertEquals( BigDecimal.valueOf( 13.6986 ), rsi[5].setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 29.7398 ), rsi[6].setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 46.3576 ), rsi[7].setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 61.5245 ), rsi[8].setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 73.8482 ), rsi[9].setScale( 4, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 82.95 ), rsi[10].setScale( 2, RoundingMode.HALF_EVEN ) );
		assertEquals( BigDecimal.valueOf( 89.2093 ), rsi[11].setScale( 4, RoundingMode.HALF_EVEN ) );
	}
}
