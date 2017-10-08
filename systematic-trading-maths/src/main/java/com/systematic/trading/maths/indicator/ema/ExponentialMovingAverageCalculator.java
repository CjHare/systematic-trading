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
 * Accuracy is only achieved after the spin up interval, in this implementation taken to be a round
 * of the look back.
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
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator( final int lookback, final Validator validator ) {
		// Look back provides one of the days of EMA values
		this.smoothingConstant = calculateSmoothingConstant(lookback);
		this.validator = validator;
		this.lookback = lookback;

		//TODO validate lookback > 0
	}

	@Override
	public int getMinimumNumberOfPrices() {
		return lookback;
	}

	@Override
	public ExponentialMovingAverageLine ema( final TradingDayPrices[] data ) {
		//TODO data != null
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		// With zero null entries the beginning is zero, then end last index
		final int startSmaIndex = 0;
		final int endEmaIndex = data.length - 1;

		return calculateEma(data, startSmaIndex, endEmaIndex);
	}

	@Override
	public ExponentialMovingAverageLine ema( final SortedMap<LocalDate, BigDecimal> data ) {

		//TODO equivalent of		validator.verifyZeroNullEntries(data);
		//TODO equivalent of		validator.verifyEnoughValues(data, lookback);

		return calculateEma(data);
	}

	private ExponentialMovingAverageLine calculateEma( final SortedMap<LocalDate, BigDecimal> data ) {
		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		int smaDataPoints = 0;
		BigDecimal simpleMovingAverage = BigDecimal.ZERO;
		BigDecimal yesterday = BigDecimal.ZERO;
		BigDecimal today;

		for (final Map.Entry<LocalDate, BigDecimal> entry : data.entrySet()) {

			if (isSmaCalculation(smaDataPoints)) {
				simpleMovingAverage = simpleMovingAverage.add(entry.getValue(), MATH_CONTEXT);
				smaDataPoints++;

				if (isSmaCalculationComplete(smaDataPoints)) {
					simpleMovingAverage = simpleMovingAverage.divide(BigDecimal.valueOf((long) smaDataPoints),
					        MATH_CONTEXT);

					ema.put(entry.getKey(), simpleMovingAverage);
					today = simpleMovingAverage;
				}

			} else {
				today = entry.getValue();

				/* EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) */
				ema.put(entry.getKey(), (today.subtract(yesterday, MATH_CONTEXT))
				        .multiply(smoothingConstant, MATH_CONTEXT).add(yesterday, MATH_CONTEXT));

				yesterday = today;
			}
		}

		return new ExponentialMovingAverageLine(ema);
	}

	private boolean isSmaCalculation( final int smaDataPoints ) {
		return smaDataPoints < lookback;
	}

	private boolean isSmaCalculationComplete( final int smaDataPoints ) {
		return smaDataPoints == lookback;
	}

	private ExponentialMovingAverageLine calculateEma( final TradingDayPrices[] data, final int startSmaIndex,
	        final int endEmaIndex ) {
		final SortedMap<LocalDate, BigDecimal> ema = new TreeMap<>();

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		BigDecimal simpleMovingAverage = BigDecimal.ZERO;

		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			simpleMovingAverage = simpleMovingAverage.add(data[i].getClosingPrice().getPrice(), MATH_CONTEXT);
		}

		simpleMovingAverage = simpleMovingAverage.divide(BigDecimal.valueOf((long) endSmaIndex - startSmaIndex),
		        MATH_CONTEXT);

		// First value is the moving average for yesterday
		ema.put(data[endSmaIndex - 1].getDate(), simpleMovingAverage);

		final int startEmaIndex = endSmaIndex;
		BigDecimal yesterday = simpleMovingAverage;
		BigDecimal today;

		// One SMA value and the <= in loop
		for (int i = startEmaIndex; i <= endEmaIndex; i++) {
			today = data[i].getClosingPrice().getPrice();

			/* EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) */
			ema.put(data[i].getDate(), (today.subtract(yesterday, MATH_CONTEXT))
			        .multiply(smoothingConstant, MATH_CONTEXT).add(yesterday, MATH_CONTEXT));

			yesterday = today;
		}

		return new ExponentialMovingAverageLine(ema);
	}

	private BigDecimal calculateSmoothingConstant( final int lookback ) {
		return BigDecimal.valueOf(2d / (lookback + 1));
	}
}