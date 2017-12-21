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
package com.systematic.trading.maths.indicator.ema;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.line;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.point;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * Test the ExponentialMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosingPriceExponentialMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private ExponentialMovingAverageIndicator calculator;

	@Test(expected = IllegalArgumentException.class)
	public void invalidLookback() {

		setUpValidationErrorGreaterThan();
		setUpCalculator(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidDaysOfEmaValues() {

		setUpValidationErrorGreaterThan();
		setUpCalculator(1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {

		setUpValidationErrorNullInput();
		setUpCalculator(1);

		ema(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullEntries() {

		setUpValidationErrorNullEntries();
		setUpCalculator(2);

		ema(new TradingDayPrices[] { null });
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNotEnoughValues() {

		setUpValidationErrorNullInput();
		setUpCalculator(1);

		ema(new TradingDayPrices[] {});
	}

	@Test
	public void emaIntelExample() {

		final int lookback = 10;
		final TradingDayPrices[] data = createExamplePrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema,
		        line(point(LocalDate.of(2010, 4, 7), 22.22), point(LocalDate.of(2010, 4, 8), 22.21),
		                point(LocalDate.of(2010, 4, 9), 22.24), point(LocalDate.of(2010, 4, 12), 22.27),
		                point(LocalDate.of(2010, 4, 13), 22.33), point(LocalDate.of(2010, 4, 14), 22.52),
		                point(LocalDate.of(2010, 4, 15), 22.80), point(LocalDate.of(2010, 4, 16), 22.97),
		                point(LocalDate.of(2010, 4, 19), 23.13), point(LocalDate.of(2010, 4, 20), 23.28),
		                point(LocalDate.of(2010, 4, 21), 23.34), point(LocalDate.of(2010, 4, 22), 23.43),
		                point(LocalDate.of(2010, 4, 23), 23.51), point(LocalDate.of(2010, 4, 26), 23.53),
		                point(LocalDate.of(2010, 4, 27), 23.47), point(LocalDate.of(2010, 4, 28), 23.40),
		                point(LocalDate.of(2010, 4, 29), 23.39), point(LocalDate.of(2010, 4, 30), 23.26),
		                point(LocalDate.of(2010, 5, 3), 23.23), point(LocalDate.of(2010, 5, 4), 23.08),
		                point(LocalDate.of(2010, 5, 5), 22.92)));
		verifyValidation(data, lookback);
	}

	/**
	 * The additional days of EMA values should have no affect on the calculations, only on the
	 * minimum data set size.
	 */
	@Test
	public void emaIntelExampleAdditionalDaysofEmaValues() {

		final int lookback = 10;
		final TradingDayPrices[] data = createExamplePrices();
		setUpCalculator(lookback, 5);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema,
		        line(point(LocalDate.of(2010, 4, 7), 22.22), point(LocalDate.of(2010, 4, 8), 22.21),
		                point(LocalDate.of(2010, 4, 9), 22.24), point(LocalDate.of(2010, 4, 12), 22.27),
		                point(LocalDate.of(2010, 4, 13), 22.33), point(LocalDate.of(2010, 4, 14), 22.52),
		                point(LocalDate.of(2010, 4, 15), 22.80), point(LocalDate.of(2010, 4, 16), 22.97),
		                point(LocalDate.of(2010, 4, 19), 23.13), point(LocalDate.of(2010, 4, 20), 23.28),
		                point(LocalDate.of(2010, 4, 21), 23.34), point(LocalDate.of(2010, 4, 22), 23.43),
		                point(LocalDate.of(2010, 4, 23), 23.51), point(LocalDate.of(2010, 4, 26), 23.53),
		                point(LocalDate.of(2010, 4, 27), 23.47), point(LocalDate.of(2010, 4, 28), 23.40),
		                point(LocalDate.of(2010, 4, 29), 23.39), point(LocalDate.of(2010, 4, 30), 23.26),
		                point(LocalDate.of(2010, 5, 3), 23.23), point(LocalDate.of(2010, 5, 4), 23.08),
		                point(LocalDate.of(2010, 5, 5), 22.92)));
		verifyValidation(data, lookback);
	}

	@Test
	public void additionalDaysOfEmaValues() {

		setUpCalculator(10, 5);

		final int requiredDays = calculator.minimumNumberOfPrices();

		assertEquals(15, requiredDays);
	}

	@Test
	public void emaIncreasing() {

		final int lookback = 5;
		final TradingDayPrices[] data = createIncreasingPrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema,
		        line(point(LocalDate.of(2017, 9, 15), 2), point(LocalDate.of(2017, 9, 18), 3),
		                point(LocalDate.of(2017, 9, 19), 4), point(LocalDate.of(2017, 9, 20), 5),
		                point(LocalDate.of(2017, 9, 21), 6), point(LocalDate.of(2017, 9, 22), 7)));
		verifyValidation(data, lookback);
	}

	@Test
	public void emaFlat() {

		final int lookback = 4;
		final TradingDayPrices[] data = createFlatPrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema,
		        line(point(LocalDate.of(2017, 10, 12), 4.5), point(LocalDate.of(2017, 10, 13), 4.5),
		                point(LocalDate.of(2017, 10, 16), 4.5), point(LocalDate.of(2017, 10, 17), 4.5),
		                point(LocalDate.of(2017, 10, 18), 4.5)));
		verifyValidation(data, lookback);
	}

	/**
	 * Default of a single EMA value in additional to the lookback
	 */
	@Test
	public void minimumNumberOfPrices() {

		setUpCalculator(4);

		final int requiredDays = calculator.minimumNumberOfPrices();

		assertEquals(5, requiredDays);
	}

	private ExponentialMovingAverageLine ema( final TradingDayPrices[] data ) {

		return calculator.calculate(data);
	}

	private void setUpValidationErrorGreaterThan() {

		doThrow(new IllegalArgumentException()).when(validator).verifyGreaterThan(anyInt(), eq(0));
	}

	private void setUpValidationErrorNullInput() {

		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void setUpValidationErrorNullEntries() {

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void verifyEma( final ExponentialMovingAverageLine actual,
	        final SortedMap<LocalDate, BigDecimal> expected ) {

		assertNotNull(actual);
		assertNotNull(actual.ema());
		assertEquals(expected.size(), actual.ema().size());
		assertValues(expected, actual.ema());

	}

	private void setUpCalculator( final int lookback ) {

		calculator = new ClosingPriceExponentialMovingAverageCalculator(lookback, 1, validator);
	}

	private void setUpCalculator( final int lookback, final int daysOfEmaValues ) {

		calculator = new ClosingPriceExponentialMovingAverageCalculator(lookback, daysOfEmaValues, validator);
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {

		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	/**
	 * Flat prices starting from LocalDate.of(2017, 10, 9).
	 */
	private TradingDayPrices[] createFlatPrices() {

		final LocalDate[] dates = { LocalDate.of(2017, 10, 9), LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11),
		        LocalDate.of(2017, 10, 12), LocalDate.of(2017, 10, 13), LocalDate.of(2017, 10, 16),
		        LocalDate.of(2017, 10, 17), LocalDate.of(2017, 10, 18) };
		final double[] close = { 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5 };

		return createPrices(dates, close);
	}

	/**
	 * Increasing prices starting from LocalDate.of(2017, 9, 11).
	 */
	private TradingDayPrices[] createIncreasingPrices() {

		final LocalDate[] dates = { LocalDate.of(2017, 9, 11), LocalDate.of(2017, 9, 12), LocalDate.of(2017, 9, 13),
		        LocalDate.of(2017, 9, 14), LocalDate.of(2017, 9, 15), LocalDate.of(2017, 9, 18),
		        LocalDate.of(2017, 9, 19), LocalDate.of(2017, 9, 20), LocalDate.of(2017, 9, 21),
		        LocalDate.of(2017, 9, 22) };
		final double[] close = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

		return createPrices(dates, close);
	}

	/**
	 * Thirty days of price data for Intel starting from LocalDate.of(2010, 3, 24).
	 */
	private TradingDayPrices[] createExamplePrices() {

		final LocalDate[] dates = { LocalDate.of(2010, 3, 24), LocalDate.of(2010, 3, 25), LocalDate.of(2010, 3, 26),
		        LocalDate.of(2010, 3, 29), LocalDate.of(2010, 3, 30), LocalDate.of(2010, 3, 31),
		        LocalDate.of(2010, 4, 1), LocalDate.of(2010, 4, 5), LocalDate.of(2010, 4, 6), LocalDate.of(2010, 4, 7),
		        LocalDate.of(2010, 4, 8), LocalDate.of(2010, 4, 9), LocalDate.of(2010, 4, 12),
		        LocalDate.of(2010, 4, 13), LocalDate.of(2010, 4, 14), LocalDate.of(2010, 4, 15),
		        LocalDate.of(2010, 4, 16), LocalDate.of(2010, 4, 19), LocalDate.of(2010, 4, 20),
		        LocalDate.of(2010, 4, 21), LocalDate.of(2010, 4, 22), LocalDate.of(2010, 4, 23),
		        LocalDate.of(2010, 4, 26), LocalDate.of(2010, 4, 27), LocalDate.of(2010, 4, 28),
		        LocalDate.of(2010, 4, 29), LocalDate.of(2010, 4, 30), LocalDate.of(2010, 5, 3),
		        LocalDate.of(2010, 5, 4), LocalDate.of(2010, 5, 5) };
		final double[] close = { 22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39,
		        22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82, 23.87, 23.65, 23.19, 23.10, 23.33, 22.68,
		        23.10, 22.40, 22.17 };

		return createPrices(dates, close);
	}

	private TradingDayPrices[] createPrices( final LocalDate[] dates, final double[] close ) {

		final TradingDayPrices[] data = new TradingDayPrices[dates.length];

		// Only the close price is used in the EMA calculation
		for (int i = 0; i < data.length; i++) {
			data[i] = new TradingDayPricesBuilder().withTradingDate(dates[i]).withClosingPrice(close[i]).build();
		}

		return data;
	}
}