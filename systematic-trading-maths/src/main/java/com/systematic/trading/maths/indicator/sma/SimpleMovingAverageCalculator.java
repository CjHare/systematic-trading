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
package com.systematic.trading.maths.indicator.sma;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;

/**
 * The mean for a consecutive set of numbers.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageCalculator implements SimpleMovingAverage {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Required number of data points required for SMA calculation. */
	private final int minimumNumberOfPrices;

	/** Number of days to average the value on. */
	private final int lookback;

	/** Provides the array to store the result in. */
	private final List<BigDecimal> smaValues;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** Number of days to calculate the SMA value on. */
	private final int daysOfSmaValues;

	/**
	 * @param lookback the number of days to use when calculating the SMA.
	 * @param daysOfSmaValues the number of trading days to calculate the RSI value.
	 * @param validator validates and parses input.
	 * @param store source for the storage array.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public SimpleMovingAverageCalculator(final int lookback, final int daysOfSmaValues, final Validator validator,
	        final MathContext mathContext) {
		this.minimumNumberOfPrices = lookback + daysOfSmaValues;
		this.daysOfSmaValues = daysOfSmaValues;
		this.mathContext = mathContext;
		this.smaValues = new NonNullableArrayList<>();
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public List<BigDecimal> sma( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, minimumNumberOfPrices);

		smaValues.clear();

		final int endSmaIndex = data.length - 1;
		final int startSmaIndex = endSmaIndex - daysOfSmaValues;

		// Start at the end and work towards the origin
		for (int i = startSmaIndex; i <= endSmaIndex; i++) {
			smaValues.add(simpleAverage(i, data));
		}

		return smaValues;
	}

	/**
	 * Calculate the average from this value and the previous look back amount.
	 */
	private BigDecimal simpleAverage( final int endIndex, final TradingDayPrices[] data ) {
		final int startIndex = endIndex - lookback + 1;
		BigDecimal average = data[endIndex].getClosingPrice().getPrice();

		for (int i = startIndex; i < endIndex; i++) {
			average = average.add(data[i].getClosingPrice().getPrice());
		}

		return average.divide(BigDecimal.valueOf(lookback), mathContext);
	}
}
