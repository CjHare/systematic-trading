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

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValuesTwoDecimalPlaces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
 * Verifies the behaviour of RelativeStrengthCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthCalculatorTest {

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private RelativeStrengthCalculator calculator;

	@Test
	public void rsiExample() {
		final int lookback = 14;
		final TradingDayPrices[] data = createExamplePrices();
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 2.39, 1.94, 1.96, 2.26, 1.95, 1.34, 1.67, 1.70, 1.25, 1.64, 1.18, 0.99, 0.65, 0.69, 0.7, 0.82,
		        0.58, 0.48, 0.6);
		verifyValidation(data, lookback);
	}

	@Test
	public void rsiFlat() {
		final int lookback = 4;
		final TradingDayPrices[] data = createPrices(8);
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 0, 0, 0, 0);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void startingWithNullDataPoint() {
		final TradingDayPrices[] data = createPrices(8);
		data[0] = null;
		setUpValidationErrorNullEntries();
		setUpCalculator(4);

		rs(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {
		final TradingDayPrices[] data = createPrices(8);
		data[data.length - 1] = null;
		setUpValidationErrorNullEntries();
		setUpCalculator(4);

		rs(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void notEnoughDataPoints() {
		final TradingDayPrices[] data = createPrices(8);
		setUpValidationErrorEnoughValues();
		setUpCalculator(4);

		rs(data);
	}

	@Test
	public void rsiIncreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createIncreasingPrices(8);
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 0.81, 0.86, 0.89, 0.92);
		verifyValidation(data, lookback);
	}

	@Test
	public void rsiDecreasing() {
		final int lookback = 4;
		final TradingDayPrices[] data = createDecreasingPrices(8);
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 0, 0, 0, 0);
		verifyValidation(data, lookback);
	}

	@Test
	public void rsiIncreasingThenDecreasing() {
		final int dataSize = 8;
		final int lookback = 4;
		setUpCalculator(lookback);
		final TradingDayPrices[] dataIncreasing = createIncreasingPrices(dataSize);
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices(dataSize, dataSize);
		final TradingDayPrices[] data = merge(dataIncreasing, dataDecreasing);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 0.81, 0.86, 0.89, 0.92, 2.94, 8.82, 3.78, 2.15, 1.36, 0.91, 0.64, 0.45);
		verifyValidation(data, lookback);
	}

	@Test
	public void rsiDecreasingThenIncreasing() {
		final int dataSize = 8;
		final int lookback = 4;
		final TradingDayPrices[] dataDecreasing = createDecreasingPrices(dataSize);
		final TradingDayPrices[] dataIncreasing = createIncreasingPrices(dataSize, dataSize);
		final TradingDayPrices[] data = merge(dataDecreasing, dataIncreasing);
		setUpCalculator(lookback);

		final RelativeStrengthLine rs = rs(data);

		verifyRs(rs, 0.00, 0.00, 0.00, 0.00, 0.00, 0.11, 0.26, 0.47, 0.73, 1.09, 1.57, 2.21);
		verifyValidation(data, lookback);
	}

	@Test(expected = IllegalArgumentException.class)
	public void macdNullInput() {
		setUpValidationErrorNullInput();
		setUpCalculator(1);

		rs(null);
	}

	private TradingDayPrices[] merge( final TradingDayPrices[] left, final TradingDayPrices[] right ) {
		final TradingDayPrices[] data = new TradingDayPrices[left.length + right.length];
		for (int i = 0; i < left.length; i++) {
			data[i] = left[i];
		}
		for (int i = 0; i < right.length; i++) {
			data[left.length + i] = right[i];
		}

		return data;
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
		calculator = new RelativeStrengthCalculator(lookback, validator);
	}

	private void verifyValidation( final TradingDayPrices[] data, final int lookback ) {
		verify(validator).verifyGreaterThan(1, lookback);
		verify(validator).verifyNotNull(data);
		verify(validator).verifyZeroNullEntries(data);
		verify(validator).verifyEnoughValues(data, lookback);
	}

	private void verifyRs( final RelativeStrengthLine rsi, final double... expected ) {
		assertNotNull(rsi);
		assertNotNull(rsi.getRs());
		assertEquals(expected.length, rsi.getRs().size());
		assertValuesTwoDecimalPlaces(expected, rsi.getRs());
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private TradingDayPrices[] createPrices( final int count ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(1)
			        .withLowestPrice(0).withHighestPrice(2).withClosingPrice(1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createIncreasingPrices( final int count ) {
		return createIncreasingPrices(count, 0);
	}

	private TradingDayPrices[] createIncreasingPrices( final int count, final int dateOffset ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i + dateOffset))
			        .withOpeningPrice(i + 1).withLowestPrice(i).withHighestPrice(i + 2).withClosingPrice(i + 1).build();
		}

		return prices;
	}

	private TradingDayPrices[] createDecreasingPrices( final int count ) {
		return createDecreasingPrices(count, 0);
	}

	private TradingDayPrices[] createDecreasingPrices( final int count, final int dateOffset ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		final int base = count * 2;

		for (int i = 0; i < count; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i + dateOffset))
			        .withOpeningPrice(base - i + 1).withLowestPrice(base + i).withHighestPrice(base + i + 2)
			        .withClosingPrice(base - i + 1).build();
		}

		return prices;
	}

	/**
	 * Prices from the chart school example.
	 */
	private TradingDayPrices[] createExamplePrices() {
		final double[] exampleCloseValues = { 44.3389, 44.0902, 44.1497, 43.6124, 44.3278, 44.8264, 45.0955, 45.4245,
		        45.8433, 46.0826, 45.8931, 46.0328, 45.6140, 46.2820, 46.2820, 46.0028, 46.0328, 46.4116, 46.2222,
		        45.6439, 46.2122, 46.2521, 45.7137, 46.4515, 45.7835, 45.3548, 44.0288, 44.1783, 44.2181, 44.5672,
		        43.4205, 42.6628, 43.1314 };
		final TradingDayPrices[] prices = new TradingDayPrices[exampleCloseValues.length];

		for (int i = 0; i < prices.length; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.now().plusDays(i)).withOpeningPrice(0)
			        .withLowestPrice(0).withHighestPrice(0).withClosingPrice(exampleCloseValues[i]).build();
		}

		return prices;
	}
}