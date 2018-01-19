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
package com.systematic.trading.maths.indicator.rsi;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertValues;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.line;
import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.point;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthIndicator;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthLine;
import com.systematic.trading.model.price.TradingDayPrices;

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
	private RelativeStrengthIndicator relativeStrength;

	/** Calculator instance being tested. */
	private RelativeStrengthIndexIndicator calculator;

	@Test
	/*
	 * Powershares QQQ Trust prices from 14 Dec 2009 to 1 Feb 2010.
	 */
	public void rsiExample() {

		final RelativeStrengthLine rsData = createExampleRelativeStrengthValues();
		setUpCalculator(rsData);
		final TradingDayPrices[] data = new TradingDayPrices[] {};

		final RelativeStrengthIndexLine rsi = rsi(data);

		verifyRsi(rsi,
		        line(point(LocalDate.of(2010, 1, 5), 70.53), point(LocalDate.of(2010, 1, 6), 66.32),
		                point(LocalDate.of(2010, 1, 7), 66.55), point(LocalDate.of(2010, 1, 8), 69.41),
		                point(LocalDate.of(2010, 1, 11), 66.35), point(LocalDate.of(2010, 1, 12), 57.97),
		                point(LocalDate.of(2010, 1, 13), 62.93), point(LocalDate.of(2010, 1, 14), 63.26),
		                point(LocalDate.of(2010, 1, 15), 56.06), point(LocalDate.of(2010, 1, 19), 62.38),
		                point(LocalDate.of(2010, 1, 20), 54.71), point(LocalDate.of(2010, 1, 21), 50.42),
		                point(LocalDate.of(2010, 1, 22), 39.99), point(LocalDate.of(2010, 1, 25), 41.46),
		                point(LocalDate.of(2010, 1, 26), 41.87), point(LocalDate.of(2010, 1, 27), 45.46),
		                point(LocalDate.of(2010, 1, 28), 37.30), point(LocalDate.of(2010, 1, 29), 33.08),
		                point(LocalDate.of(2010, 2, 1), 37.77)));

		verifyRs(data);
		verifyValidation(data);
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

		rsi(new TradingDayPrices[1]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void endingWithNullDataPoint() {

		setUpValidationErrorNoNullEntries();
		setUpCalculator();

		rsi(new TradingDayPrices[] {});
	}

	@Test
	public void minimumNumberOfPrices() {

		setUpCalculator();

		final int minimumNumberOfPrices = calculator.minimumNumberOfPrices();

		assertEquals(10, minimumNumberOfPrices);
	}

	@SafeVarargs
	private final void setUpCalculator( final RelativeStrengthLine... rsData ) {

		if (rsData.length > 0) {
			OngoingStubbing<RelativeStrengthLine> rsResponses = when(
			        relativeStrength.calculate(any(TradingDayPrices[].class)));
			for (final RelativeStrengthLine rs : rsData) {
				rsResponses = rsResponses.thenReturn(rs);
			}
		}

		when(relativeStrength.minimumNumberOfPrices()).thenReturn(8);
		calculator = new RelativeStrengthIndexCalculator(relativeStrength, validator);
	}

	private void verifyValidation( final TradingDayPrices[] data ) {

		verify(validator).verifyNotNull(data);
		verify(validator).verifyEnoughValues(data, 8);
		verify(validator).verifyZeroNullEntries(data);
	}

	private void verifyRsi( final RelativeStrengthIndexLine rsi, final SortedMap<LocalDate, BigDecimal> expected ) {

		assertNotNull(rsi);
		assertNotNull(rsi.rsi());
		assertEquals(expected.size(), rsi.rsi().size());
		assertValues(expected, rsi.rsi());
	}

	private void verifyRs( final TradingDayPrices[] prices ) {

		verify(relativeStrength).calculate(prices);
	}

	private void setUpValidationErrorNoNullEntries() {

		doThrow(new IllegalArgumentException()).when(validator).verifyZeroNullEntries(any(TradingDayPrices[].class));
	}

	private RelativeStrengthIndexLine rsi( final TradingDayPrices[] prices ) {

		return calculator.calculate(prices);
	}

	private void setUpValidationErrorNullInput() {

		doThrow(new IllegalArgumentException()).when(validator).verifyNotNull(any());
	}

	private RelativeStrengthLine createExampleRelativeStrengthValues() {

		final LocalDate[] dates = { LocalDate.of(2010, 1, 5), LocalDate.of(2010, 1, 6), LocalDate.of(2010, 1, 7),
		        LocalDate.of(2010, 1, 8), LocalDate.of(2010, 1, 11), LocalDate.of(2010, 1, 12),
		        LocalDate.of(2010, 1, 13), LocalDate.of(2010, 1, 14), LocalDate.of(2010, 1, 15),
		        LocalDate.of(2010, 1, 19), LocalDate.of(2010, 1, 20), LocalDate.of(2010, 1, 21),
		        LocalDate.of(2010, 1, 22), LocalDate.of(2010, 1, 25), LocalDate.of(2010, 1, 26),
		        LocalDate.of(2010, 1, 27), LocalDate.of(2010, 1, 28), LocalDate.of(2010, 1, 29),
		        LocalDate.of(2010, 2, 1) };
		final double[] values = { 2.3936, 1.9690, 1.9895, 2.2686, 1.9722, 1.3795, 1.6976, 1.7216, 1.2758, 1.6580,
		        1.2079, 1.0171, 0.6664, 0.7082, 0.7203, 0.8336, 0.5950, 0.4943, 0.6070 };

		return new RelativeStrengthLine(createRelativeStrengthValues(dates, values));
	}

	private final SortedMap<LocalDate, BigDecimal> createRelativeStrengthValues( final LocalDate[] dates,
	        final double[] value ) {

		final SortedMap<LocalDate, BigDecimal> rs = new TreeMap<>();

		for (int i = 0; i < dates.length; i++) {
			rs.put(dates[i], BigDecimal.valueOf(value[i]));
		}

		return rs;
	}
}