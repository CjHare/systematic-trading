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
package com.systematic.trading.maths.indicator;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;

/**
 * Verify the behaviour of the IndicatorInputValidator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class IndicatorInputValidatorTest {

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( 1 ), BigDecimal.valueOf( 0 ),
					BigDecimal.valueOf( 2 ), BigDecimal.valueOf( 1 ) );
		}

		return prices;
	}

	private BigDecimal[] createDecimals( final int count ) {
		final BigDecimal[] prices = new BigDecimal[count];

		for (int i = 0; i < count; i++) {
			prices[i] = BigDecimal.valueOf( count );
		}

		return prices;
	}

	@Test(expected = IllegalArgumentException.class)
	public void fewerDataPointsThenLookback() {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( lookback );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, lookback + 1 );
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedParameterLengths() {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices( lookback );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, lookback + 1 );
	}

	@Test(expected = IllegalArgumentException.class)
	public void allNull() {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final TradingDayPrices[] data = new TradingDayPrices[numberDataPoints];

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, numberDataPoints );
	}

	@Test
	public void noNullValues() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints + 1 );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getStartingNonNullIndex( data, numberDataPoints );

		assertEquals( 0, index );
	}

	@Test
	public void firstValueIsNull() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints + 1 );
		data[0] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getStartingNonNullIndex( data, numberDataPoints );

		assertEquals( 1, index );
	}

	@Test
	public void lastValueIsNull() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints + 1 );
		data[numberDataPoints] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getStartingNonNullIndex( data, numberDataPoints );

		assertEquals( 0, index );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesMid() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints + 1 );
		data[3] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesEnd() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[numberDataPoints - 1] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesStart() {
		final int numberDataPoints = 5;
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getStartingNonNullIndex( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void fewerDataPointsThenLookbackDecimals() {
		final int lookback = 4;
		final BigDecimal[] data = createDecimals( lookback );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, lookback + 1 );
	}

	@Test(expected = IllegalArgumentException.class)
	public void mismatchedParameterLengthsDecimals() {
		final int lookback = 4;
		final BigDecimal[] data = createDecimals( lookback );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, lookback + 1 );
	}

	@Test(expected = IllegalArgumentException.class)
	public void allNullDecimals() {
		final int lookback = 4;
		final int numberDataPoints = lookback + 1;
		final BigDecimal[] data = new BigDecimal[numberDataPoints];

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, numberDataPoints );
	}

	@Test
	public void noNullValuesDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints );

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getFirstNonNullIndex( data, numberDataPoints );

		assertEquals( 0, index );
	}

	@Test
	public void firstValueIsNullDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints + 1 );
		data[0] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getFirstNonNullIndex( data, numberDataPoints );

		assertEquals( 1, index );
	}

	@Test
	public void lastValueIsNullDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints + 1 );
		data[numberDataPoints] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		final int index = validator.getFirstNonNullIndex( data, numberDataPoints );

		assertEquals( 0, index );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesMidDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints + 1 );
		data[3] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesEndDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints );
		data[numberDataPoints - 1] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, numberDataPoints );
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughConsectiveValuesStartDecimals() {
		final int numberDataPoints = 5;
		final BigDecimal[] data = createDecimals( numberDataPoints );
		data[0] = null;

		final IndicatorInputValidator validator = new IndicatorInputValidator();

		validator.getFirstNonNullIndex( data, numberDataPoints );
	}

	@Test
	public void getFirstNonNullIndexDecimals() {
		final int numberDataPoints = 5;
		final IndicatorInputValidator validator = new IndicatorInputValidator();
		final BigDecimal[] data = createDecimals( numberDataPoints );
		data[0] = null;

		final int index = validator.getFirstNonNullIndex( data );

		assertEquals( 1, index );
	}

	@Test
	public void getFirstNonNullIndex() {
		final int numberDataPoints = 5;
		final IndicatorInputValidator validator = new IndicatorInputValidator();
		final TradingDayPrices[] data = createPrices( numberDataPoints );
		data[0] = null;

		final int index = validator.getFirstNonNullIndex( data );

		assertEquals( 1, index );
	}
}
