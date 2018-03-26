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
package com.systematic.trading.maths.indicator.sma;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.line;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.point;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
 * Verifying the behaviour for a SimpleMovingAverageCalculator implementation that uses the closing
 * price.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosingPriceSimpleMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private ClosingPriceSimpleMovingAverageCalculator calculator;

	@Test
	public void smaIncreasingValues() {

		final int lookback = 3;
		final TradingDayPrices[] data = createIncreasingPrices();
		setUpCalculator(lookback, 6);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(
		        sma,
		        line(
		                point(LocalDate.of(2017, 10, 4), 2.0),
		                point(LocalDate.of(2017, 10, 5), 3.0),
		                point(LocalDate.of(2017, 10, 6), 4.0),
		                point(LocalDate.of(2017, 10, 9), 5.0),
		                point(LocalDate.of(2017, 10, 10), 6.0),
		                point(LocalDate.of(2017, 10, 11), 7.0),
		                point(LocalDate.of(2017, 10, 12), 8.0),
		                point(LocalDate.of(2017, 10, 13), 9.0)));
		verifyValidation(data, 9, lookback);
	}

	@Test
	public void smaIntelExample() {

		final int lookback = 10;
		final TradingDayPrices[] data = createIntelExamplePrices();
		setUpCalculator(lookback, 4);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(
		        sma,
		        line(
		                point(LocalDate.of(2010, 4, 7), 22.22),
		                point(LocalDate.of(2010, 4, 8), 22.21),
		                point(LocalDate.of(2010, 4, 9), 22.23),
		                point(LocalDate.of(2010, 4, 12), 22.26),
		                point(LocalDate.of(2010, 4, 13), 22.31),
		                point(LocalDate.of(2010, 4, 14), 22.42),
		                point(LocalDate.of(2010, 4, 15), 22.61),
		                point(LocalDate.of(2010, 4, 16), 22.77),
		                point(LocalDate.of(2010, 4, 19), 22.91),
		                point(LocalDate.of(2010, 4, 20), 23.08),
		                point(LocalDate.of(2010, 4, 21), 23.21),
		                point(LocalDate.of(2010, 4, 22), 23.38),
		                point(LocalDate.of(2010, 4, 23), 23.53),
		                point(LocalDate.of(2010, 4, 26), 23.65),
		                point(LocalDate.of(2010, 4, 27), 23.71),
		                point(LocalDate.of(2010, 4, 28), 23.69),
		                point(LocalDate.of(2010, 4, 29), 23.61),
		                point(LocalDate.of(2010, 4, 30), 23.51),
		                point(LocalDate.of(2010, 5, 3), 23.43),
		                point(LocalDate.of(2010, 5, 4), 23.28),
		                point(LocalDate.of(2010, 5, 5), 23.13)));
		verifyValidation(data, 14, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooFewDaysOfSmaValues() {

		setUpValidationErrorTooFewsDaysOfSmaValues();
		setUpCalculator(6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nulEntries() {

		setUpValidationErrorZeroNullEntries();
		setUpCalculator(2, 5);

		sma(new TradingDayPrices[1]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {

		setUValidationErrorNotEnoughValues();
		setUpCalculator(2, 5);

		sma(new TradingDayPrices[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {

		setUpValidationErrorNullInput();
		setUpCalculator(1, 1);

		sma(null);
	}

	@Test
	public void requiredNumberOfTradingDays() {

		setUpCalculator(11, 6);

		final int required = calculator.minimumNumberOfPrices();

		assertEquals(17, required);
	}

	private void setUpCalculator( final int lookback, final int daysOfSmaValues ) {

		calculator = new ClosingPriceSimpleMovingAverageCalculator(lookback, daysOfSmaValues, validator);
	}

	private void verifySma( final SimpleMovingAverageLine sma, final SortedMap<LocalDate, BigDecimal> expected ) {

		assertNotNull(sma);
		assertNotNull(sma.sma());
		assertEquals(expected.size(), sma.sma().size());
		assertValues(expected, sma.sma());
	}

	private void verifyValidation( final TradingDayPrices[] data, final int numberDataPoints, final int lookback ) {

		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, numberDataPoints);
	}

	private void setUpValidationErrorZeroNullEntries() {

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void setUValidationErrorNotEnoughValues() {

		doThrow(new IllegalArgumentException()).when(validator)
		        .verifyEnoughValues(any(TradingDayPrices[].class), anyInt());
	}

	private void setUpValidationErrorTooFewsDaysOfSmaValues() {

		doThrow(new IllegalArgumentException()).when(validator).verifyGreaterThan(anyInt(), anyInt());
	}

	private SimpleMovingAverageLine sma( final TradingDayPrices[] data ) {

		return calculator.calculate(data);
	}

	private void setUpValidationErrorNullInput() {

		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	/**
	 * Thirty days of price data for Intel starting from LocalDate.of(2010, 3, 24).
	 */
	private TradingDayPrices[] createIntelExamplePrices() {

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

	/**
	 * Ten days of price data for prices starting from LocalDate.of(2017, 10, 2).
	 */
	private TradingDayPrices[] createIncreasingPrices() {

		final LocalDate[] dates = { LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 3), LocalDate.of(2017, 10, 4),
		        LocalDate.of(2017, 10, 5), LocalDate.of(2017, 10, 6), LocalDate.of(2017, 10, 9),
		        LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 12),
		        LocalDate.of(2017, 10, 13) };
		final double[] close = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

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
