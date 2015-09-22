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
package com.systematic.trading.backtest.logic;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.data.TradingDayPrices;

/**
 * Encapsulates the trading behaviour that decides whether an action shall be taken given a rolling
 * set of data, focusing on the purchasing actions rather then the liquidation action.
 * 
 * @author CJ Hare
 */
public interface EntryLogic {

	/**
	 * Updates the trading logic with a subsequent trading point.
	 * 
	 * @param fees the brokerage to execute the order with, and whose fees are to be included in
	 *            the transaction.
	 * @param cashAccount currently available funds.
	 * @param data next day of trading to add, also applying logic for trade signals.
	 * @return the order to place at the next opportunity, or <code>null</code> when no order is to
	 *         be placed.
	 */
	EquityOrder update( BrokerageFees fees, CashAccount cashAccount, TradingDayPrices data );

	/**
	 * Action to take on the order when the triggering conditions are met, however there are
	 * insufficient available funds.
	 * 
	 * @param order that cannot be executed, due to lack of funds.
	 * @return action to take in this situation.
	 */
	EquityOrderInsufficientFundsAction actionOnInsufficentFunds( EquityOrder order );
}
