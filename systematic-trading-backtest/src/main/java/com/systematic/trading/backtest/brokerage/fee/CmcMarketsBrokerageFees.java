/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.brokerage.fee;

import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.EIGHT_BASIS_POINTS;
import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.ELEVEN;
import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.NINE_NINTY;
import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.SEVENTY_FIVE_BASIS_POINTS;
import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.TEN_BASIS_POINTS;
import static com.systematic.trading.backtest.brokerage.fee.BrokerageFeeUtil.applyLargest;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Fees for the online broker Bell Direct.
 * 
 * @author CJ Hare
 */
public class CmcMarketsBrokerageFees implements BrokerageTransactionFeeStructure {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	@Override
	public BigDecimal cost( final BigDecimal tradeValue, final int tradesThisMonth ) {

		return stock(tradeValue, tradesThisMonth);
	}

	private BigDecimal stock( final BigDecimal tradeValue, final int tradesThisMonth ) {

		final BigDecimal brokerage;

		// Your first 10 trades per month = $11 or 0.1%
		if (tradesThisMonth < 11) {
			brokerage = applyLargest(tradeValue, ELEVEN, TEN_BASIS_POINTS, MATH_CONTEXT);
		}
		// Your 11th to 30th trades per month = $9.90 or 0.8%
		else if (tradesThisMonth < 31) {
			brokerage = applyLargest(tradeValue, NINE_NINTY, EIGHT_BASIS_POINTS, MATH_CONTEXT);
		}
		// Your 31st trade onwards per month = $9.90 or 0.75%
		else {
			brokerage = applyLargest(tradeValue, NINE_NINTY, SEVENTY_FIVE_BASIS_POINTS, MATH_CONTEXT);
		}

		return brokerage;
	}
}
