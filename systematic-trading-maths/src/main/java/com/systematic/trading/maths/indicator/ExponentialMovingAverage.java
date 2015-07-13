/**
 * Copyright (c) 2015, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.maths.indicator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.systematic.trading.maths.ValueWithDate;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * Exponential Moving Average (EMA)
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverage {

	private final int lookback;

	public ExponentialMovingAverage( final int lookback ) {
		this.lookback = lookback;
	}

	/**
	 * @param closePrices
	 *            ordered chronologically, from oldest to youngest (most recent first).
	 * @throws TooFewDataPoints
	 *             not enough closing prices to perform EMA calculations.
	 */
	public BigDecimal[] ema( final ValueWithDate[] data ) throws TooFewDataPoints {

		// Need at least one RSI value
		if (data.length < lookback + 1) {
			throw new TooFewDataPoints( String.format( "At least %s data points are needed, only %s given",
					lookback + 1, data.length ) );
		}

		// Skip any null entries
		int startSmaIndex = 0;
		while (data[startSmaIndex] == null || data[startSmaIndex].geValue() == null) {
			startSmaIndex++;
		}

		/*
		 * SMA for the initial time periods
		 */
		final int endSmaIndex = startSmaIndex + lookback;
		if (data.length < endSmaIndex) {
			throw new TooFewDataPoints( String.format( "At least %s data points are needed, only %s given",
					endSmaIndex, data.length ) );
		}

		BigDecimal simpleMovingAverage = BigDecimal.ZERO;
		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			simpleMovingAverage = simpleMovingAverage.add( data[i].geValue() );
		}
		simpleMovingAverage = simpleMovingAverage.divide( BigDecimal.valueOf( lookback ), 2, RoundingMode.HALF_UP );

		/*
		 * EMA
		 * 
		 * {Close - EMA(previous day)} x multiplier + EMA(previous day)
		 */
		final BigDecimal[] emaValues = new BigDecimal[data.length];
		final BigDecimal multiplier = calculateSmoothingConstant();
		BigDecimal yesterday = simpleMovingAverage;
		BigDecimal today;

		for (int i = endSmaIndex; i < data.length; i++) {
			today = data[i].geValue();

			emaValues[i] = (today.subtract( yesterday )).multiply( multiplier ).add( yesterday );

			yesterday = today;
		}

		return emaValues;
	}

	private BigDecimal calculateSmoothingConstant() {
		return BigDecimal.valueOf( 2d / (lookback + 1) );
	}
}
