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
package com.systematic.trading.maths.indicator.rsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.rs.RelativeStrength;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthDataPoint;

/**
 * Verifies the behaviour of RelativeStrengthIndexCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexCalculatorTest {

	@Mock
	private Validator validator;

	@Mock
	private RelativeStrength relativeStrength;

	@Test
	public void rsi() {
		final int relativeStrengthCount = 5;
		final List<RelativeStrengthDataPoint> rsData = createIncreasingRelativeStrengthValues(relativeStrengthCount);
		when(relativeStrength.rs(any(TradingDayPrices[].class))).thenReturn(rsData);

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator);

		final TradingDayPrices[] prices = new TradingDayPrices[] {};
		final RelativeStrengthIndexLine rsi = calculator.rsi(prices);

		assertNotNull(rsi);
		assertEquals(relativeStrengthCount, rsi.getRsi().size());
		assertValueEquals(67.21, 0, rsi);
		assertValueEquals(75.31, 1, rsi);
		assertValueEquals(80.20, 2, rsi);
		assertValueEquals(83.47, 3, rsi);

		verify(relativeStrength).rs(prices);
	}

	@Test
	public void rsiExample() {
		final List<RelativeStrengthDataPoint> rsData = createExampleRelativeStrengthValues();
		when(relativeStrength.rs(any(TradingDayPrices[].class))).thenReturn(rsData);

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator);

		final TradingDayPrices[] prices = new TradingDayPrices[] {};
		final RelativeStrengthIndexLine rsi = calculator.rsi(prices);

		assertNotNull(rsi);
		assertEquals(19, rsi.getRsi().size());
		assertValueEquals(70.53, 0, rsi);
		assertValueEquals(66.32, 1, rsi);
		assertValueEquals(66.55, 2, rsi);
		assertValueEquals(69.41, 3, rsi);
		assertValueEquals(66.35, 4, rsi);
		assertValueEquals(57.97, 5, rsi);
		assertValueEquals(62.93, 6, rsi);
		assertValueEquals(63.26, 7, rsi);
		assertValueEquals(56.06, 8, rsi);
		assertValueEquals(62.38, 9, rsi);
		assertValueEquals(54.71, 10, rsi);
		assertValueEquals(50.42, 11, rsi);
		assertValueEquals(39.99, 12, rsi);
		assertValueEquals(41.46, 13, rsi);
		assertValueEquals(41.87, 14, rsi);
		assertValueEquals(45.46, 15, rsi);
		assertValueEquals(37.30, 16, rsi);
		assertValueEquals(33.08, 17, rsi);
		assertValueEquals(37.77, 18, rsi);

		verify(relativeStrength).rs(prices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {
		setUpValidationErrorNullInput();
		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator);

		calculator.rsi(null);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void assertValueEquals( final double expected, final int index, final RelativeStrengthIndexLine actual ) {
		assertEquals(BigDecimal.valueOf(expected).setScale(2, RoundingMode.HALF_EVEN),
		        actual.getRsi().get(getDate(index)).setScale(2, RoundingMode.HALF_EVEN));
	}

	private LocalDate getDate( final int index ) {
		return LocalDate.now().plus(index, ChronoUnit.DAYS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator);

		calculator.rsi(new TradingDayPrices[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator);

		calculator.rsi(new TradingDayPrices[] {});
	}

	private List<RelativeStrengthDataPoint> createIncreasingRelativeStrengthValues( final int count ) {
		final List<RelativeStrengthDataPoint> data = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			data.add(new RelativeStrengthDataPoint(LocalDate.now().plus(i, ChronoUnit.DAYS),
			        BigDecimal.valueOf(i + 2.05)));
		}

		return data;
	}

	/**
	 * Values taken from example on chart school site
	 */
	private List<RelativeStrengthDataPoint> createExampleRelativeStrengthValues() {
		final double[] exampleData = { 2.3936, 1.9690, 1.9895, 2.2686, 1.9722, 1.3795, 1.6976, 1.7216, 1.2758, 1.6580,
		        1.2079, 1.0171, 0.6664, 0.7082, 0.7203, 0.8336, 0.5950, 0.4943, 0.6070 };
		final List<RelativeStrengthDataPoint> data = new ArrayList<>(exampleData.length);

		for (int i = 0; i < exampleData.length; i++) {
			data.add(new RelativeStrengthDataPoint(LocalDate.now().plus(i, ChronoUnit.DAYS),
			        BigDecimal.valueOf(exampleData[i])));
		}

		return data;
	}
}