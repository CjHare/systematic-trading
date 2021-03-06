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
package com.systematic.trading.strategy;

import java.time.Period;
import java.util.Optional;

import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.simulation.brokerage.BrokerageTransaction;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;

/**
 * A trading strategy that comprises of position sizing combined with entry and exit behaviour.
 * 
 * @author CJ Hare
 */
public interface Strategy {

	/**
	 * The period of time required to warm up, or initialise the indicators.
	 * 
	 * @return time period required before the start of the analysis is to begin.
	 */
	Period warmUpPeriod();

	/**
	 * Updates the trading logic with a subsequent trading point.
	 * 
	 * @param fees
	 *            the brokerage to execute the order with, and whose fees are to be included in the
	 *            transaction.
	 * @param cashAccount
	 *            currently available funds.
	 * @param data
	 *            next day of trading to add, also applying logic for trade signals.
	 * @return the order to place at the next opportunity, or <code>null</code> when no order is to
	 *         be placed.
	 */
	Optional<EquityOrder> entryTick( BrokerageTransactionFee fees, CashAccount cashAccount, TradingDayPrices data );

	/**
	 * Action to take on the order when the triggering conditions are met, however there are
	 * insufficient available funds.
	 * 
	 * @param order
	 *            that cannot be executed, due to lack of funds.
	 * @return action to take in this situation.
	 */
	EquityOrderInsufficientFundsAction actionOnInsufficentFunds( EquityOrder order );

	/**
	 * Updates the trading logic with a subsequent trading point and open positions.
	 * 
	 * @param broker
	 *            the positions currently open.
	 * @param data
	 *            next day of trading to add, also applying logic for trade signals.
	 * @return the order to place at the next opportunity, or <code>null</code> when no order is to
	 *         be placed.
	 */
	Optional<EquityOrder> exitTick( BrokerageTransaction broker, TradingDayPrices data );
}
