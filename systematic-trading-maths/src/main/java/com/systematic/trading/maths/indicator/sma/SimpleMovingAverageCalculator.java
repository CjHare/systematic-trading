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
package com.systematic.trading.maths.indicator.sma;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.store.IndicatorOutputStore;

/**
 * The mean for a consecutive set of numbers.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageCalculator implements SimpleMovingAverage {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Required number of data points required for SMA calculation. */
	private final int minimumNumberOfPrices;

	/** Number of days to average the value on. */
	private final int lookback;

	/** Provides the array to store the result in. */
	private final IndicatorOutputStore store;

	/** Responsible for parsing and validating the input. */
	private final IndicatorInputValidator validator;

	/**
	 * @param lookback the number of days to use when calculating the SMA.
	 * @param validator validates and parses input.
	 * @param store source for the storage array.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public SimpleMovingAverageCalculator( final int lookback, final IndicatorInputValidator validator,
			final IndicatorOutputStore store, final MathContext mathContext ) {
		this.minimumNumberOfPrices = lookback + 1;
		this.mathContext = mathContext;
		this.validator = validator;
		this.lookback = lookback;
		this.store = store;
	}

	@Override
	public BigDecimal[] sma( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		final BigDecimal[] smaValues = store.getStore( data.length );
		int startSmaIndex = validator.getFirstNonNullIndex( data, smaValues.length, minimumNumberOfPrices );

		// No values without the full look back range
		startSmaIndex += lookback;
		startSmaIndex--;

		final int endSmaIndex = validator.getLastNonNullIndex( data );

		// Start at the end and work towards the origin
		for (int i = endSmaIndex; i >= startSmaIndex; i--) {
			smaValues[i] = simpleAverage( i, data );
		}

		return smaValues;
	}

	/**
	 * Calculate the average from this value and the previous look back amount.
	 */
	private BigDecimal simpleAverage( final int startIndex, final TradingDayPrices[] data ) {
		BigDecimal average = data[startIndex].getClosingPrice().getPrice();
		final int endIndex = startIndex - lookback;

		for (int i = startIndex - 1; i > endIndex; i--) {
			average = average.add( data[i].getClosingPrice().getPrice() );
		}

		return average.divide( BigDecimal.valueOf( lookback ), mathContext );
	}
}
