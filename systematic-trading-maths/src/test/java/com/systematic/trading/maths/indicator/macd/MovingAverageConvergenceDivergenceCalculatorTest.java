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
package com.systematic.trading.maths.indicator.macd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorOutputStore;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.model.SignalType;

/**
 * Verifies the behaviour of the MovingAverageConvergenceDivergenceCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class MovingAverageConvergenceDivergenceCalculatorTest {

	@Mock
	private ExponentialMovingAverage fastEma;

	@Mock
	private ExponentialMovingAverage slowEma;

	@Mock
	private ExponentialMovingAverage signalEma;

	@Mock
	private IndicatorOutputStore signalStore;

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now().plusDays( i ), BigDecimal.valueOf( 1 ),
					BigDecimal.valueOf( 0 ), BigDecimal.valueOf( 2 ), BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

	@Test(expected = IllegalArgumentException.class)
	public void signalStoreTooSmall() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback - 1] );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		calculator.macd( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void slowEmaTooSmall() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback - 1] );
		when( signalEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		calculator.macd( data );
	}

	@Test(expected = IllegalArgumentException.class)
	public void fastEmaTooSmall() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 4;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback - 1] );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		calculator.macd( data );
	}

	@Test
	public void noResults() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 10;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( new BigDecimal[lookback] );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );
		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalStore ).getStore( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	private BigDecimal[] createFlatValues( final int size, final int value ) {
		final BigDecimal[] values = new BigDecimal[size];

		for (int i = 0; i < values.length; i++) {
			values[i] = BigDecimal.valueOf( value );
		}

		return values;
	}

	private BigDecimal[] createIncreasingValues( final int size, final int startingValue ) {
		final BigDecimal[] values = new BigDecimal[size];

		for (int i = 0; i < values.length; i++) {
			values[i] = BigDecimal.valueOf( startingValue + i );
		}

		return values;
	}

	@Test
	public void signalLineInput() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 1 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValues( lookback, 2 ) );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );
		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );

		verify( signalEma ).ema( isBigDecimalArrayOf( BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 2 ),
				BigDecimal.valueOf( 3 ), BigDecimal.valueOf( 4 ), BigDecimal.valueOf( 5 ) ) );

		verify( signalStore ).getStore( data );
	}

	@Test
	public void signalLineInputFastEmaFirstNull() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 1 ) );

		final BigDecimal[] fastEmaValues = createIncreasingValues( lookback, 2 );
		fastEmaValues[0] = null;
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( fastEmaValues );

		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( new BigDecimal[lookback] );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );
		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );

		verify( signalEma ).ema( isBigDecimalArrayOf( BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 2 ),
				BigDecimal.valueOf( 3 ), BigDecimal.valueOf( 4 ), BigDecimal.valueOf( 5 ) ) );

		verify( signalStore ).getStore( data );
	}

	@Test
	public void noCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 1 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValues( lookback, 2 ) );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final BigDecimal[] signalEmaValues = createFlatValues( 5, 1 );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
		verify( signalStore ).getStore( data );
	}

	@Test
	public void bullishSignalLineCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 1 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValues( lookback, 2 ) );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final BigDecimal[] signalEmaValues = createFlatValues( 5, 2 );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 1, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
		verify( signalStore ).getStore( data );
	}

	@Test
	public void bullishOriginCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 0 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValues( lookback, -1 ) );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final BigDecimal[] signalEmaValues = createFlatValues( 5, 8 );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 1, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
		verify( signalStore ).getStore( data );
	}

	@Test
	public void bullishSignalLineAndOriginCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValues( lookback, 0 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValues( lookback, -1 ) );
		final BigDecimal[] signalValues = new BigDecimal[lookback];
		when( signalStore.getStore( any( TradingDayPrices[].class ) ) ).thenReturn( signalValues );

		final BigDecimal[] signalEmaValues = createFlatValues( 5, 1 );
		when( signalEma.ema( any( BigDecimal[].class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, signalStore );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 2, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma ).ema( signalValues );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
		verify( signalStore ).getStore( data );
	}

	private BigDecimal[] isBigDecimalArrayOf( final BigDecimal... bigDecimals ) {
		return argThat( new IsBigDecimalArray( bigDecimals ) );
	}

	class IsBigDecimalArray extends ArgumentMatcher<BigDecimal[]> {

		final BigDecimal[] expected;

		public IsBigDecimalArray( final BigDecimal... bigDecimals ) {
			this.expected = bigDecimals;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof BigDecimal[]) {

				final BigDecimal[] given = (BigDecimal[]) argument;

				for (int i = 0; i < expected.length; i++) {
					if (given[i] != null && given[i].compareTo( expected[i] ) != 0)
						return false;
				}

				return true;
			}

			return false;
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( "[" );
			for (int i = 0; i < expected.length; i++) {
				description.appendText( String.valueOf( expected[i] ) );

				if (i + 1 < expected.length) {
					description.appendText( "," );
				}
			}
			description.appendText( "]" );
		}
	}
}
