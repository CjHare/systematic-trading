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

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.rs.RelativeStrength;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthLine;

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

	/** Calculator instance being tested. */
	private RelativeStrengthIndexCalculator calculator;

	@Test
	public void rsi() {
		final RelativeStrengthLine rsData = createIncreasingRelativeStrengthValues(5);
		setUpCalculator(rsData);
		final TradingDayPrices[] prices = new TradingDayPrices[] {};

		final RelativeStrengthIndexLine rsi = rsi(prices);

		verifyRsi(rsi, 67.21, 75.31, 80.20, 83.47, 85.82);
		verifyRs(prices);
	}

	@Test
	public void rsiExample() {
		final RelativeStrengthLine rsData = createExampleRelativeStrengthValues();
		setUpCalculator(rsData);
		final TradingDayPrices[] prices = new TradingDayPrices[] {};

		final RelativeStrengthIndexLine rsi = rsi(prices);

		verifyRsi(rsi, 70.53, 66.32, 66.55, 69.41, 66.35, 57.97, 62.93, 63.26, 56.06, 62.38, 54.71, 50.42, 39.99, 41.46,
		        41.87, 45.46, 37.30, 33.08, 37.77);
		verifyRs(prices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator();

		rsi(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() {
		setUpValidationErrorNoNullEntries();
		setUpCalculator();

		rsi(new TradingDayPrices[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {
		setUpValidationErrorNoNullEntries();
		setUpCalculator();

		rsi(new TradingDayPrices[] {});
	}

	@SafeVarargs
	private final void setUpCalculator( final RelativeStrengthLine... rsData ) {
		if (rsData.length > 0) {
			OngoingStubbing<RelativeStrengthLine> rsResponses = when(
			        relativeStrength.rs(any(TradingDayPrices[].class)));
			for (final RelativeStrengthLine rs : rsData) {
				rsResponses = rsResponses.thenReturn(rs);
			}
		}

		calculator = new RelativeStrengthIndexCalculator(relativeStrength, validator);
	}

	private void verifyRsi( final RelativeStrengthIndexLine rsi, final double... expected ) {
		assertNotNull(rsi);
		assertNotNull(rsi.getRsi());
		assertEquals(expected.length, rsi.getRsi().size());
		assertValues(expected, rsi.getRsi());
	}

	private void verifyRs( final TradingDayPrices[] prices ) {
		verify(relativeStrength).rs(prices);
	}

	private void setUpValidationErrorNoNullEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private RelativeStrengthIndexLine rsi( final TradingDayPrices[] prices ) {
		return calculator.rsi(prices);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private RelativeStrengthLine createIncreasingRelativeStrengthValues( final int count ) {
		final SortedMap<LocalDate, BigDecimal> rs = new TreeMap<>();

		for (int i = 0; i < count; i++) {
			rs.put(LocalDate.now().plus(i, ChronoUnit.DAYS), BigDecimal.valueOf(i + 2.05));
		}

		return new RelativeStrengthLine(rs);
	}

	/**
	 * Values taken from example on chart school site
	 */
	private RelativeStrengthLine createExampleRelativeStrengthValues() {
		final double[] exampleData = { 2.3936, 1.9690, 1.9895, 2.2686, 1.9722, 1.3795, 1.6976, 1.7216, 1.2758, 1.6580,
		        1.2079, 1.0171, 0.6664, 0.7082, 0.7203, 0.8336, 0.5950, 0.4943, 0.6070 };

		final SortedMap<LocalDate, BigDecimal> rs = new TreeMap<>();

		for (int i = 0; i < exampleData.length; i++) {
			rs.put(LocalDate.now().plus(i, ChronoUnit.DAYS), BigDecimal.valueOf(exampleData[i]));
		}

		return new RelativeStrengthLine(rs);
	}
}