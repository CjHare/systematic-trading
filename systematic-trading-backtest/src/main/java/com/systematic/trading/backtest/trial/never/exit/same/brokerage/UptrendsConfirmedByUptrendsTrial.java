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
package com.systematic.trading.backtest.trial.never.exit.same.brokerage;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestApplication;
import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.brokerage.fee.VanguardBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfigurationFactory;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.EmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.SmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.backtest.input.DataServiceTypeLaunchArgument;
import com.systematic.trading.backtest.input.EndDateLaunchArgument;
import com.systematic.trading.backtest.input.EquityDatasetLaunchArgument;
import com.systematic.trading.backtest.input.FileBaseDirectoryLaunchArgument;
import com.systematic.trading.backtest.input.LaunchArgumentValidator;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.input.OutputLaunchArgument;
import com.systematic.trading.backtest.input.StartDateLaunchArgument;
import com.systematic.trading.backtest.input.TickerSymbolLaunchArgument;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.BaseTrial;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Combines EMA and SMA up trends, combining both the longs with the mediums and longs and short up trends.
 * 
 * As the short and medium up trends fire events earlier then the long, the long get used as the confirm signal.
 * 
 * @author CJ Hare
 */
public class UptrendsConfirmedByUptrendsTrial extends BaseTrial implements BacktestConfiguration {
	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();

		final LaunchArguments launchArgs = new LaunchArguments(new CommandLineLaunchArgumentsParser(),
		        new OutputLaunchArgument(validator), new DataServiceTypeLaunchArgument(),
		        new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		        new EquityDatasetLaunchArgument(), new TickerSymbolLaunchArgument(validator),
		        new FileBaseDirectoryLaunchArgument(validator), args);

		new BacktestApplication(launchArgs.getDataService()).runBacktest(new UptrendsConfirmedByUptrendsTrial(),
		        launchArgs);
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		final BrokerageTransactionFeeStructure brokerage = new VanguardBrokerageFees();

		// Date based buying
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicConfiguration.WEEKLY));

		final MinimumTrade minimumTrade = MinimumTrade.ZERO;
		final MaximumTrade maximumTrade = MaximumTrade.ALL;

		// Signal based buying
		configurations
		        .addAll(getCombinedUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getCombinedUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageTransactionFeeStructure brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        2 + ConfirmaByConfiguration.values().length);

		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();

		// (LongSMA OR LongEMA) AND (MediumSMA OR MediumEMA)
		final EntryConfiguration mediumLongEntry = factory.entry(getLongSmaOrEma(), OperatorConfiguration.Selection.AND,
		        getMediumSmaOrEma());
		configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
		        factory.strategy(mediumLongEntry, entryPositionSizing, exit, exitPositionSizing)));

		// (LongSMA OR LongEMA) AND (ShortSMA OR ShortEMA)
		final EntryConfiguration shortLongEntry = factory.entry(getLongSmaOrEma(), OperatorConfiguration.Selection.AND,
		        getShortSmaOrEma());
		configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
		        factory.strategy(shortLongEntry, entryPositionSizing, exit, exitPositionSizing)));

		for (final ConfirmaByConfiguration by : ConfirmaByConfiguration.values()) {

			// (MediumSMA OR MediumEMA) ConfirmedBy (LongSMA OR LongEMA)
			final EntryConfiguration mediumConfirmedEntry = factory.entry(getMediumSmaOrEma(), by, getLongSmaOrEma());
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        factory.strategy(mediumConfirmedEntry, entryPositionSizing, exit, exitPositionSizing)));

			// (ShortSMA OR ShortEMA) ConfirmedBy (LongSMA OR LongEMA) 
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        factory.strategy(factory.entry(getShortSmaOrEma(), by, getLongSmaOrEma()), entryPositionSizing,
			                exit, exitPositionSizing)));
		}

		return configurations;
	}

	private EntryConfiguration getShortSmaOrEma() {
		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(factory.entry(converter.translate(EmaUptrendConfiguration.SHORT)),
		        OperatorConfiguration.Selection.OR, factory.entry(converter.translate(SmaUptrendConfiguration.SHORT)));
	}

	private EntryConfiguration getMediumSmaOrEma() {
		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(factory.entry(converter.translate(EmaUptrendConfiguration.MEDIUM)),
		        OperatorConfiguration.Selection.OR, factory.entry(converter.translate(SmaUptrendConfiguration.MEDIUM)));
	}

	private EntryConfiguration getLongSmaOrEma() {
		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(factory.entry(converter.translate(EmaUptrendConfiguration.LONG)),
		        OperatorConfiguration.Selection.OR, factory.entry(converter.translate(SmaUptrendConfiguration.LONG)));
	}
}