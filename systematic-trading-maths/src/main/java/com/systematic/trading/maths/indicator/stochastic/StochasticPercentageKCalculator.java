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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.store.IndicatorOutputStore;

/**
 * %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
 * 
 * @author CJ Hare
 */
public class StochasticPercentageKCalculator implements StochasticPercentageK {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );
	private static final BigDecimal ONE_HUNDREDTH = BigDecimal.valueOf( 0.01 );

	/** Required number of data points required for ATR calculation. */
	private final int minimumNumberOfPrices;

	/** Number of days to read the ranges on. */
	private final int lookback;

	/** Provides the array to store the result in. */
	private final IndicatorOutputStore store;

	/** Responsible for parsing and validating the input. */
	private final IndicatorInputValidator validator;

	/**
	 * @param lookback the number of days to use when calculating the Stochastic%K.
	 * @param daysOfPercentageKValues the number of trading days to calculate the RSI value.
	 * @param validator validates and parses input.
	 * @param store memory allocator for the result array.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public StochasticPercentageKCalculator( final int lookback, final int daysOfPercentageKValues,
			final IndicatorInputValidator validator, final IndicatorOutputStore store, final MathContext mathContext ) {
		this.minimumNumberOfPrices = lookback + daysOfPercentageKValues;
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
		this.store = store;
	}

	@Override
	public BigDecimal[] percentageK( final TradingDayPrices[] data ) throws TooManyDataPoints, TooFewDataPoints {

		final BigDecimal[] pK = store.getStore( data.length );
		int pkSmaIndex = validator.getStartingNonNullIndex( data, pK.length, minimumNumberOfPrices );
		final int pkEndIndex = validator.getLastNonNullIndex( data );

		LowestPrice lowestLow;
		HighestPrice highestHigh;
		ClosingPrice currentClose;
		BigDecimal lowestHighestDifference;
		pkSmaIndex += lookback;

		for (int i = pkSmaIndex; i <= pkEndIndex; i++) {
			currentClose = data[i].getClosingPrice();
			lowestLow = lowestLow( data, i );
			highestHigh = highestHigh( data, i );
			lowestHighestDifference = differenceBetweenHighestHighAndLowestLow( lowestLow, highestHigh );

			pK[i] = calculatePercentageK( lowestLow, highestHigh, currentClose, lowestHighestDifference );
		}

		return pK;
	}

	private BigDecimal calculatePercentageK( final LowestPrice lowestLow, final HighestPrice highestHigh,
			final ClosingPrice currentClose, final BigDecimal lowestHighestDifference ) {
		// %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
		final BigDecimal pK = ((currentClose.subtract( lowestLow, mathContext )).divide( lowestHighestDifference,
				mathContext )).multiply( ONE_HUNDRED, mathContext );

		// Cap output at 100
		return (pK.compareTo( ONE_HUNDRED ) > 0) ? ONE_HUNDRED : pK;
	}

	private BigDecimal differenceBetweenHighestHighAndLowestLow( final LowestPrice lowestLow,
			final HighestPrice highestHigh ) {
		if (lowestLow.isEqaul( highestHigh )) {
			return ONE_HUNDREDTH;
		}

		return highestHigh.getPrice().subtract( lowestLow.getPrice(), mathContext );
	}

	private LowestPrice lowestLow( final TradingDayPrices[] data, final int exclusiveEnd ) {
		final int inclusiveStart = exclusiveEnd - lookback;
		LowestPrice contender, lowest = data[inclusiveStart].getLowestPrice();

		for (int i = inclusiveStart + 1; i < exclusiveEnd; i++) {
			contender = data[i].getLowestPrice();
			if (contender.isLessThan( lowest )) {
				lowest = contender;
			}
		}

		return lowest;
	}

	private HighestPrice highestHigh( final TradingDayPrices[] data, final int exclusiveEnd ) {
		final int inclusiveStart = exclusiveEnd - lookback;
		HighestPrice contender, highest = data[inclusiveStart].getHighestPrice();

		for (int i = inclusiveStart + 1; i < exclusiveEnd; i++) {
			contender = data[i].getHighestPrice();
			if (contender.isGreaterThan( highest )) {
				highest = contender;
			}
		}

		return highest;
	}
}
