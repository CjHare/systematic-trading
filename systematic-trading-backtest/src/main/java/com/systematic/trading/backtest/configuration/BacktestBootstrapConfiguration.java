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
package com.systematic.trading.backtest.configuration;

import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;

/**
 * Configuration for a back testing bootstrap configuration.
 * <p/>
 * The inputs, including the entry and exit logic.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapConfiguration {

	/** Description used for uniquely identifying the configuration. */
	private final String description;

	/** Applied to each equity transaction. */
	private final Brokerage brokerage;

	/** Cash account to use during the simulation. */
	private final CashAccount cashAccount;

	/** Decision maker for when to enter a trade. */
	private final EntryLogic entryLogic;

	/** Decision maker for when to exit a trade. */
	private final ExitLogic exitLogic;

	/**
	 * @param startDate inclusive beginning date for the back testing.
	 * @param endDate inclusive end date for back testing.
	 */
	public BacktestBootstrapConfiguration( final EntryLogic entryLogic, final ExitLogic exitLogic,
			final Brokerage brokerage, final CashAccount cashAccount, final String description ) {
		this.cashAccount = cashAccount;
		this.description = description;
		this.entryLogic = entryLogic;
		this.exitLogic = exitLogic;
		this.brokerage = brokerage;
	}

	/**
	 * Exit logic used to generate sell orders.
	 * 
	 * @return input to the simulation that provides sell orders.
	 */
	public ExitLogic getExitLogic() {
		return exitLogic;
	}

	/**
	 * Broker that handles equity transactions.
	 * 
	 * @return broker that executes buy and sell orders.
	 */
	public Brokerage getBroker() {
		return brokerage;
	}

	/**
	 * Account that manages the cash.
	 * 
	 * @return cash account to use during the simulation.
	 */
	public CashAccount getCashAccount() {
		return cashAccount;
	}

	/**
	 * Entry logic used to generate buy orders.
	 * 
	 * @return input to the simulation that provides buy orders.
	 */
	public EntryLogic getEntryLogic() {
		return entryLogic;
	}

	/**
	 * Describes the behaviour of the configuration.
	 * 
	 * @return unique description of the configurations behaviour, a meaningful key.
	 */
	public String getDescription() {
		return description;
	}
}
