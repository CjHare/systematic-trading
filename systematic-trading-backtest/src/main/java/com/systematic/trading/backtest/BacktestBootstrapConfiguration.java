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
package com.systematic.trading.backtest;

import java.time.LocalDate;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityIdentity;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;

/**
 * Configuration for a back testing bootstrap configuration.
 * <p/>
 * The inputs, including the entry and exit logic.
 * 
 * @author CJ Hare
 */
public interface BacktestBootstrapConfiguration {

	/**
	 * First day to begin the back testing.
	 * 
	 * @return inclusive date of the first trading day, or closest following day to begin back
	 *         testing.
	 */
	LocalDate getStartDate();

	/**
	 * Last day for the back testing.
	 * 
	 * @return inclusive date of the last trading day, or closest preceding day to cease back
	 *         testing.
	 */
	LocalDate getEndDate();

	/**
	 * Exit logic used to generate sell orders.
	 * 
	 * @return input to the simulation that provides sell orders.
	 */
	ExitLogic getExitLogic();

	/**
	 * Broker that handles equity transactions.
	 * 
	 * @param equity subject of the back testing.
	 * @return broker that executes buy and sell orders.
	 */
	Brokerage getBroker( EquityIdentity equity );

	/**
	 * Account that manages the cash.
	 * 
	 * @param openingDate date the cash account is opened, begins interest calculations.
	 * @return cash account to use during the simulation.
	 */
	CashAccount getCashAccount( LocalDate openingDate );

	/**
	 * Entry logic used to generate buy orders.
	 * 
	 * @param equity subject of the back testing.
	 * @param openingDate date the cash account is opened, begins interest calculations.
	 * @return input to the simulation that provides buy orders.
	 */
	EntryLogic getEntryLogic( EquityIdentity equity, LocalDate openingDate );

	/**
	 * Describes the behaviour of the configuration.
	 * 
	 * @return unique description of the configurations behaviour, a meaningful key.
	 */
	String getDescription();
}
