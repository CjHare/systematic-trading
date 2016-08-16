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
package com.systematic.trading.simulation.logic.trade;

import java.math.BigDecimal;

/**
 * Trade value with minimum and maximum thresholds.
 * 
 * @author CJ Hare
 */
public class BoundedTradeValue implements TradeValueLogic {

	/** Smallest value to trade. */
	private final TradeValueCalculator minimum;

	/** Smallest value to trade. */
	private final TradeValueCalculator maximum;

	public BoundedTradeValue(final TradeValueCalculator minimum, final TradeValueCalculator maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	public BigDecimal calculate( final BigDecimal funds ) {

		BigDecimal tradeValue = minimum.getTradeValue(funds);

		// If above the minimum there's a chance to use the percentage
		if (tradeValue.compareTo(funds) < 0) {

			final BigDecimal maximumTradeValue = maximum.getTradeValue(funds);

			// Only when the maximum is above the threshold, use it
			if (maximumTradeValue.compareTo(tradeValue) > 0) {
				tradeValue = maximumTradeValue;
			}
		}

		return tradeValue;
	}

	@Override
	public TradeValueCalculator getMinimumValue() {
		return minimum;
	}

	@Override
	public TradeValueCalculator getMaximumValue() {
		return maximum;
	}
}