/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.simulation.brokerage;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.data.price.Price;
import com.systematic.trading.simulation.brokerage.exception.InsufficientEquitiesException;
import com.systematic.trading.simulation.order.EquityOrderVolume;

/**
 * The broker performs the trading on a customers behalf, charging for privilege.
 * 
 * @author CJ Hare
 */
public interface BrokerageTransaction {

	/**
	 * Calculates the total cost of performing a purchase, no transaction is performed.
	 * 
	 * @param price mean price paid for the equity.
	 * @param volume number of equities being purchased.
	 * @param tradeDate date of execution.
	 * @return total cost of the trade that would deducted from the cash account for the transaction.
	 */
	BigDecimal cost( Price price, EquityOrderVolume volume, LocalDate tradeDate );

	/**
	 * Performs a purchase, applying the corresponding brokers fees.
	 * 
	 * @param price mean price paid for the equity.
	 * @param volume number of equities being purchased.
	 * @param tradeDate date of execution.
	 * @return total cost of the trade to be deducted from the cash account.
	 */
	void buy( Price price, EquityOrderVolume volume, LocalDate tradeDate );

	/**
	 * Performs a liquidation, applying the corresponding brokers fees.
	 * 
	 * @param price mean price paid for the equity.
	 * @param volume number of equities being sold.
	 * @param tradeDate date of execution.
	 * @throws InsufficientEquitiesException encountered when there are insufficient equities are
	 *             held.
	 * @return total funds acquired from the liquidation.
	 */
	BigDecimal sell( Price price, EquityOrderVolume volume, LocalDate tradeDate ) throws InsufficientEquitiesException;
}
