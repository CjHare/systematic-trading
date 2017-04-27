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
package com.systematic.trading.maths.formula.rs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
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
 * Uses the EMA in calculation of the relative strength (J. Welles Wilder approach), not the SMA.
 * 
 * Taking the prior value plus the current value is a smoothing technique similar to that used in
 * exponential moving average calculation. This also means that RSI values become more accurate as
 * the calculation period extends.
 * 
 * Until there has been an upwards movements in the data set, RS value will be zero.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthCalculator implements RelativeStrength {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback the number of days to use when calculating the RS.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public RelativeStrengthCalculator( final int lookback, final Validator validator, final MathContext mathContext ) {
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public List<RelativeStrengthDataPoint> rs( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		final int startWindupIndex = 0;
		final int endWindupIndex = startWindupIndex + lookback;

		final UpwardsToDownwardsMovement initialLookback = calculateWindup(data, startWindupIndex, endWindupIndex);

		return calculateRelativeStrengthValues(data, initialLookback, endWindupIndex);
	}

	/**
	 * For the first zero - time period entries calculate the SMA based on up to down movement to use as the first RS value.
	 */
	private UpwardsToDownwardsMovement calculateWindup( final TradingDayPrices[] data, final int startWindupIndex,
	        final int endWindupIndex ) {

		ClosingPrice closeToday;
		ClosingPrice closeYesterday = data[startWindupIndex].getClosingPrice();

		// Calculate the starting values via SMA
		final UpwardsToDownwardsMovement initialLookback = new UpwardsToDownwardsMovement(mathContext);

		for (int i = startWindupIndex; i < endWindupIndex; i++) {
			closeToday = data[i].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				// Today's price is higher then yesterdays
				case 1:
					initialLookback.addUpwards(closeToday.subtract(closeYesterday, mathContext));
				break;

				// Today's price is lower then yesterdays
				case -1:
					initialLookback.addDownwards(closeYesterday.subtract(closeToday, mathContext));
				break;

				// When equal there's no movement, both are zero
				case 0:
				default:
				break;
			}

			closeYesterday = closeToday;
		}

		// Dividing by the number of time periods for a SMA
		initialLookback.divideUpwards(BigDecimal.valueOf(lookback));
		initialLookback.divideDownwards(BigDecimal.valueOf(lookback));

		return initialLookback;
	}

	/**
	 * RS Calculation being:
	 * 	Archive = look back - 1
	 * 	Average Gain = [(previous Average Gain) x archive + current Gain] / lookback.
	 * 	Average Loss = [(previous Average Loss) x archive + current Loss] / lookback.
	 */
	private List<RelativeStrengthDataPoint> calculateRelativeStrengthValues( final TradingDayPrices[] data,
	        final UpwardsToDownwardsMovement initialLookback, final int endWindupIndex ) {

		final BigDecimal archive = BigDecimal.valueOf(lookback - 1L);
		final BigDecimal history = BigDecimal.valueOf(lookback);
		BigDecimal upward = initialLookback.getUpward();
		BigDecimal downward = initialLookback.getDownward();
		BigDecimal currentGain = BigDecimal.ZERO;
		BigDecimal currentLoss = BigDecimal.ZERO;
		BigDecimal relativeStrength;
		ClosingPrice closeToday;
		ClosingPrice closeYesterday;

		final List<RelativeStrengthDataPoint> relativeStrengthValues = new NonNullableArrayList<>(
		        data.length - endWindupIndex);

		for (int i = endWindupIndex; i < data.length; i++) {

			closeToday = data[i].getClosingPrice();
			closeYesterday = data[i - 1].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				case 1: // Today's price is higher then yesterdays
					currentGain = closeToday.subtract(closeYesterday, mathContext);
					currentLoss = BigDecimal.ZERO;
				break;

				case -1: // Today's price is lower then yesterdays
					currentGain = BigDecimal.ZERO;
					currentLoss = closeYesterday.subtract(closeToday, mathContext);
				break;

				case 0: // When equal there's no movement, both are zero
					currentGain = BigDecimal.ZERO;
					currentLoss = BigDecimal.ZERO;
				break;
				default:
				break;
			}

			upward = upward.multiply(archive, mathContext).add(currentGain, mathContext).divide(history, mathContext);
			downward = downward.multiply(archive, mathContext).add(currentLoss, mathContext).divide(history,
			        mathContext);

			if (isZeroOrBelow(downward)) {
				// There's no downward, then avoid dividing by zero
				relativeStrength = upward;
			} else {
				relativeStrength = upward.divide(downward, mathContext);
			}

			relativeStrengthValues.add(new RelativeStrengthDataPoint(data[i].getDate(), relativeStrength));
		}

		return relativeStrengthValues;
	}

	private boolean isZeroOrBelow( final BigDecimal value ) {
		return value.compareTo(BigDecimal.ZERO) <= 0;
	}
}
