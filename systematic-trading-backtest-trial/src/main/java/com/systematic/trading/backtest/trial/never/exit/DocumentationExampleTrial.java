/**
 * Copyright (c) 2015-2018, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package com.systematic.trading.backtest.trial.never.exit;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.BacktestTrial;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
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
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Trial used in the documentation example.
 * The purpose being to show a subset of all available strategy configurations, with enough variety
 * to provide a template.
 * 
 * Example strategies hold forever, never generate any sell events.
 * 
 * Indicator Types:
 * <ul>
 * <li>MACD</li>
 * <li>RSI</li>
 * <li>EMA Uptrend</li>
 * <li>SMA Uptrend</li>
 * </li>
 * 
 * Strategy combinations:
 * <ul>
 * <li>Periodic</li>
 * <li>Indicator</li>
 * <li>Periodic AND Indicator</li>
 * <li>Indicator AND Indicator</li>
 * <li>Indicator OR Indicator</li>
 * <li>Indicator ConfirmedBy Indicator</li>
 * </li>
 * 
 * @author CJ Hare
 */
public class DocumentationExampleTrial extends BaseTrial implements BacktestConfiguration {

	private final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
	private final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

	public static void main( final String... args ) throws Exception {

		final BacktestLaunchArguments launchArgs = launchArguments(args);

		new BacktestTrial(launchArgs).runBacktest(new DocumentationExampleTrial(), launchArgs);
	}

	@Override
	public List<BacktestBootstrapConfiguration> configuration(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();
		final BrokerageTransactionFeeStructure brokerage = new SelfWealthBrokerageFees();

		// Put everything in at every opportunity
		final MinimumTrade minimumTrade = MinimumTrade.ZERO;
		final MaximumTrade maximumTrade = MaximumTrade.ALL;

		configurations.add(periodic(equity, simulationDates, cashAccount, brokerage));
		configurations.add(indicator(equity, simulationDates, cashAccount, brokerage, minimumTrade, maximumTrade));
		configurations
		        .add(periodicAndIndicator(equity, simulationDates, cashAccount, brokerage, minimumTrade, maximumTrade));
		configurations.add(
		        indicatorAndIndicator(equity, simulationDates, cashAccount, brokerage, minimumTrade, maximumTrade));
		configurations
		        .add(indicatorOrIndicator(equity, simulationDates, cashAccount, brokerage, minimumTrade, maximumTrade));
		configurations.add(
		        indicatorConfirmedByIndicator(
		                equity,
		                simulationDates,
		                cashAccount,
		                brokerage,
		                minimumTrade,
		                maximumTrade));

		return configurations;
	}

	/**
	 * MACD confirmed by RSI, whenever there is a MACD signal with a RSI occurring within three
	 * days, a buy event is generated.
	 */
	private BacktestBootstrapConfiguration indicatorConfirmedByIndicator(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final MacdConfiguration macdConfiguration = MacdConfiguration.MEDIUM;
		final RsiConfiguration rsiConfiguration = RsiConfiguration.MEDIUM;
		final ConfirmaByConfiguration confirmBy = ConfirmaByConfiguration.NO_DELAY_RANGE_THREE_DAYS;
		final EntryConfiguration entry = factory.entry(
		        factory.entry(converter.translate(macdConfiguration)),
		        confirmBy,
		        factory.entry(converter.translate(rsiConfiguration)));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);

	}

	/**
	 * Buy events are generated whenever there is an EMA or SMA uptrend (positive gradient).
	 */
	private BacktestBootstrapConfiguration indicatorOrIndicator(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final EmaUptrendConfiguration emaConfiguration = EmaUptrendConfiguration.LONG;
		final SmaUptrendConfiguration smaConfiguration = SmaUptrendConfiguration.LONG;
		final EntryConfiguration entry = factory.entry(
		        factory.entry(converter.translate(emaConfiguration)),
		        OperatorConfiguration.Selection.OR,
		        factory.entry(converter.translate(smaConfiguration)));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);
	}

	/**
	 * Whenever there is both an EMA Uptrend (positive gradient) and a RSI signal on the same day, a
	 * buy signal is generated.
	 */
	private BacktestBootstrapConfiguration indicatorAndIndicator(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final EmaUptrendConfiguration emaConfiguration = EmaUptrendConfiguration.LONG;
		final RsiConfiguration rsiConfiguration = RsiConfiguration.MEDIUM;
		final EntryConfiguration entry = factory.entry(
		        factory.entry(converter.translate(emaConfiguration)),
		        OperatorConfiguration.Selection.AND,
		        factory.entry(converter.translate(rsiConfiguration)));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);
	}

	/**
	 * Buying when there is a SMA uptrend that coincides with a monthly periodic (simulation start
	 * date).
	 */
	private BacktestBootstrapConfiguration periodicAndIndicator(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final EmaUptrendConfiguration emaConfiguration = EmaUptrendConfiguration.LONG;
		final EntryConfiguration entry = factory.entry(
		        factory.entry(converter.translate(emaConfiguration)),
		        OperatorConfiguration.Selection.AND,
		        factory.entry(PeriodicConfiguration.MONTHLY));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);
	}

	/**
	 * Buy signal is generated whenever there is a MACD signal.
	 */
	private BacktestBootstrapConfiguration indicator(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final EntryConfiguration entry = factory.entry(converter.translate(MacdConfiguration.MEDIUM));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);
	}

	/**
	 * Generates a buy signal monthly, beginning on the simulation start date.
	 */
	private BacktestBootstrapConfiguration periodic(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates,
	        final CashAccountConfiguration cashAccount,
	        final BrokerageTransactionFeeStructure brokerage ) {

		final EntryConfiguration entry = factory.entry(PeriodicConfiguration.MONTHLY);
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(
		        MinimumTrade.ZERO,
		        MaximumTrade.ALL);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, cashAccount, brokerage, strategy);
	}
}
