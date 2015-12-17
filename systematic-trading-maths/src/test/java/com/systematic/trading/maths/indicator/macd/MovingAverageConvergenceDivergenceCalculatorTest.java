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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
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
	private IndicatorInputValidator validator;

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now().plusDays( i ), BigDecimal.valueOf( 1 ),
					BigDecimal.valueOf( 0 ), BigDecimal.valueOf( 2 ), BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

	@Test
	public void noResults() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 10;
		final TradingDayPrices[] data = new TradingDayPrices[lookback];

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList( lookback, 2 );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( fastEmaValues );

		final List<BigDecimal> slowEmaValues = createFlatValuesList( lookback, 0 );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( slowEmaValues );

		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( new ArrayList<BigDecimal>( lookback ) );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );
		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	private List<BigDecimal> createFlatValuesList( final int size, final int value ) {
		final List<BigDecimal> values = new ArrayList<BigDecimal>( size );

		for (int i = 0; i < size; i++) {
			values.add( BigDecimal.valueOf( value ) );
		}

		return values;
	}

	private List<BigDecimal> createIncreasingValuesList( final int size, final int startingValue ) {
		final List<BigDecimal> values = new ArrayList<BigDecimal>( size );

		for (int i = 0; i < size; i++) {
			values.add( BigDecimal.valueOf( startingValue + i ) );
		}

		return values;
	}

	@Test
	public void signalLineInput() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValuesList( lookback, 1 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValuesList( lookback, 2 ) );
		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( new ArrayList<BigDecimal>( lookback ) );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );
		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );

		verify( signalEma ).ema( isBigDecimalListOf( BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 2 ),
				BigDecimal.valueOf( 3 ), BigDecimal.valueOf( 4 ), BigDecimal.valueOf( 5 ) ) );
	}

	@Test
	public void noCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createFlatValuesList( lookback, 1 ) );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( createIncreasingValuesList( lookback, 2 ) );

		final List<BigDecimal> signalEmaValues = createFlatValuesList( 5, 1 );
		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 0, signals.size() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	@Test
	public void bullishSignalLineCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );

		final List<BigDecimal> slowEmaValues = createFlatValuesList( lookback, 1 );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( slowEmaValues );

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList( lookback, 2 );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( fastEmaValues );

		final List<BigDecimal> signalEmaValues = createFlatValuesList( 5, 2 );
		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( signalEmaValues );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 1, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	@Test
	public void bullishOriginCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );

		final List<BigDecimal> slowEmaValues = createFlatValuesList( lookback, 0 );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( slowEmaValues );

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList( lookback, -1 );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( fastEmaValues );

		final List<BigDecimal> signalEmaValues = createFlatValuesList( 5, 8 );
		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( signalEmaValues );
		when( validator.getLastNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( data.length - 1 );
		when( validator.getFirstNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( 0 );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 1, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	@Test
	public void bullishSignalLineAndOriginCrossover() throws TooFewDataPoints, TooManyDataPoints {
		final int lookback = 5;
		final TradingDayPrices[] data = createPrices( lookback );

		final List<BigDecimal> slowEmaValues = createFlatValuesList( lookback, 0 );
		when( slowEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( slowEmaValues );

		final List<BigDecimal> fastEmaValues = createIncreasingValuesList( lookback, -1 );
		when( fastEma.ema( any( TradingDayPrices[].class ) ) ).thenReturn( fastEmaValues );

		final List<BigDecimal> signalEmaValues = createFlatValuesList( 5, 1 );
		when( signalEma.ema( anyListOf( BigDecimal.class ) ) ).thenReturn( signalEmaValues );
		when( validator.getLastNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( data.length - 1 );
		when( validator.getFirstNonNullIndex( anyListOf( BigDecimal.class ) ) ).thenReturn( 0 );

		final MovingAverageConvergenceDivergenceCalculator calculator = new MovingAverageConvergenceDivergenceCalculator(
				fastEma, slowEma, signalEma, validator );

		final List<DatedSignal> signals = calculator.macd( data );

		assertNotNull( signals );
		assertEquals( 2, signals.size() );
		assertEquals( SignalType.BULLISH, signals.get( 0 ).getType() );
		assertEquals( LocalDate.now().plusDays( 1 ), signals.get( 0 ).getDate() );

		verify( fastEma ).ema( data );
		verify( slowEma ).ema( data );
		verify( signalEma, never() ).ema( any( TradingDayPrices[].class ) );
	}

	private List<BigDecimal> isBigDecimalListOf( final BigDecimal... bigDecimals ) {
		return argThat( new IsBigDecimalList( bigDecimals ) );
	}

	class IsBigDecimalList extends ArgumentMatcher<List<BigDecimal>> {

		final BigDecimal[] expected;

		public IsBigDecimalList( final BigDecimal... bigDecimals ) {
			this.expected = bigDecimals;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof List<?>) {

				@SuppressWarnings("unchecked")
				final List<BigDecimal> given = (List<BigDecimal>) argument;

				for (int i = 0; i < expected.length; i++) {
					if (given.get( i ) != null && given.get( i ).compareTo( expected[i] ) != 0)
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
