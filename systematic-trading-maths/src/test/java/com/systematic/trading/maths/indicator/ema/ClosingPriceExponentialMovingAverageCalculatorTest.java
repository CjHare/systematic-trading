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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

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

	@Test
	public void emaOnePoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(lookback);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1);
		verifyValidation(data, lookback);
	}

	@Test
	public void emaTwoPoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createPrices(3);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1, 1);
		verifyValidation(data, lookback);
	}

	@Test
	public void emaThreePoints() {
		final int lookback = 2;
		final TradingDayPrices[] data = createIncreasingPrices(4);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 1.5, 2.5, 3.5);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaTwoPointsLastNull() {
		final TradingDayPrices[] data = createIncreasingPrices(4);
		data[data.length - 1] = null;
		setUpValidationErrorZeroEntries();
		setUpCalculator(2);

		ema(data);
	}

	@Test
	public void getMinimumNumberOfPrices() {
		setUpCalculator(4);

		final int requiredDays = calculator.getMinimumNumberOfPrices();

		assertEquals(9, requiredDays);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emaNullInputArray() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);
		final TradingDayPrices[] input = null;

		ema(input);
	}

	@Test
	/**
	 * Ten day EMA, with Intel price data from 24-Mar-10 to 5-May-10
	 */
	public void emaArrayIntelExample() {
		final int lookback = 10;
		final TradingDayPrices[] data = createPrices(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24,
		        22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82, 23.87, 23.65, 23.19,
		        23.10, 23.33, 22.68, 23.10, 22.40, 22.17);
		setUpCalculator(lookback);

		final ExponentialMovingAverageLine ema = ema(data);

		verifyEma(ema, 22.22, 22.21, 22.24, 22.27, 22.33, 22.52, 22.80, 22.97, 23.13, 23.28, 23.34, 23.43, 23.51, 23.53,
		        23.47, 23.40, 23.39, 23.26, 23.23, 23.08, 22.92);
		verifyValidation(data, lookback);
	}

	private ExponentialMovingAverageLine ema( final TradingDayPrices[] data ) {
		return calculator.calculate(data);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void setUpValidationErrorZeroEntries() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private void verifyEma( final ExponentialMovingAverageLine actual, final double... expected ) {
		assertNotNull(actual);
		assertNotNull(actual.getEma());
		assertEquals(expected.length, actual.getEma().size());

		//TODO code
		//		assertValues(expected, actual.getEma());
	}

	private void setUpCalculator( final int lookback ) {
		calculator = new ClosingPriceExponentialMovingAverageCalculator(lookback, 1, validator);
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data, lookback);
		verify(validator).verifyZeroNullEntries(data);
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createPrices( final double... values ) {
		final TradingDayPrices[] prices = new TradingDayPrices[values.length];

		for (int i = 0; i < values.length; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(values[i]).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i))
			        .withOpeningPrice(i + 1).withLowestPrice(1).withHighestPrice(i + 2).withClosingPrice(i + 1).build();
		}

		return prices;
	}
}