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
package com.systematic.trading.maths.indicator.rs;

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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifies the behaviour of RelativeStrengthCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosingPriceRelativeStrengthCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private ClosingPriceRelativeStrengthCalculator calculator;

	@Test
	public void rsIncreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createIncreasingPrices();
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs,
		        line(point(LocalDate.of(2017, 9, 22), 0.25), point(LocalDate.of(2017, 9, 25), 0.44),
		                point(LocalDate.of(2017, 9, 26), 0.58), point(LocalDate.of(2017, 9, 27), 0.68),
		                point(LocalDate.of(2017, 9, 28), 0.76), point(LocalDate.of(2017, 9, 29), 0.82)));
		verifyValidation(data, lookback);
	}

	@Test
	/**
	 * We only get an RS value AFTER there is at least one gain.
	 */
	public void rsDecreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createDecreasingPrices();
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs,
		        line(point(LocalDate.of(2017, 9, 22), 0), point(LocalDate.of(2017, 9, 25), 0),
		                point(LocalDate.of(2017, 9, 26), 0), point(LocalDate.of(2017, 9, 27), 0),
		                point(LocalDate.of(2017, 9, 28), 0), point(LocalDate.of(2017, 9, 29), 0)));
		verifyValidation(data, lookback);
	}

	@Test
	public void rsFlat() {
		final int lookback = 5;
		final TradingDayPrices[] data = createFlatPrices();
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs,
		        line(point(LocalDate.of(2017, 7, 17), 0), point(LocalDate.of(2017, 7, 18), 0),
		                point(LocalDate.of(2017, 7, 19), 0), point(LocalDate.of(2017, 7, 20), 0),
		                point(LocalDate.of(2017, 7, 21), 0)));
		verifyValidation(data, lookback);
	}

	@Test
	public void rsIncreasingThenDecreasing() {
		final int lookback = 6;
		setUpCalculator(lookback);
		final TradingDayPrices[] data = createIncreasingThenDecreasingPrices();

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs,
		        line(point(LocalDate.of(2017, 10, 10), 0), point(LocalDate.of(2017, 10, 11), 0.25),
		                point(LocalDate.of(2017, 10, 12), 0.29), point(LocalDate.of(2017, 10, 13), 0.70),
		                point(LocalDate.of(2017, 10, 16), 1.46), point(LocalDate.of(2017, 10, 17), 1.22),
		                point(LocalDate.of(2017, 10, 18), 1.69), point(LocalDate.of(2017, 10, 19), 1.13),
		                point(LocalDate.of(2017, 10, 20), 0.56)));

		verifyValidation(data, lookback);
	}

	@Test
	/*
	 * Powershares QQQ Trust prices from 14 Dec 2009 to 1 Feb 2010.
	 */
	public void rsExample() {
		final int lookback = 14;
		final TradingDayPrices[] data = createExampleRelativeStrength();
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs,
		        line(point(LocalDate.of(2010, 1, 5), 2.39), point(LocalDate.of(2010, 1, 6), 1.94),
		                point(LocalDate.of(2010, 1, 7), 1.96), point(LocalDate.of(2010, 1, 8), 2.26),
		                point(LocalDate.of(2010, 1, 11), 1.95), point(LocalDate.of(2010, 1, 12), 1.34),
		                point(LocalDate.of(2010, 1, 13), 1.67), point(LocalDate.of(2010, 1, 14), 1.70),
		                point(LocalDate.of(2010, 1, 15), 1.25), point(LocalDate.of(2010, 1, 19), 1.64),
		                point(LocalDate.of(2010, 1, 20), 1.18), point(LocalDate.of(2010, 1, 21), 0.99),
		                point(LocalDate.of(2010, 1, 22), 0.65), point(LocalDate.of(2010, 1, 25), 0.69),
		                point(LocalDate.of(2010, 1, 26), 0.70), point(LocalDate.of(2010, 1, 27), 0.82),
		                point(LocalDate.of(2010, 1, 28), 0.58), point(LocalDate.of(2010, 1, 29), 0.48),
		                point(LocalDate.of(2010, 2, 1), 0.60)));
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rsNullEntires() {
		setUpValidationErrorNullEntries();
		setUpCalculator(1);

		rs(new TradingDayPrices[1]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rsNotEnoughDataPoints() {
		setUpValidationErrorEnoughValues();
		setUpCalculator(4);

		rs(new TradingDayPrices[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rsNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);

		rs(null);
	}

	@Test
	public void minimumNumberOfPrices() {
		setUpCalculator(14, 2);

		final int minimumNumberOfPrices = calculator.minimumNumberOfPrices();

		assertEquals(16, minimumNumberOfPrices);
	}

	private RelativeStrengthLine rs( final TradingDayPrices[] data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorEnoughValues() {
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());
	}

	private void setUpValidationErrorNullEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void setUpCalculator( final int lookback ) {
		setUpCalculator(lookback, 0);
	}

	private void setUpCalculator( final int lookback, final int additionalRsiValues ) {
		calculator = new ClosingPriceRelativeStrengthCalculator(lookback, additionalRsiValues, validator);
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);
	}

	private void verifyRs( final RelativeStrengthLine rs, final SortedMap<LocalDate, BigDecimal> expected ) {
		assertNotNull(rs);
		assertNotNull(rs.rs());
		assertEquals(expected.size(), rs.rs().size());
		assertValues(expected, rs.rs());
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	/**
	 * Fifteen days of prices, with the price increase then decreasing after the seventh day, starting at LocalDate.of(2017, 10, 2)
	 */
	private TradingDayPrices[] createIncreasingThenDecreasingPrices() {
		final LocalDate[] dates = { LocalDate.of(2017, 10, 2), LocalDate.of(2017, 10, 3), LocalDate.of(2017, 10, 4),
		        LocalDate.of(2017, 10, 5), LocalDate.of(2017, 10, 6), LocalDate.of(2017, 10, 9),
		        LocalDate.of(2017, 10, 10), LocalDate.of(2017, 10, 11), LocalDate.of(2017, 10, 12),
		        LocalDate.of(2017, 10, 13), LocalDate.of(2017, 10, 16), LocalDate.of(2017, 10, 17),
		        LocalDate.of(2017, 10, 18), LocalDate.of(2017, 10, 19), LocalDate.of(2017, 10, 20) };
		final double[] close = { 10, 10, 10, 10, 10, 10, 10, 11.5, 12, 14.75, 20, 20, 16.4, 14.9, 11.05 };

		return createPrices(dates, close);
	}

	/**
	 * Ten days of prices containing no change in the price, starting at LocalDate.of(2017, 7, 10)
	 */
	private TradingDayPrices[] createFlatPrices() {
		final LocalDate[] dates = { LocalDate.of(2017, 7, 10), LocalDate.of(2017, 7, 11), LocalDate.of(2017, 7, 12),
		        LocalDate.of(2017, 7, 13), LocalDate.of(2017, 7, 14), LocalDate.of(2017, 7, 17),
		        LocalDate.of(2017, 7, 18), LocalDate.of(2017, 7, 19), LocalDate.of(2017, 7, 20),
		        LocalDate.of(2017, 7, 21) };
		final double[] close = { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };

		return createPrices(dates, close);
	}

	/** 
	 * Ten closing prices that start increasing after the forth entry, starting at LocalDate.of(2017, 9, 18).
	 */
	private TradingDayPrices[] createIncreasingPrices() {
		final LocalDate[] dates = { LocalDate.of(2017, 9, 18), LocalDate.of(2017, 9, 19), LocalDate.of(2017, 9, 20),
		        LocalDate.of(2017, 9, 21), LocalDate.of(2017, 9, 22), LocalDate.of(2017, 9, 25),
		        LocalDate.of(2017, 9, 26), LocalDate.of(2017, 9, 27), LocalDate.of(2017, 9, 28),
		        LocalDate.of(2017, 9, 29) };
		final double[] close = { 8, 8, 8, 8, 9, 10, 11, 12, 13, 14 };

		return createPrices(dates, close);
	}

	/** 
	 * Ten closing prices that start decreasing after the forth entry, starting at LocalDate.of(2017, 9, 18).
	 */
	private TradingDayPrices[] createDecreasingPrices() {
		final LocalDate[] dates = { LocalDate.of(2017, 9, 18), LocalDate.of(2017, 9, 19), LocalDate.of(2017, 9, 20),
		        LocalDate.of(2017, 9, 21), LocalDate.of(2017, 9, 22), LocalDate.of(2017, 9, 25),
		        LocalDate.of(2017, 9, 26), LocalDate.of(2017, 9, 27), LocalDate.of(2017, 9, 28),
		        LocalDate.of(2017, 9, 29) };
		final double[] close = { 8, 8, 8, 8, 7, 6, 5, 4, 3, 2 };

		return createPrices(dates, close);
	}

	/**
	 * Thirty three days of QQQQ (Powershares QQQ Trust) closing prices starting from LocalDate.of(2009,12,14).
	 */
	private TradingDayPrices[] createExampleRelativeStrength() {
		final LocalDate[] dates = { LocalDate.of(2009, 12, 14), LocalDate.of(2009, 12, 15), LocalDate.of(2009, 12, 16),
		        LocalDate.of(2009, 12, 17), LocalDate.of(2009, 12, 18), LocalDate.of(2009, 12, 21),
		        LocalDate.of(2009, 12, 22), LocalDate.of(2009, 12, 23), LocalDate.of(2009, 12, 24),
		        LocalDate.of(2009, 12, 28), LocalDate.of(2009, 12, 29), LocalDate.of(2009, 12, 30),
		        LocalDate.of(2009, 12, 31), LocalDate.of(2010, 1, 4), LocalDate.of(2010, 1, 5),
		        LocalDate.of(2010, 1, 6), LocalDate.of(2010, 1, 7), LocalDate.of(2010, 1, 8), LocalDate.of(2010, 1, 11),
		        LocalDate.of(2010, 1, 12), LocalDate.of(2010, 1, 13), LocalDate.of(2010, 1, 14),
		        LocalDate.of(2010, 1, 15), LocalDate.of(2010, 1, 19), LocalDate.of(2010, 1, 20),
		        LocalDate.of(2010, 1, 21), LocalDate.of(2010, 1, 22), LocalDate.of(2010, 1, 25),
		        LocalDate.of(2010, 1, 26), LocalDate.of(2010, 1, 27), LocalDate.of(2010, 1, 28),
		        LocalDate.of(2010, 1, 29), LocalDate.of(2010, 2, 1) };
		final double[] close = { 44.3389, 44.0902, 44.1497, 43.6124, 44.3278, 44.8264, 45.0955, 45.4245, 45.8433,
		        46.0826, 45.8931, 46.0328, 45.6140, 46.2820, 46.2820, 46.0028, 46.0328, 46.4116, 46.2222, 45.6439,
		        46.2122, 46.2521, 45.7137, 46.4515, 45.7835, 45.3548, 44.0288, 44.1783, 44.2181, 44.5672, 43.4205,
		        42.6628, 43.1314 };

		return createPrices(dates, close);
	}

	private TradingDayPrices[] createPrices( final LocalDate[] dates, final double[] close ) {
		final TradingDayPrices[] data = new TradingDayPrices[dates.length];

		// Only close values are used in RS calculations
		for (int i = 0; i < data.length; i++) {
			data[i] = new TradingDayPricesBuilder().withTradingDate(dates[i]).withClosingPrice(close[i]).build();
		}

		return data;
	}
}