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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * The mean for a consecutive set of numbers.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageCalculator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Number of days to average the value on. */
	private final int lookback;

	/**
	 * @param lookback the number of days to use when calculating the SMA.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public SimpleMovingAverageCalculator( final int lookback, final MathContext mathContext ) {
		this.lookback = lookback;
		this.mathContext = mathContext;
	}

	/**
	 * @param data ordered chronologically, from oldest to youngest (most recent first).
	 * @param smaValues store for the sma values.
	 * @return calculated simple moving average values in the given smaValues array.
	 * @throws TooFewDataPoints not enough closing prices to perform EMA calculations.
	 */
	public BigDecimal[] sma( final TradingDayPrices[] data, final BigDecimal[] smaValues ) throws TooFewDataPoints {

		// Expecting the same number of input data points as outputs
		if (data.length != smaValues.length) {
			throw new TooFewDataPoints( String.format(
					"The number of data points given: %s does not match the expected size: %s", data.length,
					smaValues.length ) );
		}

		// Skip any null entries
		int startSmaIndex = 0;
		while (isNullEntryWithinArray( data, startSmaIndex )) {
			startSmaIndex++;
		}

		// Have we the minimum number of values
		if (data.length < startSmaIndex + lookback) {
			throw new TooFewDataPoints( String.format(
					"At least %s data points are needed for Simple Moving Average, only %s given", lookback,
					data.length ) );
		}

		// No values without the full look back range
		startSmaIndex += lookback;
		startSmaIndex--;

		// Initialise the beginning of the return array
		for (int i = 0; i < startSmaIndex; i++) {
			smaValues[i] = null;
		}

		// Start at the end and work towards the origin
		for (int i = data.length - 1; i >= startSmaIndex; i--) {
			smaValues[i] = simpleAverage( i, data );
		}

		return smaValues;
	}

	private boolean isNullEntryWithinArray( final TradingDayPrices[] data, final int index ) {

		if (index < data.length) {
			return data[index] == null || data[index].getClosingPrice() == null
					|| data[index].getClosingPrice().getPrice() == null;
		}

		return false;
	}

	/**
	 * Calculate the average from this value and the previous look back amount.
	 */
	private BigDecimal simpleAverage( final int startIndex, final TradingDayPrices[] data ) {
		BigDecimal average = data[startIndex].getClosingPrice().getPrice();
		final int endIndex = startIndex - lookback;

		for (int i = startIndex - 1; i > endIndex; i--) {
			average = average.add( data[i].getClosingPrice().getPrice() );
		}

		return average.divide( BigDecimal.valueOf( lookback ), mathContext );
	}
}
