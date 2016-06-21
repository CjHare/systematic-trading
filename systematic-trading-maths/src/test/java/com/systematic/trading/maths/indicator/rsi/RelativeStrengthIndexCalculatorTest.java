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
package com.systematic.trading.maths.indicator.rsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
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
import com.systematic.trading.maths.formula.rs.RelativeStrength;
import com.systematic.trading.maths.formula.rs.RelativeStrengthDataPoint;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Verifies the behaviour of RelativeStrengthIndexCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

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
		        validator, MATH_CONTEXT);

		final TradingDayPrices[] prices = new TradingDayPrices[] {};
		final List<RelativeStrengthIndexDataPoint> rsi = calculator.rsi(prices);

		assertNotNull(rsi);
		assertEquals(relativeStrengthCount, rsi.size());
		assertValueEquals(67.21, rsi.get(0));
		assertValueEquals(75.31, rsi.get(1));
		assertValueEquals(80.20, rsi.get(2));
		assertValueEquals(83.47, rsi.get(3));

		verify(relativeStrength).rs(prices);
	}

	@Test
	public void rsiExample() {
		final List<RelativeStrengthDataPoint> rsData = createExampleRelativeStrengthValues();
		when(relativeStrength.rs(any(TradingDayPrices[].class))).thenReturn(rsData);

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator, MATH_CONTEXT);

		final TradingDayPrices[] prices = new TradingDayPrices[] {};
		final List<RelativeStrengthIndexDataPoint> rsi = calculator.rsi(prices);

		assertNotNull(rsi);
		assertEquals(19, rsi.size());
		assertValueEquals(70.53, rsi.get(0));
		assertValueEquals(66.32, rsi.get(1));
		assertValueEquals(66.55, rsi.get(2));
		assertValueEquals(69.41, rsi.get(3));
		assertValueEquals(66.35, rsi.get(4));
		assertValueEquals(57.97, rsi.get(5));
		assertValueEquals(62.93, rsi.get(6));
		assertValueEquals(63.26, rsi.get(7));
		assertValueEquals(56.06, rsi.get(8));
		assertValueEquals(62.38, rsi.get(9));
		assertValueEquals(54.71, rsi.get(10));
		assertValueEquals(50.42, rsi.get(11));
		assertValueEquals(39.99, rsi.get(12));
		assertValueEquals(41.46, rsi.get(13));
		assertValueEquals(41.87, rsi.get(14));
		assertValueEquals(45.46, rsi.get(15));
		assertValueEquals(37.30, rsi.get(16));
		assertValueEquals(33.08, rsi.get(17));
		assertValueEquals(37.77, rsi.get(18));

		verify(relativeStrength).rs(prices);
	}

	private void assertValueEquals( final double expected, final RelativeStrengthIndexDataPoint actual ) {
		assertEquals(BigDecimal.valueOf(expected).setScale(2, RoundingMode.HALF_EVEN),
		        actual.getValue().setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator, MATH_CONTEXT);

		calculator.rsi(new TradingDayPrices[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));

		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(relativeStrength,
		        validator, MATH_CONTEXT);

		calculator.rsi(new TradingDayPrices[] {});
	}

	private List<RelativeStrengthDataPoint> createIncreasingRelativeStrengthValues( final int count ) {
		final List<RelativeStrengthDataPoint> data = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			data.add(new RelativeStrengthDataPoint(LocalDate.now().plus(count, ChronoUnit.DAYS),
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