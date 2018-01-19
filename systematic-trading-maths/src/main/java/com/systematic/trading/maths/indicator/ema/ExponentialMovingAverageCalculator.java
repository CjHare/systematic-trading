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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.maths.indicator.Validator;

/**
 * Exponential Moving Average (EMA) implementation without restriction on maximum number of trading
 * days to analyse.
 * <p/>
 * An exponential moving average (EMA) is a type of infinite impulse response filter that applies
 * weighting factors which decrease exponentially. The weighting for each older datum decreases
 * exponentially, never reaching zero.
 * <p/>
 * Greater accuracy is achieved with more data points, with the days of gradient being larger, the
 * EMA becomes more accurate. However with more data, more computation is required, meaning a
 * balance between volume of data and accuracy is needed.
 * <p/>
 * This implementation calculates the EMA by first calculating the starting value using a SMA, then
 * applies each value with the smoothing constant to produce the EMA. This does mean those dates
 * used as part of the SMA will not have corresponding EMA values, with those in the first period of
 * the lookback being considered as inaccurate, not appropriate for use in signal generation.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageCalculator implements ExponentialMovingAverage {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Constant used for smoothing the moving average. */
	private final BigDecimal smoothingConstant;

	/** The number of previous data points used in EMA calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/**
	 * @param lookback
	 *            the number of days to use when calculating the EMA.
	 * @param validator
	 *            validates and parses input.
	 * @param mathContext
	 *            the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator( final int lookback, final Validator validator ) {

		validator.verifyGreaterThan(1, lookback);

		this.smoothingConstant = smoothingConstant(lookback);
		this.validator = validator;
		this.lookback = lookback;
	}

	@Override
	public ExponentialMovingAverageLine calculate( final SortedMap<LocalDate, BigDecimal> data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data.values());
		validator.verifyEnoughValues(data.values(), lookback);

		return ema(data);
	}

	private ExponentialMovingAverageLine ema( final SortedMap<LocalDate, BigDecimal> data ) {

		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		int smaDataPointCount = 0;
		BigDecimal smaSum = BigDecimal.ZERO;
		BigDecimal emaValue = BigDecimal.ZERO;

		for (final Map.Entry<LocalDate, BigDecimal> entry : data.entrySet()) {

			if (isSmaCalculation(smaDataPointCount)) {
				smaSum = smaSum.add(entry.getValue(), MATH_CONTEXT);
				smaDataPointCount++;

				if (isSmaCalculationComplete(smaDataPointCount)) {
					emaValue = sma(smaSum);
					ema.put(entry.getKey(), emaValue);
				}

			} else {
				emaValue = ema(emaValue, entry.getValue());
				ema.put(entry.getKey(), emaValue);
			}
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

	private boolean isSmaCalculation( final int smaDataPoints ) {

		return smaDataPoints < lookback;
	}

	private boolean isSmaCalculationComplete( final int smaDataPoints ) {

		return smaDataPoints == lookback;
	}
}