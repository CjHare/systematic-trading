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
 * This implementation uses only the closing price data, 
 * applying the EMA style smoothing in the calculation of the relative strength (J. Welles Wilder approach),
 * rather then Culter's SMA approach.
 * 
 * Until there has been at least one upwards movements in the data, RS value will be zero.
 * 
 * @author CJ Hare
 */
public class ClosingPriceRelativeStrengthCalculator implements RelativeStrengthIndicator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Comparator result for today > yesterday closing price. */
	private static final int PRICE_TODAY_IS_HIGHER = 1;

	/** Comparator result for today < yesterday closing price. */
	private static final int PRICE_YESTERDAY_WAS_HIGHER = -1;

	/** Comparator result for today == yesterday closing price. */
	private static final int NO_PRICE_MOVEMENT = 0;

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
	public ClosingPriceRelativeStrengthCalculator( final int lookback, final Validator validator ) {
		this.lookback = lookback;
		this.validator = validator;
		this.history = BigDecimal.valueOf(lookback);
		this.archive = BigDecimal.valueOf(lookback - 1L);

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
	private AverageGainToLoss windup( final TradingDayPrices[] data ) {

		// Calculate the starting values via SMA
		final AverageGainToLoss initialLookback = new AverageGainToLoss(history, MATH_CONTEXT);

		ClosingPrice closeYesterday = data[0].getClosingPrice();
		ClosingPrice closeToday;

		for (int i = 1; i < lookback; i++) {
			closeToday = data[i].getClosingPrice();

			switch (closeToday.compareTo(closeYesterday)) {

				case PRICE_TODAY_IS_HIGHER:
					initialLookback.addGain(closeToday.subtract(closeYesterday, MATH_CONTEXT));
				break;

				case PRICE_YESTERDAY_WAS_HIGHER:
					initialLookback.addLoss(closeYesterday.subtract(closeToday, MATH_CONTEXT));
				break;

				case NO_PRICE_MOVEMENT:
				default:
				break;
			}

			closeYesterday = closeToday;
		}

		return initialLookback;
	}

	/**
	 * RS Calculation being:
	 * 	Archive = look back - 1
	 * 	Average Gain = [(previous Average Gain) x archive + current Gain] / lookback.
	 * 	Average Loss = [(previous Average Loss) x archive + current Loss] / lookback.
	 */
	private RelativeStrengthLine rs( final TradingDayPrices[] data, final AverageGainToLoss initialLookback ) {
		BigDecimal averageGain = initialLookback.getAverageGain();
		BigDecimal averageLoss = initialLookback.getAverageLoss();
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

				case PRICE_TODAY_IS_HIGHER:
					currentGain = closeToday.subtract(closeYesterday, MATH_CONTEXT);
					currentLoss = BigDecimal.ZERO;
				break;

				case PRICE_YESTERDAY_WAS_HIGHER:
					currentGain = BigDecimal.ZERO;
					currentLoss = closeYesterday.subtract(closeToday, MATH_CONTEXT);
				break;

				case NO_PRICE_MOVEMENT:
				default:
					currentGain = BigDecimal.ZERO;
					currentLoss = BigDecimal.ZERO;
				break;
			}

			/**
			 * Wilder originally formulated the calculation of the moving average as: newval = (prevval * (period - 1) + newdata) / period. 
			 * This is fully equivalent to the exponential smoothing of a n-period smoothed moving average (SMMA). 
			 */
			averageGain = smooth(currentGain, averageGain);
			averageLoss = smooth(currentLoss, averageLoss);

			if (isZeroOrBelow(averageLoss)) {
				// There's no downward, then avoid dividing by zero
				relativeStrength = averageGain;
			} else {
				relativeStrength = averageGain.divide(averageLoss, MATH_CONTEXT);
			}

			rsLine.put(data[i].getDate(), relativeStrength);
		}

		return new RelativeStrengthLine(rsLine);
	}

	/**
	 * Apply the RS smoothing technique to include another value to the average.
	 */
	private BigDecimal smooth( final BigDecimal currentValue, final BigDecimal averageValue ) {
		return averageValue.multiply(archive, MATH_CONTEXT).add(currentValue, MATH_CONTEXT).divide(history,
		        MATH_CONTEXT);
	}

	private boolean isZeroOrBelow( final BigDecimal value ) {
		return value.compareTo(BigDecimal.ZERO) <= 0;
	}
}