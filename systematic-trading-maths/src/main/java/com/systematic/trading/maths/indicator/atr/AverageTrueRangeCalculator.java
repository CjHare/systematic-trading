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
package com.systematic.trading.maths.indicator.atr;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Standard ATR implementation.
 * 
 * @author CJ Hare
 */
public class AverageTrueRangeCalculator implements AverageTrueRange {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Constant used for multiplying the previous ATR in the average calculation. */
	private final BigDecimal priorMultiplier;

	/** Constant used for dividing during the average calculation. */
	private final BigDecimal lookbackDivider;

	/** Required number of data points required for ATR calculation. */
	private final int minimumNumberOfPrices;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback the number of days to use when calculating the ATR, also the number of days
	 *            prior to the averaging becoming correct.
	 * @param validator validates and parses input.
	 */
	public AverageTrueRangeCalculator( final int lookback, final Validator validator ) {
		this.minimumNumberOfPrices = lookback;
		this.priorMultiplier = BigDecimal.valueOf(lookback - 1L);
		this.lookbackDivider = BigDecimal.valueOf(lookback);
		this.validator = validator;
	}

	/**
	 * Difference between the high and low prices.
	 */
	private BigDecimal trueRangeMethodOne( final TradingDayPrices today ) {
		return today.getHighestPrice().subtract(today.getLowestPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * Difference between the today's high price and yesterday's close price.
	 */
	private BigDecimal trueRangeMethodTwo( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		return today.getHighestPrice().subtract(yesterday.getClosingPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * Difference between today's low price and yesterdays close price.
	 */
	private BigDecimal trueRangeMethodThree( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		return today.getLowestPrice().subtract(yesterday.getClosingPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * @return highest value of the three true range methods.
	 */
	private BigDecimal trueRange( final TradingDayPrices today, final TradingDayPrices yesterday ) {
		return trueRangeMethodOne(today).max(trueRangeMethodTwo(today, yesterday))
		        .max(trueRangeMethodThree(today, yesterday));
	}

	private BigDecimal average( final BigDecimal currentTrueRange, final BigDecimal priorAverageTrueRange ) {
		/* For a look back of 14: Current ATR = [(Prior ATR x 13) + Current TR] / 14 - Multiply the
		 * previous 14-day ATR by 13. - Add the most recent day's TR value. - Divide the total by 14 */
		return priorAverageTrueRange.multiply(priorMultiplier).add(currentTrueRange).divide(lookbackDivider,
		        MATH_CONTEXT);
	}

	@Override
	public AverageTrueRangeLine atr( final TradingDayPrices[] data ) {
		//TODO data != null
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, minimumNumberOfPrices);

		final SortedMap<LocalDate, BigDecimal> averageTrueRanges = new TreeMap<>();

		// For the first value just use the TR
		final BigDecimal firstTrueRange = trueRangeMethodOne(data[0]);
		averageTrueRanges.put(data[0].getDate(), firstTrueRange);

		// Starting ATR is just the first value
		BigDecimal priorAtr = firstTrueRange;
		BigDecimal atr;

		for (int i = 1; i < data.length; i++) {
			atr = average(trueRange(data[i], data[i - 1]), priorAtr);
			averageTrueRanges.put(data[i].getDate(), atr);
			priorAtr = atr;
		}

		return new AverageTrueRangeLine(averageTrueRanges);
	}
}