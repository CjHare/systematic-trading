/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.indicator.Validator;

/**
 * Test the ExponentialMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ExponentialMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private ExponentialMovingAverage calculator;

	@Test(expected = IllegalArgumentException.class)
	public void invalidLookback() {

		setUpValidationErrorGreaterThan();
		setUpCalculator(0);
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
		final SortedMap<LocalDate, BigDecimal> data = new TreeMap<LocalDate, BigDecimal>();
		data.put(LocalDate.now(), null);

		ema(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNotEnoughValues() {

		setUpValidationErrorNullInput();
		setUpCalculator(1);

		ema(new TreeMap<LocalDate, BigDecimal>());
	}

	@Test
	public void emaIntelExample() {

		final int lookback = 10;
		final SortedMap<LocalDate, BigDecimal> data = createIntelExamplePrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(
		        ema,
		        line(
		                point(LocalDate.of(2010, 4, 7), 22.22),
		                point(LocalDate.of(2010, 4, 8), 22.21),
		                point(LocalDate.of(2010, 4, 9), 22.24),
		                point(LocalDate.of(2010, 4, 12), 22.27),
		                point(LocalDate.of(2010, 4, 13), 22.33),
		                point(LocalDate.of(2010, 4, 14), 22.52),
		                point(LocalDate.of(2010, 4, 15), 22.80),
		                point(LocalDate.of(2010, 4, 16), 22.97),
		                point(LocalDate.of(2010, 4, 19), 23.13),
		                point(LocalDate.of(2010, 4, 20), 23.28),
		                point(LocalDate.of(2010, 4, 21), 23.34),
		                point(LocalDate.of(2010, 4, 22), 23.43),
		                point(LocalDate.of(2010, 4, 23), 23.51),
		                point(LocalDate.of(2010, 4, 26), 23.54),
		                point(LocalDate.of(2010, 4, 27), 23.47),
		                point(LocalDate.of(2010, 4, 28), 23.40),
		                point(LocalDate.of(2010, 4, 29), 23.39),
		                point(LocalDate.of(2010, 4, 30), 23.26),
		                point(LocalDate.of(2010, 5, 3), 23.23),
		                point(LocalDate.of(2010, 5, 4), 23.08),
		                point(LocalDate.of(2010, 5, 5), 22.92)));
		verifyValidation(data, lookback);
	}

	@Test
	public void emaIncreasing() {

		final int lookback = 5;
		final SortedMap<LocalDate, BigDecimal> data = createIncreasingPrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(
		        ema,
		        line(
		                point(LocalDate.of(2017, 9, 15), 2),
		                point(LocalDate.of(2017, 9, 18), 3),
		                point(LocalDate.of(2017, 9, 19), 4),
		                point(LocalDate.of(2017, 9, 20), 5),
		                point(LocalDate.of(2017, 9, 21), 6),
		                point(LocalDate.of(2017, 9, 22), 7)));
		verifyValidation(data, lookback);
	}

	@Test
	public void emaFlat() {

		final int lookback = 4;
		final SortedMap<LocalDate, BigDecimal> data = createFlatPrices();
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(
		        ema,
		        line(
		                point(LocalDate.of(2017, 10, 12), 4.5),
		                point(LocalDate.of(2017, 10, 13), 4.5),
		                point(LocalDate.of(2017, 10, 16), 4.5),
		                point(LocalDate.of(2017, 10, 17), 4.5),
		                point(LocalDate.of(2017, 10, 18), 4.5)));
		verifyValidation(data, lookback);
	}

	private ExponentialMovingAverageLine ema( final SortedMap<LocalDate, BigDecimal> data ) {

		return calculator.calculate(data);
	}

	private void setUpValidationErrorGreaterThan() {

		doThrow(new IllegalArgumentException()).when(validator).verifyGreaterThan(anyInt(), anyInt());
	}

	private void setUpValidationErrorNullInput() {

		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	@SuppressWarnings("unchecked")
	private void setUpValidationErrorNullEntries() {

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(anyCollection());
	}

	private void setUpCalculator( final int lookback ) {

		calculator = new ExponentialMovingAverageCalculator(lookback, validator);
	}

	private void verifyValidation( final SortedMap<LocalDate, BigDecimal> data, final int lookback ) {

		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data.values(), lookback);
		verify(validator).verifyZeroNullEntries(data.values());
	}

	private void verifyEma(
	        final ExponentialMovingAverageLine actual,
	        final SortedMap<LocalDate, BigDecimal> expected ) {

		assertNotNull(actual);
		assertNotNull(actual.ema());
		assertEquals(expected.size(), actual.ema().size());
		assertValues(expected, actual.ema());

	}

	/**
	 * Flat prices starting from LocalDate.of(2017, 10, 9).
	 */
	private SortedMap<LocalDate, BigDecimal> createFlatPrices() {

		final LocalDate[] dates = { LocalDate.of(2017, 10, 9), LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11),
		        LocalDate.of(2017, 10, 12), LocalDate.of(2017, 10, 13), LocalDate.of(2017, 10, 16),
		        LocalDate.of(2017, 10, 17), LocalDate.of(2017, 10, 18) };
		final double[] close = { 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5, 4.5 };

		return createPrices(dates, close);
	}

	/**
	 * Increasing prices starting from LocalDate.of(2017, 9, 11).
	 */
	private SortedMap<LocalDate, BigDecimal> createIncreasingPrices() {

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
	private SortedMap<LocalDate, BigDecimal> createIntelExamplePrices() {

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
		final double[] close = { 22.2734, 22.194, 22.0847, 22.1741, 22.184, 22.1344, 22.2337, 22.4323, 22.2436, 22.2933,
		        22.1542, 22.3926, 22.3816, 22.6109, 23.3558, 24.0519, 23.753, 23.8324, 23.9516, 23.6338, 23.8225,
		        23.8722, 23.6537, 23.187, 23.0976, 23.326, 22.6805, 23.0976, 22.4025, 22.1725 };

		return createPrices(dates, close);
	}

	private final SortedMap<LocalDate, BigDecimal> createPrices( final LocalDate[] dates, final double[] close ) {

		final SortedMap<LocalDate, BigDecimal> data = new TreeMap<LocalDate, BigDecimal>();

		for (int i = 0; i < dates.length; i++) {
			data.put(dates[i], BigDecimal.valueOf(close[i]));
		}

		return data;
	}
}
