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
package com.systematic.trading.backtest.brokerage.fees.impl;

import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.EIGHT_BASIS_POINTS;
import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.FIFTEEN;
import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.TEN;
import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.TEN_BASIS_POINTS;
import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.THIRTEEN;
import static com.systematic.trading.backtest.brokerage.impl.BrokerageFeeUtil.applyLargest;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.exception.UnsupportedEquityClass;

/**
 * Fees for the online broker Bell Direct.
 * 
 * @author CJ Hare
 */
public class BellDirectFeeStructure implements BrokerageFeeStructure {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext context;

	/**
	 * @param context math context defining the scale and precision to apply to operations.
	 */
	public BellDirectFeeStructure( final MathContext context ) {
		this.context = context;
	}

	@Override
	public BigDecimal calculateFee( final BigDecimal tradeValue, final EquityClass type, final int tradesThisMonth )
			throws UnsupportedEquityClass {

		final BigDecimal brokerage;

		switch (type) {
			case BOND:
			case STOCK:
				// Your first 10 trades per month = $15 or 0.1%
				if (tradesThisMonth < 11) {
					brokerage = applyLargest( tradeValue, FIFTEEN, TEN_BASIS_POINTS, context );
				}
				// Your 11th to 30th trades per month = $13 or 0.8%
				else if (tradesThisMonth < 31) {
					brokerage = applyLargest( tradeValue, THIRTEEN, EIGHT_BASIS_POINTS, context );
				}
				// Your 31st trade onwards per month = $10 or 0.8%
				else {
					brokerage = applyLargest( tradeValue, TEN, EIGHT_BASIS_POINTS, context );
				}

				break;
			default:
				throw new UnsupportedEquityClass( type );
		}

		return brokerage;
	}
}
