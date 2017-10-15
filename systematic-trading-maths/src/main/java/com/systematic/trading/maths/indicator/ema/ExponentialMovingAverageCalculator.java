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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Exponential Moving Average (EMA) implementation without restriction on maximum number of trading
 * days to analyse.
 * <p/>
 * An exponential moving average (EMA) is a type of infinite impulse response filter that applies
 * weighting factors which decrease exponentially. The weighting for each older datum decreases
 * exponentially, never reaching zero.
 * <p/>
 * Greater accuracy is achieved with more data points, with the days of gradient being larger, the EMA becomes more accurate.
 * However with more data, more computation is required, meaning a balance between volume of data and accuracy is needed.
 * 
 * Calculates the EMA by first calculating the starting value using a SMA, then applies each value with the smoothing constant to produce the EMA. 
 * This does mean those dates used as part of the SMA will not have corresponding EMA values.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageCalculator implements ExponentialMovingAverage {

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
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param daysOfEmaValues the minimum number of EMA values to produce.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator( final int lookback, final int daysOfEmaValues,
	        final Validator validator ) {
		// Look back provides one of the days of EMA values
		this.smoothingConstant = calculateSmoothingConstant(lookback);
		this.validator = validator;
		this.lookback = lookback;

		validator.verifyGreaterThan(1, lookback);
		validator.verifyGreaterThan(1, daysOfEmaValues);

		//TODO add days of values
		this.minimumNumberOfPrices = 2 * lookback + daysOfEmaValues;
	}

	@Override
	public int getMinimumNumberOfPrices() {
		return minimumNumberOfPrices;
	}

	@Override
	public ExponentialMovingAverageLine calculate( final TradingDayPrices[] data ) {
		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		// With zero null entries the beginning is zero, then end last index
		return calculateEma(data, 0, data.length - 1);
	}

	@Override
	public ExponentialMovingAverageLine calculate( final SortedMap<LocalDate, BigDecimal> data ) {
		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data.values());
		validator.verifyEnoughValues(data.values(), lookback);

		return calculateEma(data);
	}

	private ExponentialMovingAverageLine calculateEma( final SortedMap<LocalDate, BigDecimal> data ) {
		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		int smaDataPointCount = 0;
		BigDecimal smaSum = BigDecimal.ZERO;
		BigDecimal yesterday = BigDecimal.ZERO;
		BigDecimal today;

		for (final Map.Entry<LocalDate, BigDecimal> entry : data.entrySet()) {

			if (isSmaCalculation(smaDataPointCount)) {
				smaSum = smaSum.add(entry.getValue(), MATH_CONTEXT);
				smaDataPointCount++;

				if (isSmaCalculationComplete(smaDataPointCount)) {
					today = getSma(smaSum);
					ema.put(entry.getKey(), today);
					yesterday = today;
				}

			} else {
				today = entry.getValue();
				ema.put(entry.getKey(), getEma(yesterday, today));
				yesterday = today;
			}
		}

		return new ExponentialMovingAverageLine(ema);
	}

	private ExponentialMovingAverageLine calculateEma( final TradingDayPrices[] data, final int startSmaIndex,
	        final int endEmaIndex ) {
		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		BigDecimal smaSum = BigDecimal.ZERO;

		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			smaSum = smaSum.add(data[i].getClosingPrice().getPrice(), MATH_CONTEXT);
		}

		smaSum = getSma(smaSum);

		// First value is the moving average for yesterday
		ema.put(data[endSmaIndex - 1].getDate(), smaSum);

		final int startEmaIndex = endSmaIndex;
		BigDecimal yesterday = smaSum;
		BigDecimal today;

		// One SMA value and the <= in loop
		for (int i = startEmaIndex; i <= endEmaIndex; i++) {
			today = data[i].getClosingPrice().getPrice();
			ema.put(data[i].getDate(), getEma(yesterday, today));
			yesterday = today;
		}

		return new ExponentialMovingAverageLine(ema);
	}

	/**
	 * EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) 
	 */
	private BigDecimal getEma( final BigDecimal yesterday, final BigDecimal today ) {
		return today.subtract(yesterday, MATH_CONTEXT).multiply(smoothingConstant, MATH_CONTEXT).add(yesterday,
		        MATH_CONTEXT);

	}

	private BigDecimal getSma( final BigDecimal sum ) {
		return sum.divide(BigDecimal.valueOf(lookback), MATH_CONTEXT);
	}

	/**
	 * 2 / numberOfValues + 1
	 */
	private BigDecimal calculateSmoothingConstant( final int lookback ) {
		return BigDecimal.valueOf(2d / (lookback + 1));
	}

	private boolean isSmaCalculation( final int smaDataPoints ) {
		return smaDataPoints < lookback;
	}

	private boolean isSmaCalculationComplete( final int smaDataPoints ) {
		return smaDataPoints == lookback;
	}
}