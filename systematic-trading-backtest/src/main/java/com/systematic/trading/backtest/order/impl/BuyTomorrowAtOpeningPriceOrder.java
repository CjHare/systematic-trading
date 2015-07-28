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
package com.systematic.trading.backtest.order.impl;

import java.math.BigDecimal;

import com.systematic.trading.backtest.brokerage.BrokerageTransaction;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.exception.OrderException;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.DataPoint;

/**
 * Placing an order to purchase an equity.
 * 
 * @author CJ Hare
 */
public class BuyTomorrowAtOpeningPriceOrder implements EquityOrder {

	/** Number of equities to purchase. */
	private final EquityOrderVolume volume;

	public BuyTomorrowAtOpeningPriceOrder( final EquityOrderVolume volume ) {
		this.volume = volume;
	}

	@Override
	public boolean isValid( final DataPoint todaysTrading ) {
		// Never expire
		return true;
	}

	@Override
	public boolean areExecutionConditionsMet( final DataPoint todaysTrading ) {
		// Buy irrespective of the date or price
		return true;
	}

	@Override
	public void execute( final BrokerageTransaction broker, final CashAccount cashAccount, final DataPoint todaysTrade )
			throws OrderException {		
		final BigDecimal totalCost = broker.buy( todaysTrade.getOpeningPrice(), volume, todaysTrade.getDate() );
		cashAccount.debit( totalCost, todaysTrade.getDate() );
	}

	public EquityOrderVolume getVolume() {
		return volume;
	}
}
