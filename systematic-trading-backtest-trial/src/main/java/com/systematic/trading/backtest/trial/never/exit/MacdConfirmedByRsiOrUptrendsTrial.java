/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
import java.util.Map;

import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.BacktestTrial;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfigurationFactory;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.EmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.MacdConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.RsiConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.SmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.BaseTrial;
import com.systematic.trading.input.BacktestLaunchArguments;
import com.systematic.trading.input.LaunchArgumentKey;
import com.systematic.trading.input.LaunchArgumentValidator;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * SelfWealth brokerage for the short confirmed by long up trend, compared with the baseline.
 * 
 * @author CJ Hare
 */
public class MacdConfirmedByRsiOrUptrendsTrial extends BaseTrial implements BacktestConfiguration {

	public static void main( final String... args ) throws Exception {

		final BacktestLaunchArguments backtestLaunchArgs = launchArguments(args);
		final Map<LaunchArgumentKey, String> launchArgsByKey = launchArgumentsByKey(args);

		new BacktestTrial(launchArgsByKey, new LaunchArgumentValidator())
		        .runBacktest(new MacdConfirmedByRsiOrUptrendsTrial(), backtestLaunchArgs);
	}

	@Override
	public List<BacktestBootstrapConfiguration> configuration(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		// Date based buying
		configurations.add(
		        periodic(
		                equity,
		                simulationDates,
		                cashAccount,
		                new SelfWealthBrokerageFees(),
		                PeriodicConfiguration.MONTHLY));

		final MaximumTrade maximumTrade = MaximumTrade.ALL;
		final MinimumTrade minimumTrade = MinimumTrade.ONE_THOUSAND;

		// Signal based buying
		configurations.addAll(
		        macdConfirmedByRsiOrUptrends(
		                equity,
		                simulationDates,
		                cashAccount,
		                new SelfWealthBrokerageFees(),
		                minimumTrade,
		                maximumTrade));

		configurations.addAll(
		        macd(equity, simulationDates, cashAccount, new SelfWealthBrokerageFees(), minimumTrade, maximumTrade));

		configurations.addAll(
		        rsi(equity, simulationDates, cashAccount, new SelfWealthBrokerageFees(), minimumTrade, maximumTrade));

		configurations.addAll(
		        smaEmaUptrendsAndRsi(
		                equity,
		                simulationDates,
		                cashAccount,
		                new SelfWealthBrokerageFees(),
		                minimumTrade,
		                maximumTrade));

		configurations.addAll(
		        emaUptrendsAndRsi(
		                equity,
		                simulationDates,
		                cashAccount,
		                new SelfWealthBrokerageFees(),
		                minimumTrade,
		                maximumTrade));

		configurations.addAll(
		        smaUptrendsAndRsi(
		                equity,
		                simulationDates,
		                cashAccount,
		                new SelfWealthBrokerageFees(),
		                minimumTrade,
		                maximumTrade));

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> macdConfirmedByRsiOrUptrends(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length);
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(mediumMacdConfirmedByRsi(), entryPositionSizing, exit, exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(
		                        factory.entry(
		                                mediumMacdConfirmedByRsi(),
		                                OperatorConfiguration.Selection.OR,
		                                shortSmaConfirmedByEma()),
		                        entryPositionSizing,
		                        exit,
		                        exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(
		                        factory.entry(
		                                mediumMacdConfirmedByRsi(),
		                                OperatorConfiguration.Selection.OR,
		                                shortEmaConfirmedByEma()),
		                        entryPositionSizing,
		                        exit,
		                        exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(
		                        factory.entry(
		                                mediumMacdConfirmedByRsi(),
		                                OperatorConfiguration.Selection.OR,
		                                longEma()),
		                        entryPositionSizing,
		                        exit,
		                        exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(
		                        factory.entry(
		                                mediumMacdConfirmedByRsi(),
		                                OperatorConfiguration.Selection.OR,
		                                shortEmaOrSmaConfirmedByEma()),
		                        entryPositionSizing,
		                        exit,
		                        exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(longEma(), entryPositionSizing, exit, exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(longSma(), entryPositionSizing, exit, exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(longEmaOrSma(), entryPositionSizing, exit, exitPositionSizing)));

		configurations.add(
		        configuration(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                factory.strategy(
		                        factory.entry(
		                                mediumMacdConfirmedByRsi(),
		                                OperatorConfiguration.Selection.OR,
		                                longEmaOrSma()),
		                        entryPositionSizing,
		                        exit,
		                        exitPositionSizing)));

		return configurations;
	}

	private EntryConfiguration mediumMacdConfirmedByRsi() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		final EntryConfiguration longMacdEntry = factory.entry(converter.translate(MacdConfiguration.MEDIUM));
		final EntryConfiguration rsientry = factory.entry(
		        factory.entry(converter.translate(RsiConfiguration.MEDIUM)),
		        OperatorConfiguration.Selection.OR,
		        factory.entry(converter.translate(RsiConfiguration.LONG)));
		return factory.entry(longMacdEntry, ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_THREE_DAYS, rsientry);
	}

	private EntryConfiguration shortSmaConfirmedByEma() {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(shortSma(), ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_THREE_DAYS, longEma());
	}

	private EntryConfiguration shortEmaConfirmedByEma() {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(shortEma(), ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_THREE_DAYS, longEma());
	}

	private EntryConfiguration shortEmaOrSmaConfirmedByEma() {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(shortEmaOrSma(), ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_THREE_DAYS, longEma());
	}

	private EntryConfiguration shortEmaOrSma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(
		        factory.entry(converter.translate(EmaUptrendConfiguration.SHORT)),
		        OperatorConfiguration.Selection.OR,
		        factory.entry(converter.translate(SmaUptrendConfiguration.SHORT)));
	}

	private EntryConfiguration shortSma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(converter.translate(SmaUptrendConfiguration.SHORT));
	}

	private EntryConfiguration shortEma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(converter.translate(EmaUptrendConfiguration.SHORT));
	}

	private EntryConfiguration longEma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(converter.translate(EmaUptrendConfiguration.LONG));
	}

	private EntryConfiguration longSma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(converter.translate(SmaUptrendConfiguration.LONG));
	}

	private EntryConfiguration longEmaOrSma() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		return factory.entry(
		        factory.entry(converter.translate(SmaUptrendConfiguration.LONG)),
		        OperatorConfiguration.Selection.OR,
		        factory.entry(converter.translate(EmaUptrendConfiguration.LONG)));
	}
}
