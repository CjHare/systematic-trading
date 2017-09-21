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
package com.systematic.trading.backtest.trial;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdUptrendConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;

/**
 * Provides the configuration for the baseline for result comparison.
 * 
 * @author CJ Hare
 */
public class TrialHoldForever {

	public BacktestBootstrapConfiguration getBuyWeeklyHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(PeriodicFilterConfiguration.WEEKLY, MaximumTrade.ALL, MinimumTrade.ZERO),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getBuyMonthlyHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(PeriodicFilterConfiguration.MONTHLY, MaximumTrade.ALL, MinimumTrade.ZERO),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdUptrendHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdUptrendConfiguration macdConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(
		                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, macdConfiguration),
		                maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getSmaUptrendHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final SmaConfiguration smaConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(
		                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, smaConfiguration),
		                maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getSmaUptrendRsiHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final SmaConfiguration smaConfiguration,
	        final RsiConfiguration rsiConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
		                smaConfiguration, rsiConfiguration), maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdSmaHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdConfiguration macdConfiguration,
	        final SmaConfiguration smaConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
		                macdConfiguration, smaConfiguration), maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdSmaRsiHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdConfiguration macdConfiguration,
	        final SmaConfiguration smaConfiguration, final RsiConfiguration rsiConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
		                macdConfiguration, smaConfiguration, rsiConfiguration), maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdConfiguration macdConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(
		                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, macdConfiguration),
		                maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdRsiHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdConfiguration macdConfiguration,
	        final RsiConfiguration rsiConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
		                macdConfiguration, rsiConfiguration), maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}

	public BacktestBootstrapConfiguration getMacdConfirmedByRsiHoldForever( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final MacdConfiguration macdConfiguration,
	        final RsiConfiguration rsiConfiguration,
	        final ConfirmationSignalFilterConfiguration.Type filterConfiguration ) {
		return new BacktestBootstrapConfiguration(simulationDates, brokerage,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit,
		        new EntryLogicConfiguration(new ConfirmationSignalFilterConfiguration(filterConfiguration,
		                rsiConfiguration, macdConfiguration), maximumTrade, minimumTrade),
		        equity, ExitLogicConfiguration.HOLD_FOREVER);
	}
}