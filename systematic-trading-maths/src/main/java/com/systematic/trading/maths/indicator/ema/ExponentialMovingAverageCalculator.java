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
package com.systematic.trading.maths.indicator.ema;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * Exponential Moving Average (EMA) implementation without restriction on maximum number of trading
 * days to analyse.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageCalculator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	/**
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator( final int lookback, final MathContext mathContext ) {
		this.lookback = lookback;
		this.mathContext = mathContext;
	}

	/**
	 * Calculates the ema.
	 * 
	 * @param data ordered chronologically, from oldest to youngest (most recent first).
	 * @param emaValues store for the ema values that is returned populated.
	 * @throws TooFewDataPoints not enough closing prices to perform EMA calculations.
	 */
	public BigDecimal[] ema( final TradingDayPrices[] data, final BigDecimal[] emaValues ) throws TooFewDataPoints {

		// Expecting the same number of input data points as outputs
		if (data.length != emaValues.length) {
			throw new TooFewDataPoints( String.format(
					"The number of data points given: %s does not match the expected size: %s", data.length,
					emaValues.length ) );
		}

		// Need at least one RSI value
		if (data.length < lookback + 1) {
			throw new TooFewDataPoints( String.format(
					"At least %s data points are needed for Exponential Moving Average, only %s given", lookback + 1,
					data.length ) );
		}

		// Skip any null entries
		int startSmaIndex = 0;
		while (data[startSmaIndex] == null || data[startSmaIndex].getClosingPrice() == null) {
			startSmaIndex++;
		}

		// Initialise the return array with null to the start Sma
		for (int i = 0; i < startSmaIndex; i++) {
			emaValues[i] = null;
		}

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		if (data.length < endSmaIndex) {
			throw new TooFewDataPoints( String.format(
					"At least %s data points are needed for Exponential Moving Average, only %s given", endSmaIndex,
					data.length ) );
		}

		BigDecimal simpleMovingAverage = BigDecimal.ZERO;
		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			simpleMovingAverage = simpleMovingAverage.add( data[i].getClosingPrice().getPrice(), mathContext );
		}

		simpleMovingAverage = simpleMovingAverage.divide( BigDecimal.valueOf( lookback ), mathContext );

		/* EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) */
		final BigDecimal multiplier = calculateSmoothingConstant();
		BigDecimal yesterday = simpleMovingAverage;
		BigDecimal today;

		for (int i = endSmaIndex; i < data.length; i++) {
			today = data[i].getClosingPrice().getPrice();

			emaValues[i] = (today.subtract( yesterday, mathContext )).multiply( multiplier, mathContext ).add(
					yesterday, mathContext );

			yesterday = today;
		}

		return emaValues;
	}

	private BigDecimal calculateSmoothingConstant() {
		return BigDecimal.valueOf( 2d / (lookback + 1) );
	}
}
