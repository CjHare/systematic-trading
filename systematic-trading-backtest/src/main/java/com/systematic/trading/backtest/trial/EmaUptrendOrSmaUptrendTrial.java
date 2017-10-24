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
package com.systematic.trading.backtest.trial;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestApplication;
import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.backtest.input.EndDateLaunchArgument;
import com.systematic.trading.backtest.input.FileBaseDirectoryLaunchArgument;
import com.systematic.trading.backtest.input.LaunchArgumentValidator;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.input.OutputLaunchArgument;
import com.systematic.trading.backtest.input.StartDateLaunchArgument;
import com.systematic.trading.backtest.input.TickerSymbolLaunchArgument;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.configuration.BaseTrialConfiguration;
import com.systematic.trading.backtest.trial.configuration.EmaUptrendConfiguration;
import com.systematic.trading.backtest.trial.configuration.SmaUptrendConfiguration;
import com.systematic.trading.strategy.periodic.PeriodicFilterConfiguration;

/**
 * Executes all the combinations of SMA and EMA uptrrends. When there is either an SMA or EMA signal it's a buy signal.
 * 
 * @author CJ Hare
 */
public class EmaUptrendOrSmaUptrendTrial extends BaseTrialConfiguration implements BacktestConfiguration {
	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();

		new BacktestApplication().runBacktest(new EmaUptrendOrSmaUptrendTrial(),
		        new LaunchArguments(new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(validator),
		                new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		                new TickerSymbolLaunchArgument(validator), new FileBaseDirectoryLaunchArgument(validator),
		                args));
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.VANGUARD_RETAIL;

		// Date based buying
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicFilterConfiguration.WEEKLY));
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicFilterConfiguration.MONTHLY));

		final MinimumTrade minimumTrade = MinimumTrade.ZERO;
		final MaximumTrade maximumTrade = MaximumTrade.ALL;

		// Signal based buying
		configurations
		        .addAll(getSmaOrEmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));

		return configurations;
	}

	@Override
	public Period getWarmUpPeriod() {
		return Period.ofDays(
		        Math.max(EmaUptrendConfiguration.LONG.getLookback() + EmaUptrendConfiguration.LONG.getDaysOfGradient(),
		                SmaUptrendConfiguration.LONG.getLookback() + SmaUptrendConfiguration.LONG.getDaysOfGradient()));
	}
}