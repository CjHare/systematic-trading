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
package com.systematic.trading.backtest.context;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.strategy.Strategy;

/**
 * Context that a back testing will occurs within.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapContext {

	/** Applied to each equity transaction. */
	private final Brokerage brokerage;

	/** Cash account to use during the simulation. */
	private final CashAccount cashAccount;

	/** Decision maker for when to enter and exit trades. */
	private final Strategy tradingStrategy;

	/** Details of the simulation dates. */
	private final BacktestSimulationDates simulationDates;

	/**
	 * @param startDate
	 *            inclusive beginning date for the back testing.
	 * @param endDate
	 *            inclusive end date for back testing.
	 */
	public BacktestBootstrapContext( final Strategy tradingStrategy, final Brokerage brokerage,
	        final CashAccount cashAccount, final BacktestSimulationDates simulationDates ) {

		this.cashAccount = cashAccount;
		this.tradingStrategy = tradingStrategy;
		this.brokerage = brokerage;
		this.simulationDates = simulationDates;
	}

	/**
	 * Trading strategy that contains the entry and exit trading behaviour.
	 * 
	 * @return trading strategy, never <code>null</code>.
	 */
	public Strategy tradingStrategy() {

		return tradingStrategy;
	}

	/**
	 * Broker that handles equity transactions.
	 * 
	 * @return broker that executes buy and sell orders.
	 */
	public Brokerage broker() {

		return brokerage;
	}

	/**
	 * Account that manages the cash.
	 * 
	 * @return cash account to use during the simulation.
	 */
	public CashAccount cashAccount() {

		return cashAccount;
	}

	/**
	 * The date variables for the simulation.
	 * 
	 * @return everything about the simulation dates.
	 */
	public BacktestSimulationDates simulationDates() {

		return simulationDates;
	}
}