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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.store.IndicatorOutputStore;

/**
 * Exponential Moving Average (EMA) implementation without restriction on maximum number of trading
 * days to analyse.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageCalculator implements ExponentialMovingAverage {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Constant used for smoothing the moving average. */
	private final BigDecimal smoothingConstant;

	/** Provides the array to store the result in. */
	private final IndicatorOutputStore store;

	/** Required number of data points required for EMA calculation. */
	private final int minimumNumberOfPrices;

	/** The number of previous data points used in EMA calculation. */
	private final int lookback;

	/** Responsible for parsing and validating the input. */
	private final IndicatorInputValidator validator;

	/**
	 * @param lookback the number of days to use when calculating the EMA.
	 * @param validator validates and parses input.
	 * @param store source for the storage array.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public ExponentialMovingAverageCalculator( final int lookback, final IndicatorInputValidator validator,
			final IndicatorOutputStore store, final MathContext mathContext ) {
		this.minimumNumberOfPrices = lookback + 1;
		this.smoothingConstant = calculateSmoothingConstant( lookback );
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
		this.store = store;
	}

	@Override
	public BigDecimal[] ema( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		final BigDecimal[] emaValues = store.getStore( data.length );
		final int startSmaIndex = validator.getFirstNonNullIndex( data, emaValues.length, minimumNumberOfPrices );
		final int endEmaIndex = validator.getLastNonNullIndex( data );

		return ema( new Data( data ), startSmaIndex, endEmaIndex, emaValues );
	}

	private BigDecimal calculateSmoothingConstant( final int lookback ) {
		return BigDecimal.valueOf( 2d / (lookback + 1) );
	}

	@Override
	public BigDecimal[] ema( final BigDecimal[] data ) throws TooFewDataPoints, TooManyDataPoints {

		final BigDecimal[] emaValues = store.getStore( data.length );
		final int startSmaIndex = validator.getFirstNonNullIndex( data, emaValues.length, minimumNumberOfPrices );
		final int endEmaIndex = validator.getLastNonNullIndex( data );

		return ema( new Data( data ), startSmaIndex, endEmaIndex, emaValues );
	}

	private BigDecimal[] ema( final Data data, final int startSmaIndex, final int endEmaIndex,
			final BigDecimal[] emaValues ) {

		/* SMA for the initial time periods */
		final int endSmaIndex = startSmaIndex + lookback;
		BigDecimal simpleMovingAverage = BigDecimal.ZERO;

		for (int i = startSmaIndex; i < endSmaIndex; i++) {
			simpleMovingAverage = simpleMovingAverage.add( data.getPrice( i ), mathContext );
		}

		simpleMovingAverage = simpleMovingAverage.divide( BigDecimal.valueOf( endSmaIndex - startSmaIndex ),
				mathContext );

		/* EMA {Close - EMA(previous day)} x multiplier + EMA(previous day) */
		BigDecimal yesterday = simpleMovingAverage;
		BigDecimal today;

		for (int i = endSmaIndex; i <= endEmaIndex; i++) {
			today = data.getPrice( i );

			emaValues[i] = (today.subtract( yesterday, mathContext )).multiply( smoothingConstant, mathContext )
					.add( yesterday, mathContext );

			yesterday = today;
		}

		return emaValues;
	}

	class Data {
		private final BigDecimal[] dataDecimal;
		private final TradingDayPrices[] dataPrices;

		public Data( final BigDecimal[] data ) {
			this.dataDecimal = data;
			this.dataPrices = null;
		}

		public Data( final TradingDayPrices[] data ) {
			this.dataDecimal = null;
			this.dataPrices = data;
		}

		public BigDecimal getPrice( final int index ) {
			return dataDecimal == null ? dataPrices[index].getClosingPrice().getPrice() : dataDecimal[index];
		}
	}
}
