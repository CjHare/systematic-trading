/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.maths.indicator.rs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Relative Strength  - RS
 * 
 * A technical momentum indicator that compares the magnitude of recent gains to recent losses in an
 * attempt to determine over bought and over sold conditions of an asset.
 * 
 * RS = Average of x days' up closes / Average of x days' down closes.
 * 
 * Uses the EMA in calculation of the relative strength (J. Welles Wilder approach), not Culter's SMA approach.
 * 
 * Until there has been an upwards movements in the data set, RS value will be zero.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthCalculator implements RelativeStrength {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** The least number of prices to calculate the ATR on. */
	private static final int MINIMUM_NUMBER_OF_PRICES = 1;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** Look back as a BigDecimal. */
	private final BigDecimal history;

	/** Look back minus one as a BigDecimal. */
	private final BigDecimal archive;

	/**
	 * @param lookback the number of days to use when calculating the RS.
	 * @param validator validates and parses input.
	 * @param MATH_CONTEXT the scale, precision and rounding to apply to mathematical operations.
	 */
	public RelativeStrengthCalculator( final int lookback, final Validator validator ) {
		this.validator = validator;
		this.lookback = lookback;
		this.archive = BigDecimal.valueOf(lookback - 1L);
		this.history = BigDecimal.valueOf(lookback);

		validator.verifyGreaterThan(1, lookback);
	}

	@Override
	public int getMinimumNumberOfPrices() {
		return MINIMUM_NUMBER_OF_PRICES;
	}

	@Override
	public RelativeStrengthLine calculate( final TradingDayPrices[] data ) {
		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		return rs(data, windup(data));
	}

	/**
	 * For the first zero - time period entries calculate the SMA based on up to down movement to use as the first RS value.
	 */
	private UpwardsToDownwardsMovement windup( final TradingDayPrices[] data ) {

		// Calculate the starting values via SMA
		final UpwardsToDownwardsMovement initialLookback = new UpwardsToDownwardsMovement(MATH_CONTEXT);

		ClosingPrice closeYesterday = data[0].getClosingPrice();
		ClosingPrice closeToday;

		for (int i = 1; i < lookback; i++) {
			closeToday = data[i].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				// Today's price is higher then yesterdays
				case 1:
					initialLookback.addUpwards(closeToday.subtract(closeYesterday, MATH_CONTEXT));
				break;

				// Today's price is lower then yesterdays
				case -1:
					initialLookback.addDownwards(closeYesterday.subtract(closeToday, MATH_CONTEXT));
				break;

				// When equal there's no movement, both are zero
				case 0:
				default:
				break;
			}

			closeYesterday = closeToday;
		}

		// Dividing by the number of time periods for a SMA
		initialLookback.divideUpwards(history);
		initialLookback.divideDownwards(history);

		return initialLookback;
	}

	/**
	 * RS Calculation being:
	 * 	Archive = look back - 1
	 * 	Average Gain = [(previous Average Gain) x archive + current Gain] / lookback.
	 * 	Average Loss = [(previous Average Loss) x archive + current Loss] / lookback.
	 */
	private RelativeStrengthLine rs( final TradingDayPrices[] data, final UpwardsToDownwardsMovement initialLookback ) {
		BigDecimal upward = initialLookback.getUpward();
		BigDecimal downward = initialLookback.getDownward();
		BigDecimal currentGain;
		BigDecimal currentLoss;
		BigDecimal relativeStrength;
		ClosingPrice closeToday;
		ClosingPrice closeYesterday;

		final SortedMap<LocalDate, BigDecimal> rsLine = new TreeMap<>();

		for (int i = lookback; i < data.length; i++) {

			closeToday = data[i].getClosingPrice();
			closeYesterday = data[i - 1].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				case 1: // Today's price is higher then yesterdays
					currentGain = closeToday.subtract(closeYesterday, MATH_CONTEXT);
					currentLoss = BigDecimal.ZERO;
				break;

				case -1: // Today's price is lower then yesterdays
					currentGain = BigDecimal.ZERO;
					currentLoss = closeYesterday.subtract(closeToday, MATH_CONTEXT);
				break;

				case 0: // When equal there's no movement, both are zero
				default:
					currentGain = BigDecimal.ZERO;
					currentLoss = BigDecimal.ZERO;
				break;
			}

			/**
			 * Wilder originally formulated the calculation of the moving average as: newval = (prevval * (period - 1) + newdata) / period. 
			 * This is fully equivalent to the exponential smoothing of a n-period smoothed moving average (SMMA). 
			 */
			upward = upward.multiply(archive, MATH_CONTEXT).add(currentGain, MATH_CONTEXT).divide(history,
			        MATH_CONTEXT);
			downward = downward.multiply(archive, MATH_CONTEXT).add(currentLoss, MATH_CONTEXT).divide(history,
			        MATH_CONTEXT);

			if (isZeroOrBelow(downward)) {
				// There's no downward, then avoid dividing by zero
				relativeStrength = upward;
			} else {
				relativeStrength = upward.divide(downward, MATH_CONTEXT);
			}

			rsLine.put(data[i].getDate(), relativeStrength);
		}

		return new RelativeStrengthLine(rsLine);
	}

	private boolean isZeroOrBelow( final BigDecimal value ) {
		return value.compareTo(BigDecimal.ZERO) <= 0;
	}
}