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
package com.systematic.trading.backtest;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.data.DataPoint;

/**
 * Encapsulates the trading behaviour that decides whether an action shall be taken given a rolling set of data.
 * 
 * @author CJ Hare
 */
public interface TradingLogic {

	/**
	 * Updates the trading logic with a subsequent trading point.
	 * 
	 * @param data
	 *            the next day of trading to add, also applying logic for trade signals.
	 * @return any signal generated from analysis after adding the given additional data point, <code>null</code> when
	 *         no signal is triggered.
	 */
	TradeSignal update( DataPoint data );

	/**
	 * Interprets a trading signal and given the current available funds and broker determines the order.
	 * 
	 * @param signal
	 *            the trade to action.
	 * @param cashAccount
	 *            the currently available funds.
	 * @param broker
	 *            the brokerage to execute the order with, and whose fees are to be included in the transaction.
	 * @return the order to place at the next opportunity, or <code>null</code> when no order is to be placed.
	 */
	TradingOrder createOrder( TradeSignal signal, CashAccount cashAccount, Brokerage broker );

	/**
	 * Action to take on the order when the triggering conditions are met, however there are insufficient available
	 * funds.
	 * 
	 * @param order
	 *            that cannot be executed, due to lack of funds.
	 * @return action to take in this situation.
	 */
	OrderInsufficientFundsAction insufficentFunds( TradingOrder order );
}
