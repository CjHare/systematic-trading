/**
 * Copyright (c) 2015-2017, CJ Hare
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
package com.systematic.trading.backtest.trial.configuration;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;

/**
 * Provides construction of configuration.
 * 
 * @author CJ Hare
 */
public class TrialConfigurationBuilder {

	private EquityConfiguration equity;
	private BacktestSimulationDates simulationDates;
	private DepositConfiguration deposit;
	private BrokerageFeesConfiguration brokerage;
	private EntryLogicConfiguration entryLogic;

	public TrialConfigurationBuilder withEquity( final EquityConfiguration equity ) {
		this.equity = equity;
		return this;
	}

	public TrialConfigurationBuilder withSimulationDates( final BacktestSimulationDates simulationDates ) {
		this.simulationDates = simulationDates;
		return this;
	}

	public TrialConfigurationBuilder withDeposit( final DepositConfiguration deposit ) {
		this.deposit = deposit;
		return this;
	}

	public TrialConfigurationBuilder withBrokerage( final BrokerageFeesConfiguration brokerage ) {
		this.brokerage = brokerage;
		return this;
	}

	public TrialConfigurationBuilder withEntry( final EntryLogicConfiguration entryLogic ) {
		this.entryLogic = entryLogic;
		return this;
	}

	public BacktestBootstrapConfiguration build() {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entryLogic, equity,
		        ExitLogicConfiguration.HOLD_FOREVER);
	}
}