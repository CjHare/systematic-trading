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
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.line;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.point;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;

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
	public void minimumNumberOfPrices() {
		setUpCalculator(2);

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals(3, requiredDays);
	}

	@Test
	public void atrFlat() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices();
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, line(point(LocalDate.of(2015, 7, 16), 2), point(LocalDate.of(2015, 7, 17), 2),
		        point(LocalDate.of(2015, 7, 20), 2), point(LocalDate.of(2015, 7, 21), 2)));
		verifyValidation(data, lookback);
	}

	@Test
	public void atrIncreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createIncreasingPrices();
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, line(point(LocalDate.of(2016, 11, 17), 9), point(LocalDate.of(2016, 11, 18), 12.75),
		        point(LocalDate.of(2016, 11, 21), 17.06)));
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrInitialNullEntry() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices();
		data[0] = null;
		setUpValidationErrorNoNullEntries();
		setUpCalculator(lookback);

		atr(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void atrLastNullEntry() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices();
		data[data.length - 1] = null;
		setUpValidationErrorNoNullEntries();
		setUpCalculator(lookback);

		atr(data);
	}

	@Test
	public void atrThreeRangeTypes() {
		final int lookback = 4;
		final TradingDayPrices[] data = createThreeTypesOfVolatility();
		setUpCalculator(lookback);

		final AverageTrueRangeLine atr = atr(data);

		verifyAtr(atr, line(point(LocalDate.of(2017, 7, 13), 0), point(LocalDate.of(2017, 7, 14), 16.78),
		        point(LocalDate.of(2017, 7, 17), 20.34), point(LocalDate.of(2017, 7, 18), 15.87)));
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

		verifyAtr(atr,
		        line(point(LocalDate.of(2010, 4, 21), 0.55), point(LocalDate.of(2010, 4, 22), 0.59),
		                point(LocalDate.of(2010, 4, 23), 0.59), point(LocalDate.of(2010, 4, 26), 0.57),
		                point(LocalDate.of(2010, 4, 27), 0.61), point(LocalDate.of(2010, 4, 28), 0.62),
		                point(LocalDate.of(2010, 4, 29), 0.64), point(LocalDate.of(2010, 4, 30), 0.67),
		                point(LocalDate.of(2010, 5, 3), 0.69), point(LocalDate.of(2010, 5, 4), 0.77),
		                point(LocalDate.of(2010, 5, 5), 0.78), point(LocalDate.of(2010, 5, 6), 1.21),
		                point(LocalDate.of(2010, 5, 7), 1.30), point(LocalDate.of(2010, 5, 10), 1.38),
		                point(LocalDate.of(2010, 5, 11), 1.37), point(LocalDate.of(2010, 5, 12), 1.34),
		                point(LocalDate.of(2010, 5, 13), 1.32)));
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

	private void verifyAtr( final AverageTrueRangeLine atr, final SortedMap<LocalDate, BigDecimal> expected ) {
		assertNotNull(atr);
		assertNotNull(atr.getAtr());
		assertEquals(expected.size(), atr.getAtr().size());
		assertValues(expected, atr.getAtr());
	}

	private void setUpCalculator( final int lookback ) {
		calculator = new AverageTrueRangeCalculator(lookback, validator);
	}

	/**
	 * Prices starting on LocalDate.of(2015, 7, 15).
	 * 
	 * ATR should always be two, as there are not changes in the price or ranges between days.
	 */
	private TradingDayPrices[] createPrices() {
		final LocalDate[] dates = { LocalDate.of(2015, 7, 15), LocalDate.of(2015, 7, 16), LocalDate.of(2015, 7, 17),
		        LocalDate.of(2015, 7, 20), LocalDate.of(2015, 7, 21) };
		final double[] high = { 2, 2, 2, 2, 2 };
		final double[] low = { 0, 0, 0, 0, 0 };
		final double[] close = { 1, 1, 1, 1, 1 };

		return createPrices(dates, high, low, close);
	}

	/**
	 * Prices starting on LocalDate.of(2016, 11, 14).
	 * 
	 * The closing price has linear increases, with the daily high increase being quadratic.
	 */
	private TradingDayPrices[] createIncreasingPrices() {
		final LocalDate[] dates = { LocalDate.of(2016, 11, 14), LocalDate.of(2016, 11, 15), LocalDate.of(2016, 11, 16),
		        LocalDate.of(2016, 11, 17), LocalDate.of(2016, 11, 18), LocalDate.of(2016, 11, 21) };
		final double[] high = { 0, 6, 12, 18, 24, 30 };
		final double[] low = { 0, 0, 0, 0, 0, 0, 0 };
		final double[] close = { 0, 1, 2, 3, 4, 5, 6 };

		return createPrices(dates, high, low, close);
	}

	/**
	 * Prices starting on LocalDate.of(2017, 7, 10).
	 * 
	 * Three cases for ATR:
	 *  - Biggest swing is between today's high & low
	 *	- Biggest swing is between the highest of today and yesterday's close
	 *	- Biggest swing is between the low of today and yesterday's close
	 */
	private TradingDayPrices[] createThreeTypesOfVolatility() {
		final LocalDate[] dates = { LocalDate.of(2017, 7, 10), LocalDate.of(2017, 7, 11), LocalDate.of(2017, 7, 12),
		        LocalDate.of(2017, 7, 13), LocalDate.of(2017, 7, 14), LocalDate.of(2017, 7, 17),
		        LocalDate.of(2017, 7, 18) };
		final double[] high = { 2.5, 2.5, 2.5, 2.5, 67.25, 33.5, 2.5 };
		final double[] low = { 2.5, 2.5, 2.5, 2.5, 0.13, 2.5, 0.01 };
		final double[] close = { 2.5, 2.5, 2.5, 2.5, 2.5, 2.5, 2.5 };

		return createPrices(dates, high, low, close);
	}

	/** 
	 * QQQQ (Powershares QQQ Trust), from 1 April 2010 - 13 May 2010
	 */
	private TradingDayPrices[] createExampleAverageTrueRange() {
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

		return createPrices(dates, high, low, close);
	}

	private TradingDayPrices[] createPrices( final LocalDate[] dates, final double[] high, final double[] low,
	        final double[] close ) {
		final TradingDayPrices[] data = new TradingDayPrices[dates.length];

		// Open values are not used in ATR calculations
		for (int i = 0; i < data.length; i++) {
			data[i] = new TradingDayPricesBuilder().withTradingDate(dates[i]).withLowestPrice(low[i])
			        .withHighestPrice(high[i]).withClosingPrice(close[i]).build();
		}

		return data;
	}
}