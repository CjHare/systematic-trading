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
package com.systematic.trading.backtest.trial.never.exit;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.BacktestTrial;
import com.systematic.trading.backtest.brokerage.fee.VanguardBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfigurationFactory;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.MacdConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.RsiConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.BaseTrial;
import com.systematic.trading.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.input.DataServiceTypeLaunchArgument;
import com.systematic.trading.input.EndDateLaunchArgument;
import com.systematic.trading.input.EquityDatasetLaunchArgument;
import com.systematic.trading.input.FileBaseDirectoryLaunchArgument;
import com.systematic.trading.input.LaunchArgumentValidator;
import com.systematic.trading.input.LaunchArguments;
import com.systematic.trading.input.OutputLaunchArgument;
import com.systematic.trading.input.StartDateLaunchArgument;
import com.systematic.trading.input.TickerSymbolLaunchArgument;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Long MACD confirmed by Medium or Long RSI
 * 
 * @author CJ Hare
 */
public class MacdConfirmedByRsiTrial extends BaseTrial implements BacktestConfiguration {
	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();

		final LaunchArguments launchArgs = new LaunchArguments(new CommandLineLaunchArgumentsParser(),
		        new OutputLaunchArgument(validator), new DataServiceTypeLaunchArgument(),
		        new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		        new EquityDatasetLaunchArgument(validator), new TickerSymbolLaunchArgument(validator),
		        new FileBaseDirectoryLaunchArgument(validator), args);

		new BacktestTrial(launchArgs.getDataService()).runBacktest(new MacdConfirmedByRsiTrial(), launchArgs);
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		final BrokerageTransactionFeeStructure brokerage = new VanguardBrokerageFees();

		// Date based buying
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicConfiguration.WEEKLY));
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicConfiguration.MONTHLY));

		final MinimumTrade minimumTrade = MinimumTrade.ZERO;
		final MaximumTrade maximumTrade = MaximumTrade.ALL;

		// Signal based buying
		configurations.addAll(
		        getLongMacdConfirmedByRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> getLongMacdConfirmedByRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageTransactionFeeStructure brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        ConfirmaByConfiguration.values().length);

		final EntryConfiguration longMacdEntry = factory.entry(converter.translate(MacdConfiguration.LONG));
		final EntryConfiguration rsientry = factory.entry(factory.entry(converter.translate(RsiConfiguration.MEDIUM)),
		        OperatorConfiguration.Selection.OR, factory.entry(converter.translate(RsiConfiguration.LONG)));

		for (final ConfirmaByConfiguration confirmConfiguration : ConfirmaByConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(longMacdEntry, confirmConfiguration, rsientry);
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
		}

		return configurations;
	}
}