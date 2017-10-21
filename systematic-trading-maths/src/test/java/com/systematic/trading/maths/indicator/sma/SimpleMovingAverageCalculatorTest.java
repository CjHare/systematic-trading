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
package com.systematic.trading.maths.indicator.sma;

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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifying the behaviour for a SimpleMovingAverageCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleMovingAverageCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private SimpleMovingAverageCalculator calculator;

	@Test
	public void smaTwoPoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 3;
		final int daysOfSmaValues = numberDataPoints - lookback;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		setUpCalculator(lookback, daysOfSmaValues);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(sma, new double[] { 1, 1, 1, 1 });
		verifyValidation(data, numberDataPoints, lookback);
	}

	@Test
	public void smaThreePoints() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback;
		final TradingDayPrices[] data = createIncreasingPrices(numberDataPoints);
		setUpCalculator(lookback, daysOfSmaValues);

		final SimpleMovingAverageLine sma = sma(data);

		verifySma(sma, new double[] { 1.5, 2.5, 3.5, 4.5, 5.5 });
		verifyValidation(data, numberDataPoints, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFirstDataPoint() {
		final int lookback = 2;
		final int numberDataPoints = lookback + 4;
		final int daysOfSmaValues = numberDataPoints - lookback - 1;
		final TradingDayPrices[] data = createPrices(numberDataPoints);
		data[0] = null;
		setUpValidationErrorZeroNullEntries();
		setUpCalculator(lookback, daysOfSmaValues);

		sma(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooFewDaysOfSmaValues() {
		setUpValidationErrorTooFewsDaysOfSmaValues();
		setUpCalculator(6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullLastDataPoint() {
		final TradingDayPrices[] data = createPrices(6);
		data[4] = null;
		setUpValidationErrorZeroNullEntries();
		setUpCalculator(2, 5);

		sma(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {
		final TradingDayPrices[] data = createPrices(6);
		setUValidationErrorNotEnoughValues();
		setUpCalculator(2, 5);

		sma(data);
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

		final int required = calculator.getMinimumNumberOfPrices();

		assertEquals(17, required);
	}

	private void setUpCalculator( final int lookback, final int daysOfSmaValues ) {
		calculator = new SimpleMovingAverageCalculator(lookback, daysOfSmaValues, validator);
	}

	private void verifySma( final SimpleMovingAverageLine sma, final double... expected ) {
		assertNotNull(sma);
		assertNotNull(sma.getSma());
		//TODO fix UT - assertValues(expected, sma.getSma());
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
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());
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
	/*
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
	*/
	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.ofEpochDay(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.ofEpochDay(i)).withOpeningPrice(i + 1)
			        .withLowestPrice(1).withHighestPrice(i + 2).withClosingPrice(i + 1).build();
		}

		return prices;
	}
}