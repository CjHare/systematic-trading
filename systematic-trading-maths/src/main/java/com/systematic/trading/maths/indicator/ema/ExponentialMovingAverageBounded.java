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
import com.systematic.trading.maths.exception.TooManyDataPoints;

/**
 * Exponential Moving Average (EMA) implementation with restriction on maximum number of trading
 * days to analyse.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageBounded implements ExponentialMovingAverage {

	/** Delegate for the exponential moving average calculations. */
	private final ExponentialMovingAverageCalculator calculator;

	/** Reused data store for the exponential moving average. */
	private final BigDecimal[] ema;

	/** Maximum number of trading days to calculate the ATR on. */
	private final int maximum;

	/**
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param maximum number of days to calculate the ATR on.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageBounded( final int lookback, final int maximum, final MathContext mathContext ) {
		this.calculator = new ExponentialMovingAverageCalculator( lookback, mathContext );
		this.maximum = maximum;
		this.ema = new BigDecimal[maximum];
	}

	@Override
	public BigDecimal[] ema( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		// Restrict on the number of trading days
		if (data.length > maximum) {
			throw new TooManyDataPoints( String.format(
					"At most %s data points are needed for Exponential Moving Average, %s given", maximum, data.length ) );
		}

		return calculator.ema( data, ema );
	}
}
