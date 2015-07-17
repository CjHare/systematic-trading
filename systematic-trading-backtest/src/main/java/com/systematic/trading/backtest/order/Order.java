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
package com.systematic.trading.backtest.order;

import java.time.LocalDate;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.exception.OrderException;
import com.systematic.trading.data.DataPoint;

/**
 * The trading order that can be executed by a specific brokerage.
 * 
 * @author CJ Hare
 */
public interface Order {

	/**
	 * Whether the order has expire.
	 * 
	 * @param todaysTrading
	 *            the price action for today.
	 * @return <code>true</code> has expire and should not be executed, <code>false</code> otherwise.
	 */
	boolean isNotExpired( DataPoint todaysTrading );

	/**
	 * Whether the day's trading movement satisfied the execution criteria for the order.
	 * 
	 * @param todaysTrading
	 *            the price action for today.
	 * @return <code>true</code> the conditions are met, <code>false</code> otherwise.
	 */
	boolean areExecutionConditionsMet( DataPoint todaysTrading );

	/**
	 * Executes the trade, side affecting the broker and cash account.
	 * 
	 * @param broker performs the execution of the order.
	 * @param cashAccount where the money for the transaction is withdrawn.
	 * @param tradeDate date of execution.
	 * @throws OrderException when the order fails.
	 */
	void execute( Brokerage broker, CashAccount cashAccount, LocalDate tradeDate ) throws OrderException;
}
