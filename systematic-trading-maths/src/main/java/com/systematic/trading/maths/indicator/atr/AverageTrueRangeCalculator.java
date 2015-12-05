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
package com.systematic.trading.maths.indicator.atr;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorOutputStore;

/**
 * Standard ATR implementation.
 * 
 * @author CJ Hare
 */
public class AverageTrueRangeCalculator implements AverageTrueRange {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Constant used for multiplying the previous ATR in the average calculation. */
	private final BigDecimal priorMultiplier;

	/** Constant used for dividing during the average calculation. */
	private final BigDecimal lookbackDivider;

	/** Required number of data points required for ATR calculation. */
	private final int minimumNumberOfPrices;

	/** Provides the array to store the result in. */
	private final IndicatorOutputStore store;

	/**
	 * @param lookback the number of days to use when calculating the ATR, also the number of days
	 *            prior to the averaging becoming correct.
	 * @param store source for the storage array.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public AverageTrueRangeCalculator( final int lookback, final IndicatorOutputStore store,
			final MathContext mathContext ) {
		this.priorMultiplier = BigDecimal.valueOf( lookback - 1 );
		this.lookbackDivider = BigDecimal.valueOf( lookback );
		this.mathContext = mathContext;
		this.minimumNumberOfPrices = lookback + 1;
		this.store = store;
	}

	private BigDecimal trueRangeMethodOne( final TradingDayPrices today ) {
		return today.getHighestPrice().subtract( today.getLowestPrice(), mathContext ).abs();
	}

	private BigDecimal trueRangeMethodTwo( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		return today.getHighestPrice().subtract( yesterday.getClosingPrice(), mathContext ).abs();
	}

	private BigDecimal trueRangeMethodThree( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		return today.getLowestPrice().subtract( yesterday.getClosingPrice(), mathContext ).abs();
	}

	/**
	 * @return highest value of the three true range methods.
	 */
	private BigDecimal getTrueRange( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		final BigDecimal one = trueRangeMethodOne( today );
		final BigDecimal two = trueRangeMethodTwo( today, yesterday );
		final BigDecimal three = trueRangeMethodThree( today, yesterday );

		if (one.compareTo( two ) >= 0 && one.compareTo( three ) >= 0) {
			return one;
		}

		if (two.compareTo( three ) >= 0) {
			return two;
		}

		return three;
	}

	private BigDecimal average( final BigDecimal currentTrueRange, final BigDecimal priorAverageTrueRange ) {
		/* For a look back of 14: Current ATR = [(Prior ATR x 13) + Current TR] / 14 - Multiply the
		 * previous 14-day ATR by 13. - Add the most recent day's TR value. - Divide the total by 14 */
		return priorAverageTrueRange.multiply( priorMultiplier ).add( currentTrueRange ).divide( lookbackDivider,
				mathContext );
	}

	@Override
	public BigDecimal[] atr( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		final BigDecimal[] atrValues = store.getStore( data );

		// Expecting the same number of input data points as outputs
		if (data.length != atrValues.length) {
			throw new IllegalArgumentException(
					String.format( "The number of data points given: %s does not match the expected size: %s",
							data.length, atrValues.length ) );
		}

		// Skip any null entries
		int startAtrIndex = 0;
		while (isNullEntryWithinArray( data, startAtrIndex )) {
			startAtrIndex++;
		}

		// Enough data to calculate ATR?
		if (data.length - startAtrIndex < minimumNumberOfPrices) {
			throw new TooFewDataPoints(
					String.format( "At least %s non null data points for Average True Range, only %s given",
							minimumNumberOfPrices, data.length ) );
		}

		// Initialise the return array with null to the start ATR
		for (int i = 0; i < startAtrIndex; i++) {
			atrValues[i] = null;
		}

		// For the first value just use the TR
		atrValues[startAtrIndex] = trueRangeMethodOne( data[startAtrIndex] );

		// Starting ATR is just the first value
		BigDecimal priorAtr = atrValues[startAtrIndex];

		for (int i = startAtrIndex + 1; i < atrValues.length; i++) {
			atrValues[i] = average( getTrueRange( data[i], data[i - 1] ), priorAtr );
			priorAtr = atrValues[i];
		}

		return atrValues;
	}

	private boolean isNullEntryWithinArray( final TradingDayPrices[] data, final int index ) {
		return (index < data.length) && (data[index] == null);
	}
}
