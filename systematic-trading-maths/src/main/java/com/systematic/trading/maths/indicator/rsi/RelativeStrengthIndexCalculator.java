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
package com.systematic.trading.maths.indicator.rsi;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * Relative Strength Index - RSI
 * 
 * A technical momentum indicator that compares the magnitude of recent gains to recent losses in an
 * attempt to determine over bought and over sold conditions of an asset.
 * 
 * RSI = 100 - 100/(1 + RS*)
 * 
 * Where RS = Average of x days' up closes / Average of x days' down closes.
 * 
 * Uses the EMA in calculation of the relative strength (J. Welles Wilder approach), not the SMA.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexCalculator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	/**
	 * @param lookback the number of days to use when calculating the RSI.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public RelativeStrengthIndexCalculator( final int lookback, final MathContext mathContext ) {
		this.lookback = lookback;
		this.mathContext = mathContext;
	}

	/**
	 * @param data ordered chronologically, from oldest to youngest (most recent first).
	 * @param relativeStrength store for the relative strength values.
	 * @param rsiValues store for the RSI values calculated, also the array returned.
	 * @throws TooFewDataPoints not enough closing prices to perform RSI calculations.
	 */
	public BigDecimal[] rsi( final TradingDayPrices[] data, final BigDecimal[] relativeStrength,
			final BigDecimal[] rsiValues ) throws TooFewDataPoints {

		// Expecting the same number of input data points as outputs
		if (data.length != relativeStrength.length) {
			throw new TooFewDataPoints( String.format(
					"The number of data points given: %s does not match the expected size: %s", data.length,
					relativeStrength.length ) );
		}

		// Expecting the same number of input data points as outputs
		if (data.length != rsiValues.length) {
			throw new TooFewDataPoints( String.format(
					"The number of data points given: %s does not match the expected size: %s", data.length,
					rsiValues.length ) );
		}

		// Need at least one RSI value
		if (data.length < lookback + 1) {
			throw new TooFewDataPoints( String.format(
					"At least %s data points are needed for Relative Strength Index, only %s given", lookback + 1,
					data.length ) );
		}

		/* For the first zero - time period entries calculate the SMA based on up to down movement
		 * Upwards movement upward = closeToday - closeYesterday downward = 0 Downwards movement
		 * upward = closeYesterday - closeToday */
		final int warmUpTimePeriod = lookback;
		ClosingPrice closeToday, closeYesterday = data[0].getClosingPrice();
		BigDecimal upward = BigDecimal.valueOf( 0 );
		BigDecimal downward = BigDecimal.valueOf( 0 );

		for (int i = 0; i < warmUpTimePeriod; i++) {
			closeToday = data[i].getClosingPrice();

			switch (closeToday.compareTo( closeYesterday )) {

			// Today's price is higher then yesterdays
				case 1:
					upward = upward.add( closeToday.subtract( closeYesterday, mathContext ) );
					break;

				// Today's price is lower then yesterdays
				case -1:
					downward = downward.add( closeYesterday.subtract( closeToday, mathContext ) );
					break;

				// When equal there's no movement, both are zero
				case 0:
				default:
					break;
			}

			closeYesterday = closeToday;
		}

		// Dividing by the number of time periods for a SMA
		upward = upward.divide( BigDecimal.valueOf( warmUpTimePeriod ), mathContext );
		downward = downward.divide( BigDecimal.valueOf( warmUpTimePeriod ), mathContext );

		/* RS = EMA(U,n) / EMA(D,n) (smoothing constant) multiplier: (2 / (Time periods + 1) ) EMA:
		 * {Close - EMA(previous day)} x multiplier + EMA(previous day). */

		final BigDecimal multiplier = calculateSmoothingConstant();

		for (int i = warmUpTimePeriod; i < relativeStrength.length; i++) {
			closeToday = data[i].getClosingPrice();
			closeYesterday = data[i - 1].getClosingPrice();

			switch (closeToday.compareTo( closeYesterday )) {

			// Today's price is higher then yesterdays
				case 1:
					upward = (closeToday.subtract( closeYesterday, mathContext ).subtract( upward, mathContext ))
							.multiply( multiplier, mathContext ).add( upward, mathContext );

					downward = downward.negate().multiply( multiplier, mathContext ).add( downward, mathContext );
					break;

				// Today's price is lower then yesterdays
				case -1:
					upward = upward.negate().multiply( multiplier, mathContext ).add( upward, mathContext );

					downward = (closeYesterday.subtract( closeToday, mathContext ).subtract( downward, mathContext ))
							.multiply( multiplier, mathContext ).add( downward, mathContext );
					break;

				// When equal there's no movement, both are zero
				case 0:
				default:
					break;
			}

			// When downward approaches zero, RSI approaches 100
			if (downward.compareTo( BigDecimal.ZERO ) <= 0) {
				relativeStrength[i] = BigDecimal.valueOf( 100 );
			} else {
				relativeStrength[i] = upward.divide( downward, mathContext );
			}
		}

		/* RSI = 100 / 1 + RS */
		final BigDecimal oneHundred = BigDecimal.valueOf( 100 );

		// Initialise the start of the return array with nulls
		for (int i = 0; i < warmUpTimePeriod; i++) {
			rsiValues[i] = null;
		}

		for (int i = warmUpTimePeriod; i < rsiValues.length; i++) {
			rsiValues[i] = oneHundred.subtract( oneHundred.divide( BigDecimal.ONE.add( relativeStrength[i] ),
					mathContext ) );
		}

		return rsiValues;
	}

	private BigDecimal calculateSmoothingConstant() {
		return BigDecimal.valueOf( 2d / (lookback + 1) );
	}
}
