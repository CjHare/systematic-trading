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
package com.systematic.trading.maths.indicator;

import java.math.BigDecimal;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;

/**
 * ATR implementation with restriction on the upper bounds for the size of values to calculate the
 * average true range on.
 * 
 * @author CJ Hare
 */
public class ReuseIndicatorOutputStore implements IndicatorOutputStore {

	/** Maximum number of trading days to calculate on. */
	private final int maximum;

	/** Reused array to hold the indicator values. */
	private final BigDecimal[] store;

	/**
	 * @param maximum number of days to that will be provided to the indicator calculator.
	 */
	public ReuseIndicatorOutputStore( final int maximum ) {
		this.maximum = maximum;
		this.store = new BigDecimal[maximum];
	}

	@Override
	public BigDecimal[] getStore( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		// Restrict on the number of trading days
		if (data.length > maximum) {
			throw new TooManyDataPoints( String.format( "At most %s data points are needed, however %s are given",
					maximum, data.length ) );
		}

		return store;
	}
}
