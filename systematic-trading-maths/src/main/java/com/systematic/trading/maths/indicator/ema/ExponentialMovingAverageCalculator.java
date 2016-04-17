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
package com.systematic.trading.maths.indicator.ema;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
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
	private final MathContext mathContext;

	/** Constant used for smoothing the moving average. */
	private final BigDecimal smoothingConstant;

	/** Provides the array to store the result in. */
	private final List<BigDecimal> emaValues;

	/** The number of previous data points used in EMA calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** Provides generic access to different array types. */
	private final Data wrapper;

	/**
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator(final int lookback, final Validator validator,
	        final MathContext mathContext) {
		// Look back provides one of the days of EMA values
		this.smoothingConstant = calculateSmoothingConstant(lookback);
		this.mathContext = mathContext;
		this.emaValues = new NonNullableArrayList<BigDecimal>();
		this.validator = validator;
		this.lookback = lookback;
		this.wrapper = new Data();
	}

	@Override
	public int getMinimumNumberOfPrices() {
		return lookback;
	}

	@Override
	public List<BigDecimal> ema( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		// With zero null entries the beginning is zero, then end last index
		final int startSmaIndex = 0;
		final int endEmaIndex = data.length - 1;

		wrapper.set(data);

		return ema(wrapper, startSmaIndex, endEmaIndex);
	}

	private BigDecimal calculateSmoothingConstant( final int lookback ) {
		return BigDecimal.valueOf(2d / (lookback + 1));
	}

	@Override
	public List<BigDecimal> ema( final List<BigDecimal> data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, lookback);

		wrapper.set(data);

		return ema(wrapper, 0, data.size() - 1);
	}

	private List<BigDecimal> ema( final Data data, final int startSmaIndex, final int endEmaIndex ) {

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		BigDecimal simpleMovingAverage = BigDecimal.ZERO;

		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			simpleMovingAverage = simpleMovingAverage.add(data.getPrice(i), mathContext);
		}

		simpleMovingAverage = simpleMovingAverage.divide(BigDecimal.valueOf((long) endSmaIndex - startSmaIndex),
		        mathContext);

		final int startEmaIndex = endSmaIndex;
		BigDecimal yesterday = simpleMovingAverage;
		BigDecimal today;

		// Empty the return store, populating with the look back SMA
		emaValues.clear();
		emaValues.add(yesterday);

		for (int i = startEmaIndex; i <= endEmaIndex; i++) {
			today = data.getPrice(i);

			/* EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) */
			emaValues.add((today.subtract(yesterday, mathContext)).multiply(smoothingConstant, mathContext)
			        .add(yesterday, mathContext));

			yesterday = today;
		}

		return emaValues;
	}

	class Data {
		private List<BigDecimal> dataDecimal;
		private TradingDayPrices[] dataPrices;

		public void set( final List<BigDecimal> data ) {
			this.dataDecimal = data;
			this.dataPrices = null;
		}

		public void set( final TradingDayPrices[] data ) {
			this.dataDecimal = null;
			this.dataPrices = data;
		}

		public BigDecimal getPrice( final int index ) {
			return dataDecimal == null ? dataPrices[index].getClosingPrice().getPrice() : dataDecimal.get(index);
		}
	}
}
