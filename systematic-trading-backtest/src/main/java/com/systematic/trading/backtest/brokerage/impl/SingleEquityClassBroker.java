/**
 * Copyright (c) 2015, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest.brokerage.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.exception.InsufficientEquitiesException;
import com.systematic.trading.backtest.order.OrderVolume;
import com.systematic.trading.backtest.order.Price;

/**
 * Handles execution of trades and maintains the balance of equities.
 * 
 * @author CJ Hare
 */
public class SingleEquityClassBroker implements Brokerage {

	/** Fee structure to apply to equity transactions. */
	private final BrokerageFees fees;

	/** Single type of equity class traded in. */
	private final EquityClass type;

	/** Number of equities held. */
	private BigDecimal balance = BigDecimal.ZERO;

	/** Month of the last trade, used in counting number of transactions in month. */
	private Month monthOfLastTrade;

	/** Counter for number of trades in the monthOfLastTrade. */
	private int tradesThisMonth = 0;

	public SingleEquityClassBroker( final BrokerageFees fees, final EquityClass type ) {
		this.fees = fees;
		this.type = type;
	}

	@Override
	public BigDecimal buy( final Price price, final OrderVolume volume, final LocalDate tradeDate ) {
		balance = balance.add( volume.getVolume() );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume() );

		if (monthOfLastTrade == tradeDate.getMonth()) {
			tradesThisMonth++;
		} else {
			monthOfLastTrade = tradeDate.getMonth();
			tradesThisMonth = 1;
		}

		return fees.calculateFee( tradeValue, type, tradesThisMonth );
	}

	@Override
	public BigDecimal sell( final Price price, final OrderVolume volume, final LocalDate tradeDate )
			throws InsufficientEquitiesException {
		balance = balance.subtract( volume.getVolume() );

		if (balance.compareTo( BigDecimal.ZERO ) < 0) {
			throw new InsufficientEquitiesException();
		}

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume() );

		if (monthOfLastTrade == tradeDate.getMonth()) {
			tradesThisMonth++;
		} else {
			monthOfLastTrade = tradeDate.getMonth();
			tradesThisMonth = 1;
		}

		return fees.calculateFee( tradeValue, type, tradesThisMonth );
	}
}
