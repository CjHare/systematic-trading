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
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.maths.indicator.Validator;

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
 * Taking the prior value plus the current value is a smoothing technique similar to that used in
 * exponential moving average calculation. This also means that RSI values become more accurate as
 * the calculation period extends.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexCalculator implements RelativeStrengthIndex {

	/** Constant for the value of 100. */
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(50);

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The number of trading days to look back for calculation. */
	private final int lookback;

	/** Provides the array to store the result in. */
	private final List<BigDecimal> relativeStrengthValues;

	/** Provides the array to store the result in. */
	private final List<BigDecimal> relativeStrengthIndexValues;

	/** Required number of data points required for ATR calculation. */
	private final int minimumNumberOfPrices;

	/** Constant used for smoothing the moving average. */
	private final BigDecimal smoothingConstant;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback the number of days to use when calculating the RSI.
	 * @param daysOfRsiValues the number of trading days to calculate the RSI value.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public RelativeStrengthIndexCalculator(final int lookback, final int daysOfRsiValues, final Validator validator,
	        final MathContext mathContext) {
		this.relativeStrengthIndexValues = new NonNullableArrayList<BigDecimal>();
		this.relativeStrengthValues = new NonNullableArrayList<BigDecimal>();
		this.minimumNumberOfPrices = lookback + daysOfRsiValues;
		this.smoothingConstant = calculateSmoothingConstant(lookback);
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public List<BigDecimal> rsi( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, minimumNumberOfPrices);

		relativeStrengthIndexValues.clear();
		relativeStrengthValues.clear();

		/* For the first zero - time period entries calculate the SMA based on up to down movement
		 * Upwards movement upward = closeToday - closeYesterday downward = 0 Downwards movement
		 * upward = closeYesterday - closeToday */
		final int startRsiIndex = 0;
		ClosingPrice closeToday;
		ClosingPrice closeYesterday = data[startRsiIndex].getClosingPrice();
		BigDecimal upward = BigDecimal.ZERO;
		BigDecimal downward = BigDecimal.ZERO;

		final int endInitialLookback = startRsiIndex + lookback;
		for (int i = startRsiIndex; i < endInitialLookback; i++) {
			closeToday = data[i].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				// Today's price is higher then yesterdays
				case 1:
					upward = upward.add(closeToday.subtract(closeYesterday, mathContext));
				break;

				// Today's price is lower then yesterdays
				case -1:
					downward = downward.add(closeYesterday.subtract(closeToday, mathContext));
				break;

				// When equal there's no movement, both are zero
				case 0:
				default:
				break;
			}

			closeYesterday = closeToday;
		}

		// Dividing by the number of time periods for a SMA
		// Reduce lookup by one, as the initial value is neither up or down
		upward = upward.divide(BigDecimal.valueOf(lookback - 1), mathContext);
		downward = downward.divide(BigDecimal.valueOf(lookback - 1), mathContext);

		/* RS = EMA(U,n) / EMA(D,n) (smoothing constant) multiplier: (2 / (Time periods + 1) ) EMA:
		 * {Close - EMA(previous day)} x multiplier + EMA(previous day). */
		for (int i = endInitialLookback; i < data.length; i++) {

			closeToday = data[i].getClosingPrice();
			closeYesterday = data[i - 1].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				// Today's price is higher then yesterdays
				case 1:
					upward = (closeToday.subtract(closeYesterday, mathContext).subtract(upward, mathContext))
					        .multiply(smoothingConstant, mathContext).add(upward, mathContext);

					downward = downward.negate().multiply(smoothingConstant, mathContext).add(downward, mathContext);
				break;

				// Today's price is lower then yesterdays
				case -1:
					upward = upward.negate().multiply(smoothingConstant, mathContext).add(upward, mathContext);

					downward = (closeYesterday.subtract(closeToday, mathContext).subtract(downward, mathContext))
					        .multiply(smoothingConstant, mathContext).add(downward, mathContext);
				break;

				// When equal there's no movement, both are zero
				case 0:
				default:
				break;
			}

			// When downward approaches zero, RSI approaches 100
			if (downward.compareTo(BigDecimal.ZERO) <= 0) {
				relativeStrengthValues.add(ONE_HUNDRED);
			} else {
				relativeStrengthValues.add(upward.divide(downward, mathContext));
			}
		}

		/* RSI = 100 / 1 + RS */
		for (final BigDecimal rs : relativeStrengthValues) {
			relativeStrengthIndexValues
			        .add(ONE_HUNDRED.subtract(ONE_HUNDRED.divide(BigDecimal.ONE.add(rs), mathContext)));
		}

		return relativeStrengthIndexValues;
	}

	private BigDecimal calculateSmoothingConstant( final int lookback ) {
		return BigDecimal.valueOf(2d / (lookback + 1));
	}
}
