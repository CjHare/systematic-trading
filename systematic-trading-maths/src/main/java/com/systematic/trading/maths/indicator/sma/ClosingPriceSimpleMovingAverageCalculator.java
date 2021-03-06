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
package com.systematic.trading.maths.indicator.sma;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * The mean for a consecutive set of numbers.
 * 
 * @author CJ Hare
 */
public class ClosingPriceSimpleMovingAverageCalculator implements SimpleMovingAverageIndicator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Required number of data points required for SMA calculation. */
	private final int minimumNumberOfPrices;

	/** Number of days to average the value on. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback
	 *            the number of days to use when calculating the SMA.
	 * @param daysOfSmaValues
	 *            the number of trading days to have a SMA values for, with lookback being the
	 *            number of trading days averaged..
	 * @param validator
	 *            validates and parses input.
	 * @param store
	 *            source for the storage array.
	 */
	public ClosingPriceSimpleMovingAverageCalculator(
	        final int lookback,
	        final int daysOfSmaValues,
	        final Validator validator ) {

		validator.verifyGreaterThan(1, lookback);
		validator.verifyGreaterThan(1, daysOfSmaValues);

		this.minimumNumberOfPrices = lookback + daysOfSmaValues;
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public int minimumNumberOfPrices() {

		return minimumNumberOfPrices;
	}

	@Override
	public SimpleMovingAverageLine calculate( final TradingDayPrices[] data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, minimumNumberOfPrices);

		final SortedMap<LocalDate, BigDecimal> sma = new TreeMap<>();

		// Start at the end and work towards the origin
		for (int i = lookback - 1; i < data.length; i++) {
			sma.put(data[i].date(), simpleAverage(i, data));
		}

		return new SimpleMovingAverageLine(sma);
	}

	/**
	 * Calculate the average from this value and the previous look back amount.
	 */
	private BigDecimal simpleAverage( final int endIndex, final TradingDayPrices[] data ) {

		final int startIndex = endIndex - lookback + 1;
		BigDecimal average = data[endIndex].closingPrice().price();

		for (int i = startIndex; i < endIndex; i++) {
			average = average.add(data[i].closingPrice().price());
		}

		return average.divide(BigDecimal.valueOf(lookback), MATH_CONTEXT);
	}
}
