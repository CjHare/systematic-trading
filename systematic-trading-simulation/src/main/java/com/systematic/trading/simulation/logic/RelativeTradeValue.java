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
package com.systematic.trading.simulation.logic;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Trade value behaviour with a minimum threshold, with a relative percentage in amounts above.
 * 
 * @author CJ Hare
 */
public class RelativeTradeValue implements TradeValue {

	/** Smallest value to trade. */
	private final BigDecimal minimumTradeValue;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Percentage of funds to trade (range of 0-1). */
	private final BigDecimal maximumPercentage;

	public RelativeTradeValue( final BigDecimal minimumTradeValue, final BigDecimal maximumPercentage,
			final MathContext mathContext ) {
		this.minimumTradeValue = minimumTradeValue;
		this.maximumPercentage = maximumPercentage;
		this.mathContext = mathContext;
	}

	@Override
	public BigDecimal getTradeValue( final BigDecimal availableFunds ) {

		BigDecimal tradeValue = minimumTradeValue;

		// If above the minimum there's a chance to use the percentage
		if (minimumTradeValue.compareTo( availableFunds ) < 0) {

			final BigDecimal maximumTradeValue = availableFunds.multiply( maximumPercentage, mathContext );

			// Only when the maximum is above the threshold, use it
			if (maximumTradeValue.compareTo( minimumTradeValue ) > 0) {
				tradeValue = maximumTradeValue;
			}
		}

		return tradeValue;
	}
}
