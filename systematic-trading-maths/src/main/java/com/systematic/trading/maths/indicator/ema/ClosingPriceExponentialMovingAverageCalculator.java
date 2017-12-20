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
package com.systematic.trading.maths.indicator.ema;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;

/**
 * This implementation calculates the EMA from the closing price, by first calculating the starting value using a SMA,
 * then applies each value with the smoothing constant to produce the EMA.
 * This does mean those dates used as part of the SMA will not have corresponding EMA values,
 * with those in the first period of the lookback being considered as inaccurate, not appropriate for use in signal
 * generation.
 * 
 * @author CJ Hare
 */
public class ClosingPriceExponentialMovingAverageCalculator implements ExponentialMovingAverageIndicator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Constant used for smoothing the moving average. */
	private final BigDecimal smoothingConstant;

	/** Number of prices needed for the wind up and days for EMA values to produce. */
	private final int minimumNumberOfPrices;

	/** The number of previous data points used in EMA calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback
	 *            the number of days to use when calculating the EMA.
	 * @param daysOfEmaValues
	 *            the minimum number of EMA values to produce.
	 * @param validator
	 *            validates and parses input.
	 * @param mathContext
	 *            the scale, precision and rounding to apply to mathematical operations.
	 */
	public ClosingPriceExponentialMovingAverageCalculator( final int lookback, final int daysOfEmaValues,
	        final Validator validator ) {
		validator.verifyGreaterThan(1, lookback);
		validator.verifyGreaterThan(1, daysOfEmaValues);

		this.minimumNumberOfPrices = lookback + daysOfEmaValues;
		this.smoothingConstant = smoothingConstant(lookback);
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public int minimumNumberOfPrices() {

		return minimumNumberOfPrices;
	}

	@Override
	public ExponentialMovingAverageLine calculate( final TradingDayPrices[] data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		// With zero null entries the beginning is zero, then end last index
		return ema(data, 0, data.length - 1);
	}

	private ExponentialMovingAverageLine ema( final TradingDayPrices[] data, final int startSmaIndex,
	        final int endEmaIndex ) {

		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		BigDecimal smaSum = BigDecimal.ZERO;

		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			smaSum = smaSum.add(data[i].closingPrice().getPrice(), MATH_CONTEXT);
		}

		smaSum = sma(smaSum);

		// First value is the moving average for yesterday
		ema.put(data[endSmaIndex - 1].date(), smaSum);

		final int startEmaIndex = endSmaIndex;
		BigDecimal emaValue = smaSum;

		// One SMA value and the <= in loop
		for (int i = startEmaIndex; i <= endEmaIndex; i++) {
			emaValue = ema(emaValue, data[i].closingPrice().getPrice());
			ema.put(data[i].date(), emaValue);
		}

		return new ExponentialMovingAverageLine(ema);
	}

	/**
	 * EMA {Close - EMA(previous day)} x multiplier + EMA(previous day)
	 */
	private BigDecimal ema( final BigDecimal yesterdayEma, final BigDecimal todayClose ) {

		return todayClose.subtract(yesterdayEma, MATH_CONTEXT).multiply(smoothingConstant, MATH_CONTEXT)
		        .add(yesterdayEma, MATH_CONTEXT);

	}

	private BigDecimal sma( final BigDecimal sum ) {

		return sum.divide(BigDecimal.valueOf(lookback), MATH_CONTEXT);
	}

	/**
	 * 2 / numberOfValues + 1
	 */
	private BigDecimal smoothingConstant( final int lookback ) {

		return BigDecimal.valueOf(2d / (lookback + 1));
	}
}