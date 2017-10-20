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
package com.systematic.trading.maths.indicator.macd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageIndicator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageLine;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.matcher.IsSortedMap;
import com.systematic.trading.maths.util.TradingDayPricesBuilder;

/**
 * Verifies the behaviour of the MovingAverageConvergenceDivergenceCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class MovingAverageConvergenceDivergenceCalculatorTest {

	private static final int SLOW_EMA_OFFSET = 2;
	private static final int NO_SLOW_EMA_OFFSET = 0;

	@Mock
	private ExponentialMovingAverageIndicator fastEma;

	@Mock
	private ExponentialMovingAverageIndicator slowEma;

	@Mock
	private ExponentialMovingAverage signalEma;

	@Mock
	private Validator validator;

	/** Calculator instance being tested. */
	private MovingAverageConvergenceDivergenceCalculator calculator;

	@Before
	public void setUp() {
		calculator = new MovingAverageConvergenceDivergenceCalculator(fastEma, slowEma, signalEma, validator);
	}

	@Test
	public void minimumNumberOfPrices() {
		setUpMinimumNumberOfPrices(16);

		final int minimumNuumberOfPrices = calculator.getMinimumNumberOfPrices();

		assertEquals(16, minimumNuumberOfPrices);
		verifyMinimumNumberOfPricesInteractions();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullDataSet() {
		setUpEngoughValuesValidationException();

		macd(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyDataSet() {
		setUpNoNUllEntriesValidationException();

		macd(new TradingDayPrices[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void singleNullEntryDataSet() {
		setUpEngoughValuesValidationException();

		macd(new TradingDayPrices[1]);
	}

	@Test
	public void macd() {
		final TradingDayPrices[] data = createPrices(5);
		final SortedMap<LocalDate, BigDecimal> signalLine = asBigDecimal(5.5, 4.4, 3.3, 2.2, 1.1);
		final SortedMap<LocalDate, BigDecimal> macdValues = asBigDecimal(-2.3, -1.4, -2.5, -1.0, 0.25);
		setUpFastEma(1, 3, 3, 5, 6.25);
		setUpSlowEma(SLOW_EMA_OFFSET, 1.1, 2.2, 3.3, 4.4, 5.5, 6, 6);
		setUpSignalEma(signalLine);

		final MovingAverageConvergenceDivergenceLines lines = macd(data);

		verifyMacdLines(lines, macdValues, signalLine);
		verfiyEmaCalls(data, macdValues);
		verifyValidation(data);
	}

	@Test
	public void macdSameSizeFastSlowEma() {
		final TradingDayPrices[] data = createPrices(5);
		final SortedMap<LocalDate, BigDecimal> signalLine = asBigDecimal(5.5, 4.4, 3.3, 2.2, 1.1);
		final SortedMap<LocalDate, BigDecimal> macdValues = asBigDecimal(-0.1, 0.8, -0.3, 0.6, 0.75);
		setUpFastEma(1, 3, 3, 5, 6.25);
		setUpSlowEma(NO_SLOW_EMA_OFFSET, 1.1, 2.2, 3.3, 4.4, 5.5);
		setUpSignalEma(signalLine);

		final MovingAverageConvergenceDivergenceLines lines = macd(data);

		verifyMacdLines(lines, macdValues, signalLine);
		verfiyEmaCalls(data, macdValues);
		verifyValidation(data);
	}

	@Test
	public void macdSameLargerFastSlowEma() {
		final TradingDayPrices[] data = createPrices(5);
		final SortedMap<LocalDate, BigDecimal> signalLine = asBigDecimal(5.5, 4.4, 3.3, 2.2, 1.1);
		final SortedMap<LocalDate, BigDecimal> macdValues = asBigDecimal(-0.1, 0.8, -0.3, 0.6, 0.75);
		setUpFastEma(1, 3, 3, 5, 6.25, 7);
		setUpSlowEma(NO_SLOW_EMA_OFFSET, 1.1, 2.2, 3.3, 4.4, 5.5);
		setUpSignalEma(signalLine);

		final MovingAverageConvergenceDivergenceLines lines = macd(data);

		verifyMacdLines(lines, macdValues, signalLine);
		verfiyEmaCalls(data, macdValues);
		verifyValidation(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void macdNullInput() {
		setUpValidationErrorNullInput();

		macd(null);
	}

	private void verifyMinimumNumberOfPricesInteractions() {
		verify(slowEma).getMinimumNumberOfPrices();
		verifyNoMoreInteractions(slowEma);
		verifyZeroInteractions(fastEma);
		verifyZeroInteractions(signalEma);
	}

	private void setUpMinimumNumberOfPrices( final int minimium ) {
		when(slowEma.getMinimumNumberOfPrices()).thenReturn(minimium);
	}

	private void verifyValidation( final TradingDayPrices[] data ) {
		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data, 1);
		verify(validator).verifyZeroNullEntries(data);
	}

	private MovingAverageConvergenceDivergenceLines macd( final TradingDayPrices[] dataSet ) {
		return calculator.calculate(dataSet);
	}

	private void setUpValidationErrorNullInput() {
		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private void verifyMacdLines( final MovingAverageConvergenceDivergenceLines lines,
	        final SortedMap<LocalDate, BigDecimal> expectedMacd,
	        final SortedMap<LocalDate, BigDecimal> expectedSignalLine ) {
		assertNotNull(lines);

		verifySortedMap(expectedMacd, lines.getMacd());
		verifySortedMap(expectedSignalLine, lines.getSignalLine());
	}

	private void verifySortedMap( final SortedMap<LocalDate, BigDecimal> expected,
	        final SortedMap<LocalDate, BigDecimal> actual ) {
		assertNotNull(actual);
		assertNotNull(expected);
		assertEquals(expected.size(), actual.size());

		for (final Map.Entry<LocalDate, BigDecimal> entry : expected.entrySet()) {
			assertTrue(String.format("Mising key: %s", entry.getKey()), actual.containsKey(entry.getKey()));
			assertEquals(entry.getValue(), actual.get(entry.getKey()));
		}
	}

	private void verfiyEmaCalls( final TradingDayPrices[] dataSet, final SortedMap<LocalDate, BigDecimal> macdValues ) {
		verify(fastEma).calculate(dataSet);
		verifyNoMoreInteractions(fastEma);

		verify(slowEma).calculate(dataSet);
		verifyNoMoreInteractions(slowEma);

		verify(signalEma).calculate(isSortedMap(macdValues));
		verifyNoMoreInteractions(signalEma);
	}

	@SuppressWarnings("unchecked")
	private void setUpSignalEma( final SortedMap<LocalDate, BigDecimal> signalLine ) {
		when(signalEma.calculate(any(SortedMap.class))).thenReturn(new ExponentialMovingAverageLine(signalLine));
	}

	private void setUpSlowEma( final int offset, final double... values ) {
		when(slowEma.calculate(any(TradingDayPrices[].class)))
		        .thenReturn(new ExponentialMovingAverageLine(asBigDecimalDateOffset(offset, values)));
	}

	private void setUpFastEma( final double... values ) {
		when(fastEma.calculate(any(TradingDayPrices[].class)))
		        .thenReturn(new ExponentialMovingAverageLine(asBigDecimal(values)));
	}

	/**
	 * Creates the requested number of local dates, starting from the Epoch.
	 */
	public List<LocalDate> createLocalDates( final int size ) {
		final List<LocalDate> list = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			list.add(LocalDate.ofEpochDay(i));
		}

		return list;
	}

	public SortedMap<LocalDate, BigDecimal> asBigDecimal( final double... values ) {
		return asBigDecimalDateOffset(0, values);
	}

	public SortedMap<LocalDate, BigDecimal> asBigDecimalDateOffset( final int dateOffset, final double... values ) {
		final SortedMap<LocalDate, BigDecimal> converted = new TreeMap<>();

		for (int i = 0; i < values.length; i++) {
			converted.put(LocalDate.now().plusDays(i - dateOffset), BigDecimal.valueOf(values[i]));
		}

		return converted;
	}

	private TradingDayPrices[] createPrices( final int size ) {
		final TradingDayPrices[] prices = new TradingDayPrices[size];

		for (int i = 0; i < prices.length; i++) {
			prices[i] = new TradingDayPricesBuilder().withTradingDate(LocalDate.ofEpochDay(i)).build();
		}

		return prices;
	}

	private void setUpEngoughValuesValidationException() {
		doThrow(new IllegalArgumentException()).when(validator).verifyEnoughValues(any(TradingDayPrices[].class),
		        anyInt());
	}

	private void setUpNoNUllEntriesValidationException() {
		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private SortedMap<LocalDate, BigDecimal> isSortedMap( final SortedMap<LocalDate, BigDecimal> values ) {
		return argThat(new IsSortedMap(values));
	}
}