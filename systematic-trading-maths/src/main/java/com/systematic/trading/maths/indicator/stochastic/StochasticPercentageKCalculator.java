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
package com.systematic.trading.maths.indicator.stochastic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestEquityPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.maths.indicator.Validator;

/**
 * %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
 * 
 * @author CJ Hare
 */
public class StochasticPercentageKCalculator implements StochasticPercentageK {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final BigDecimal ONE_HUNDREDTH = BigDecimal.valueOf(0.01);

	/** Required number of data points required for ATR calculation. */
	private final int minimumNumberOfPrices;

	/** Number of days to read the ranges on. */
	private final int lookback;

	/** Provides the array to store the result in. */
	private final List<BigDecimal> stochasticValues;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback the number of days to use when calculating the Stochastic%K.
	 * @param daysOfPercentageKValues the number of trading days to calculate the RSI value.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public StochasticPercentageKCalculator(final int lookback, final int daysOfPercentageKValues,
	        final Validator validator, final MathContext mathContext) {
		this.minimumNumberOfPrices = lookback + daysOfPercentageKValues;
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
		this.stochasticValues = new NonNullableArrayList<>();
	}

	@Override
	public List<BigDecimal> percentageK( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, minimumNumberOfPrices);

		stochasticValues.clear();

		int pkSmaIndex = 0;

		LowestPrice lowestLow;
		HighestEquityPrice highestHigh;
		ClosingPrice currentClose;
		pkSmaIndex += lookback;

		for (int i = pkSmaIndex; i < data.length; i++) {
			currentClose = data[i].getClosingPrice();
			lowestLow = lowestLow(data, i);
			highestHigh = highestHigh(data, i);

			stochasticValues.add(calculatePercentageK(lowestLow, highestHigh, currentClose));
		}

		return stochasticValues;
	}

	private BigDecimal calculatePercentageK( final LowestPrice lowestLow, final HighestEquityPrice highestHigh,
	        final ClosingPrice currentClose ) {
		// %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
		final BigDecimal pK = (currentCloseMinusLowestLow(lowestLow, currentClose)
		        .divide(highestHighMinusLowestLow(lowestLow, highestHigh), mathContext))
		                .multiply(ONE_HUNDRED, mathContext);

		// Cap output at 100
		return (pK.compareTo(ONE_HUNDRED) > 0) ? ONE_HUNDRED : pK;
	}

	private BigDecimal currentCloseMinusLowestLow( final LowestPrice lowestLow,
	        final ClosingPrice currentClose ) {
		return currentClose.subtract(lowestLow, mathContext);
	}

	private BigDecimal highestHighMinusLowestLow( final LowestPrice lowestLow,
	        final HighestEquityPrice highestHigh ) {
		if (lowestLow.isEqaul(highestHigh)) {
			return ONE_HUNDREDTH;
		}

		return highestHigh.getPrice().subtract(lowestLow.getPrice(), mathContext);
	}

	private LowestPrice lowestLow( final TradingDayPrices[] data, final int exclusiveEnd ) {
		final int inclusiveStart = exclusiveEnd - lookback;
		LowestPrice contender;
		LowestPrice lowest = data[inclusiveStart].getLowestPrice();

		for (int i = inclusiveStart + 1; i < exclusiveEnd; i++) {
			contender = data[i].getLowestPrice();
			if (contender.isLessThan(lowest)) {
				lowest = contender;
			}
		}

		return lowest;
	}

	private HighestEquityPrice highestHigh( final TradingDayPrices[] data, final int exclusiveEnd ) {
		final int inclusiveStart = exclusiveEnd - lookback;
		HighestEquityPrice contender;
		HighestEquityPrice highest = data[inclusiveStart].getHighestPrice();

		for (int i = inclusiveStart + 1; i < exclusiveEnd; i++) {
			contender = data[i].getHighestPrice();
			if (contender.isGreaterThan(highest)) {
				highest = contender;
			}
		}

		return highest;
	}
}
