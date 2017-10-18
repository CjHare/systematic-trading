/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.maths.indicator.atr;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Tests the behaviour of the AverageTrueRangeCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class AverageTrueRangeCalculatorTest {

	@Mock
	private Validator validator;

	/** The calculator instance being tested. */
	private AverageTrueRangeCalculator calculator;

	@Test
	public void atrFlat() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(3);
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, 2, 2);
		verifyValidation(data, lookback);
	}

	@Test
	public void atrIncreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createIncreasingPrices(5);
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, 12.5, 15.62);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrInitialNullEntry() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(4);
		data[0] = null;
		setUpValidationErrorNoNullEntries();
		setUpCalculator(lookback);

		atr(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrLastNullEntry() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(4);
		data[data.length - 1] = null;
		setUpValidationErrorNoNullEntries();
		setUpCalculator(lookback);

		atr(data);
	}

	@Test
	public void atrThreeRangeTypes() {
		final int lookback = 4;
		final TradingDayPrices[] data = createThreeTypesOfVolatility(5);
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, 13.5, 18.88);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);

		atr(null);
	}

	@Test
	/** 
	 * QQQQ (Powershares QQQ Trust), from 1 April 2010 - 13 May 2010
	 */
	public void atrExample() {
		final int lookback = 14;
		final TradingDayPrices[] data = createExampleAverageTrueRange();
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, 0.55, 0.59, 0.59, 0.57, 0.61, 0.62, 0.64, 0.67, 0.69, 0.77, 0.78, 1.21, 1.30, 1.38, 1.37, 1.34,
		        1.32);
		verifyValidation(data, lookback);
	}

	private AverageTrueRangeLine atr( final TradingDayPrices[] data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorNoNullEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);
	}

	private void verifyAtr( final AverageTrueRangeLine atr, final double... expected ) {
		assertNotNull(atr);
		assertNotNull(atr.getAtr());
		assertEquals(expected.length, atr.getAtr().size());
		assertValues(expected, atr.getAtr());
	}

	private void setUpCalculator( final int lookback ) {
		calculator = new AverageTrueRangeCalculator(lookback, validator);
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i))
			        .withOpeningPrice(count).withLowestPrice(0).withHighestPrice(count + i * 5).withClosingPrice(1)
			        .build();
		}

		return prices;
	}

	private TradingDayPrices[] createThreeTypesOfVolatility( final int count ) {
		final TradingDayPrices[] prices = createIncreasingPrices(count);

		// Biggest swing is between today's high & low
		prices[count - 2] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(count - 3))
		        .withOpeningPrice(count).withLowestPrice(-5 * count).withHighestPrice(5 * count)
		        .withClosingPrice(count - 2).build();

		// Biggest swing is between the highest of today and yesterday's close
		prices[count - 2] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(count - 2))
		        .withOpeningPrice(count).withLowestPrice(count).withHighestPrice(5 * count).withClosingPrice(2 * count)
		        .build();

		// Biggest swing is between the low of today and yesterday's close
		prices[count - 1] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(count - 1))
		        .withOpeningPrice(count).withLowestPrice(-5 * count).withHighestPrice(count).withClosingPrice(count)
		        .build();

		return prices;
	}

	/** 
	 * QQQQ (Powershares QQQ Trust), from 1 April 2010 - 13 May 2010
	 */
	private TradingDayPrices[] createExampleAverageTrueRange() {
		final TradingDayPrices[] data = new TradingDayPrices[30];
		final LocalDate[] dates = { LocalDate.of(2010, 4, 1), LocalDate.of(2010, 4, 5), LocalDate.of(2010, 4, 6),
		        LocalDate.of(2010, 4, 7), LocalDate.of(2010, 4, 8), LocalDate.of(2010, 4, 9), LocalDate.of(2010, 4, 12),
		        LocalDate.of(2010, 4, 13), LocalDate.of(2010, 4, 14), LocalDate.of(2010, 4, 15),
		        LocalDate.of(2010, 4, 16), LocalDate.of(2010, 4, 19), LocalDate.of(2010, 4, 20),
		        LocalDate.of(2010, 4, 21), LocalDate.of(2010, 4, 22), LocalDate.of(2010, 4, 23),
		        LocalDate.of(2010, 4, 26), LocalDate.of(2010, 4, 27), LocalDate.of(2010, 4, 28),
		        LocalDate.of(2010, 4, 29), LocalDate.of(2010, 4, 30), LocalDate.of(2010, 5, 3),
		        LocalDate.of(2010, 5, 4), LocalDate.of(2010, 5, 5), LocalDate.of(2010, 5, 6), LocalDate.of(2010, 5, 7),
		        LocalDate.of(2010, 5, 10), LocalDate.of(2010, 5, 11), LocalDate.of(2010, 5, 12),
		        LocalDate.of(2010, 5, 13) };
		final double[] high = { 48.70, 48.72, 48.90, 48.87, 48.82, 49.05, 49.20, 49.35, 49.92, 50.19, 50.12, 49.66,
		        49.88, 50.19, 50.36, 50.57, 50.65, 50.43, 49.63, 50.33, 50.29, 50.17, 49.32, 48.50, 48.32, 46.80, 47.80,
		        48.39, 48.66, 48.79 };
		final double[] low = { 47.79, 48.14, 48.39, 48.37, 48.24, 48.64, 48.94, 48.86, 49.50, 49.87, 49.20, 48.90,
		        49.43, 49.73, 49.26, 50.09, 50.30, 49.21, 48.98, 49.61, 49.20, 49.43, 48.08, 47.64, 41.55, 44.28, 47.31,
		        47.20, 47.90, 47.73 };
		final double[] close = { 48.16, 48.61, 48.75, 48.63, 48.74, 49.03, 49.07, 49.32, 49.91, 50.13, 49.53, 49.50,
		        49.75, 50.03, 50.31, 50.52, 50.41, 49.43, 49.37, 50.23, 49.24, 49.93, 48.43, 48.18, 46.57, 45.41, 47.77,
		        47.72, 48.62, 47.85 };

		// Open values are not used in ATR calculations
		for (int i = 0; i < data.length; i++) {
			data[i] = new TradingDayPricesBuilder().withTradingDate(dates[i]).withLowestPrice(low[i])
			        .withHighestPrice(high[i]).withClosingPrice(close[i]).build();
		}

		return data;
	}
}