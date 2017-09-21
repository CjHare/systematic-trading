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

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestApplication;
import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdUptrendConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
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

/**
 * MACD upward trend is when the fast EMA is above the slow EMA, denoting a upward trend in price movement.
 * 
 * @author CJ Hare
 */
public class MacdUpwardTrendBuyHoldlTrial implements BacktestConfiguration {

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private TrialHoldForever trial = new TrialHoldForever();

	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();

		new BacktestApplication(MATH_CONTEXT).runBacktest(new MacdUpwardTrendBuyHoldlTrial(),
		        new LaunchArguments(new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(validator),
		                new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		                new TickerSymbolLaunchArgument(validator), new FileBaseDirectoryLaunchArgument(validator),
		                args));
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		// Comparison baseline 
		configurations.add(trial.getBuyWeeklyHoldForever(equity, simulationDates, deposit,
		        BrokerageFeesConfiguration.VANGUARD_RETAIL));

		final MaximumTrade maximumTrade = MaximumTrade.ALL;
		final MinimumTrade[] minimumTrades = { MinimumTrade.FIVE_HUNDRED, MinimumTrade.TWO_THOUSAND };
		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.CMC_MARKETS;

		for (final MinimumTrade minimumTrade : minimumTrades) {

			configurations.addAll(
			        getAllMacdUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getAllSmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllMacdUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdUptrendConfiguration macdConfiguration : MacdUptrendConfiguration.values()) {
			configurations.add(trial.getMacdUptrendHoldForever(equity, simulationDates, deposit, brokerage,
			        minimumTrade, maximumTrade, macdConfiguration));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(SmaConfiguration.values().length);

		for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
			configurations.add(trial.getSmaUptrendHoldForever(equity, simulationDates, deposit, brokerage, minimumTrade,
			        maximumTrade, smaConfiguration));
		}

		return configurations;
	}

}