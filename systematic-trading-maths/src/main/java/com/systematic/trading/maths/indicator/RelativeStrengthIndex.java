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
public class RelativeStrengthIndex {

	/** Number of decimal places for scaling. */
	private static final int ROUNDING_SCALE = 2;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	public RelativeStrengthIndex( final int lookback ) {
		this.lookback = lookback;
	}

	/**
	 * @param closePrices ordered chronologically, from oldest to youngest (most recent first).
	 * @throws TooFewDataPoints not enough closing prices to perform RSI calculations.
	 */
	public BigDecimal[] rsi( final TradingDayPrices[] data ) throws TooFewDataPoints {

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
					upward = upward.add( closeToday.subtract( closeYesterday ) );
					break;

				// Today's price is lower then yesterdays
				case -1:
					downward = downward.add( closeYesterday.subtract( closeToday ) );
					break;

				// When equal there's no movement, both are zero
				case 0:
				default:
					break;
			}

			closeYesterday = closeToday;
		}

		// Dividing by the number of time periods for a SMA
		upward = upward.divide( BigDecimal.valueOf( warmUpTimePeriod ), 2, RoundingMode.HALF_UP );
		downward = downward.divide( BigDecimal.valueOf( warmUpTimePeriod ), 2, RoundingMode.HALF_UP );

		/* RS = EMA(U,n) / EMA(D,n) (smoothing constant) multiplier: (2 / (Time periods + 1) ) EMA:
		 * {Close - EMA(previous day)} x multiplier + EMA(previous day). */

		final BigDecimal multiplier = calculateSmoothingConstant();
		final BigDecimal[] relativeStrength = new BigDecimal[data.length];

		for (int i = warmUpTimePeriod; i < relativeStrength.length; i++) {
			closeToday = data[i].getClosingPrice();
			closeYesterday = data[i - 1].getClosingPrice();

			switch (closeToday.compareTo( closeYesterday )) {

			// Today's price is higher then yesterdays
				case 1:
					upward = (closeToday.subtract( closeYesterday ).subtract( upward )).multiply( multiplier ).add(
							upward );

					downward = downward.negate().multiply( multiplier ).add( downward );
					break;

				// Today's price is lower then yesterdays
				case -1:
					upward = upward.negate().multiply( multiplier ).add( upward );

					downward = (closeYesterday.subtract( closeToday ).subtract( downward )).multiply( multiplier ).add(
							downward );
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
				relativeStrength[i] = upward.divide( downward, ROUNDING_SCALE, RoundingMode.HALF_UP );
			}
		}

		/* RSI = 100 / 1 + RS */
		final BigDecimal[] rsiValues = new BigDecimal[relativeStrength.length];
		final BigDecimal oneHundred = BigDecimal.valueOf( 100 );

		for (int i = warmUpTimePeriod; i < rsiValues.length; i++) {
			rsiValues[i] = oneHundred.subtract( oneHundred.divide( BigDecimal.ONE.add( relativeStrength[i] ),
					ROUNDING_SCALE, RoundingMode.HALF_UP ) );
		}

		return rsiValues;
	}

	private BigDecimal calculateSmoothingConstant() {
		return BigDecimal.valueOf( 2d / (lookback + 1) );
	}
}
