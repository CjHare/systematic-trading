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

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * Standard ATR implementation.
 * 
 * Example calculation applied to a lookback of 14
 * 
 * Current ATR = [(Prior ATR x 13) + Current TR] / 14 - Multiply the previous 14-day ATR by 13. -
 * Add the most recent day's TR value. - Divide the total by 14
 * 
 * For the period of the lookback there are not ATR, with the first value being the average of the
 * lookback TR values.
 * 
 * @author CJ Hare
 */
public class AverageTrueRangeCalculator implements AverageTrueRangeIndicator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Constant used for multiplying the previous ATR in the average calculation. */
	private final BigDecimal priorMultiplier;

	/** Constant used for dividing during the average calculation. */
	private final BigDecimal lookbackDivider;

	/** Required number of data points required for ATR calculation. */
	private final int lookback;

	/** The least number of prices to calculate the ATR on. */
	private final int minimumNumberOfPrices;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback
	 *            the number of days to use when calculating the ATR, also the number of days prior
	 *            to the averaging becoming correct.
	 * @param validator
	 *            validates and parses input.
	 */
	public AverageTrueRangeCalculator( final int lookback, final Validator validator ) {
		validator.verifyGreaterThan(1, lookback);

		this.lookback = lookback;
		this.validator = validator;
		this.priorMultiplier = BigDecimal.valueOf(lookback - 1L);
		this.lookbackDivider = BigDecimal.valueOf(lookback);
		this.minimumNumberOfPrices = lookback + 1;
	}

	@Override
	public int minimumNumberOfPrices() {

		return minimumNumberOfPrices;
	}

	@Override
	public AverageTrueRangeLine calculate( final TradingDayPrices[] data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		final SortedMap<LocalDate, BigDecimal> averageTrueRanges = new TreeMap<>();

		// For the first value just use the TR
		final BigDecimal firstTrueRange = calculateFirstAtr(data);
		averageTrueRanges.put(data[lookback - 1].date(), firstTrueRange);

		// Starting ATR is just the first value
		BigDecimal priorAtr = firstTrueRange;
		BigDecimal atr;

		for (int i = lookback; i < data.length; i++) {
			atr = average(trueRange(data[i], data[i - 1]), priorAtr);
			averageTrueRanges.put(data[i].date(), atr);
			priorAtr = atr;
		}

		return new AverageTrueRangeLine(averageTrueRanges);
	}

	/**
	 * First ATR is the average of the TR for the first lookback period.
	 */
	private BigDecimal calculateFirstAtr( final TradingDayPrices[] data ) {

		BigDecimal totalTrueRange = trueRangeMethodOne(data[0]);

		for (int i = 1; i < lookback; i++) {
			totalTrueRange = totalTrueRange.add(trueRange(data[i], data[i - 1]));
		}

		return totalTrueRange.divide(lookbackDivider, MATH_CONTEXT);
	}

	/**
	 * Difference between the high and low prices.
	 */
	private BigDecimal trueRangeMethodOne( final TradingDayPrices today ) {

		return today.highestPrice().subtract(today.lowestPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * Difference between the today's high price and yesterday's close price.
	 */
	private BigDecimal trueRangeMethodTwo( final TradingDayPrices today, final TradingDayPrices yesterday ) {

		return today.highestPrice().subtract(yesterday.closingPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * Difference between today's low price and yesterdays close price.
	 */
	private BigDecimal trueRangeMethodThree( final TradingDayPrices today, final TradingDayPrices yesterday ) {

		return today.lowestPrice().subtract(yesterday.closingPrice(), MATH_CONTEXT).abs();
	}

	/**
	 * @return highest value of the three true range methods.
	 */
	private BigDecimal trueRange( final TradingDayPrices today, final TradingDayPrices yesterday ) {

		return trueRangeMethodOne(today).max(trueRangeMethodTwo(today, yesterday))
		        .max(trueRangeMethodThree(today, yesterday));
	}

	private BigDecimal average( final BigDecimal currentTrueRange, final BigDecimal priorAverageTrueRange ) {

		/*
		 * For a look back of 14: Current ATR = [(Prior ATR x 13) + Current TR] / 14 - Multiply the
		 * previous 14-day ATR by 13. - Add the most recent day's TR value. - Divide the total by 14
		 */
		return priorAverageTrueRange.multiply(priorMultiplier).add(currentTrueRange).divide(lookbackDivider,
		        MATH_CONTEXT);
	}

}