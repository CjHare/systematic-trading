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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
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

	@Test
	public void emaTwoPointsDecimal() {
		final int lookback = 2;
		final SortedMap<LocalDate, BigDecimal> data = createIncreasingDecimalPrices(4);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 0.5, 1.5, 2.5);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPointsDecimal() {
		final SortedMap<LocalDate, BigDecimal> data = createIncreasingDecimalPrices(1);
		setUpValidationErrorEnoughValues();
		setUpCalculator(2);

		ema(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);
		final SortedMap<LocalDate, BigDecimal> input = null;

		ema(input);
	}

	@Test
	/**
	 * Ten day EMA, with Intel price data from 24-Mar-10 to 5-May-10
	 */
	public void emaIntelExample() {
		final int lookback = 10;
		final SortedMap<LocalDate, BigDecimal> data = createDecimalPrices(22.27, 22.19, 22.08, 22.17, 22.18, 22.13,
		        22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82,
		        23.87, 23.65, 23.19, 23.10, 23.33, 22.68, 23.10, 22.40, 22.17);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 22.22, 22.21, 22.24, 22.27, 22.33, 22.52, 22.80, 22.97, 23.13, 23.28, 23.34, 23.43, 23.51, 23.53,
		        23.47, 23.40, 23.39, 23.26, 23.23, 23.08, 22.92);
		verifyValidation(data, lookback);
	}

	private ExponentialMovingAverageLine ema( final SortedMap<LocalDate, BigDecimal> data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void setUpValidationErrorEnoughValues() {
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(anyListOf(BigDecimal.class),
		        anyInt());
	}

	private void verifyEma( final ExponentialMovingAverageLine actual, final double... expected ) {
		assertNotNull(actual);
		assertNotNull(actual.getEma());
		assertEquals(expected.length, actual.getEma().size());

		//TODO code
		//		assertValues(expected, actual.getEma());
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

	private SortedMap<LocalDate, BigDecimal> createIncreasingDecimalPrices( final int count ) {
		final SortedMap<LocalDate, BigDecimal> prices = new TreeMap<>();

		for (int i = 0; i < count; i++) {
			prices.put(LocalDate.now().plusDays(i), BigDecimal.valueOf(i));
		}

		return prices;
	}

	private SortedMap<LocalDate, BigDecimal> createDecimalPrices( final double... values ) {
		final SortedMap<LocalDate, BigDecimal> prices = new TreeMap<>();

		for (int i = 0; i < values.length; i++) {
			prices.put(LocalDate.now().plusDays(i), BigDecimal.valueOf(values[i]));
		}

		return prices;
	}
}