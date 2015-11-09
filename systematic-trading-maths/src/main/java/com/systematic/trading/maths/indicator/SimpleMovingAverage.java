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
package com.systematic.trading.maths.indicator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.systematic.trading.maths.ValueWithDate;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * The mean for a consecutive set of numbers.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverage {

	/** Number of decimal places for scaling. */
	private static final int ROUNDING_SCALE = 2;

	/** Number of days to average the value on. */
	private final int lookback;

	public SimpleMovingAverage( final int lookback ) {
		this.lookback = lookback;
	}

	/**
	 * @param data ordered chronologically, from oldest to youngest (most recent first).
	 * @throws TooFewDataPoints not enough closing prices to perform EMA calculations.
	 */
	public BigDecimal[] sma( final ValueWithDate[] data ) throws TooFewDataPoints {

		// Skip any null entries
		int startSmaIndex = 0;
		while (isNullEntryWithinArray( data, startSmaIndex )) {
			startSmaIndex++;
		}

		// Have we the minimum number of values
		if (data.length < startSmaIndex + lookback) {
			throw new TooFewDataPoints( String.format( "At least %s data points are needed for Simple Moving Average, only %s given", lookback,
					data.length ) );
		}

		// No values without the full look back range
		startSmaIndex += lookback;
		startSmaIndex--;

		final BigDecimal[] sma = new BigDecimal[data.length];

		// Start at the end and work towards the origin
		for (int i = data.length - 1; i >= startSmaIndex; i--) {
			sma[i] = simpleAverage( i, data );
		}

		return sma;
	}

	private boolean isNullEntryWithinArray( final ValueWithDate[] data, final int index ) {

		if (index < data.length) {
			return data[index] == null || data[index].geValue() == null;
		}

		return false;
	}

	/**
	 * Calculate the average from this value and the previous look back amount.
	 */
	private BigDecimal simpleAverage( final int startIndex, final ValueWithDate[] data ) {
		BigDecimal average = data[startIndex].geValue();
		final int endIndex = startIndex - lookback;

		for (int i = startIndex - 1; i > endIndex; i--) {
			average = average.add( data[i].geValue() );
		}

		return average.divide( BigDecimal.valueOf( lookback ), ROUNDING_SCALE, RoundingMode.HALF_UP );
	}
}
