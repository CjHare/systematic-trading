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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfigurationFactory;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmationConfiguration;
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
import com.systematic.trading.backtest.trial.configuration.BaseTrialConfiguration;

/**
 * Executes all trials, allowing different configuration for the trade sizes.
 * 
 * @author CJ Hare
 */
public abstract class AllTrials extends BaseTrialConfiguration implements BacktestConfiguration {

	//TODO move this into the factory, possible rename indicatorConfiguration too
	private final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();

	private final BrokerageFeesConfiguration brokerage;
	private final Set<Pair<MinimumTrade, MaximumTrade>> tradeSizes;

	public AllTrials( final BrokerageFeesConfiguration brokerage,
	        final Set<Pair<MinimumTrade, MaximumTrade>> tradeSizes ) {
		this.brokerage = brokerage;
		this.tradeSizes = tradeSizes;
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		// Date based buying
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicConfiguration.WEEKLY));
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicConfiguration.MONTHLY));

		for (final Pair<MinimumTrade, MaximumTrade> tradeSize : tradeSizes) {
			final MinimumTrade minimumTrade = tradeSize.getLeft();
			final MaximumTrade maximumTrade = tradeSize.getRight();

			// Signal based buying
			configurations.addAll(
			        getMacdConfirmedByRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getSameDayMacdRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getEmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(getMacd(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getSameDayEmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getSameDayMacdSmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getSameDayMacdEmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getSameDayMacdSma(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getSameDaySmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getSmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getSmaOrEmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getMacdConfirmedByRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        MacdConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
				for (final ConfirmationConfiguration.Type filterConfiguration : ConfirmationConfiguration.Type
				        .values()) {

					final EntryConfiguration entry = factory.entry(
					        factory.entry(converter.translate(macdConfiguration)), filterConfiguration,
					        factory.entry(converter.translate(rsiConfiguration)));

					final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
					        maximumTrade);
					final ExitConfiguration exit = factory.exit();
					final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
					final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
					        exitPositionSizing);
					configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
				}
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDayMacdRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        MacdConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

				final EntryConfiguration entry = factory.entry(factory.entry(converter.translate(macdConfiguration)),
				        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));
				final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
				        maximumTrade);
				final ExitConfiguration exit = factory.exit();
				final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
				final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
				        exitPositionSizing);
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getMacd( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(converter.translate(macdConfiguration));
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDayMacdSmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length
		        * SmaUptrendConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

					final EntryConfiguration entry = factory.entry(
					        factory.entry(factory.entry(converter.translate(macdConfiguration)),
					                OperatorConfiguration.Selection.AND,
					                factory.entry(converter.translate(smaConfiguration))),
					        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));

					final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
					        maximumTrade);
					final ExitConfiguration exit = factory.exit();
					final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
					final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
					        exitPositionSizing);
					configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
				}
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDayMacdEmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length
		        * SmaUptrendConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

					final EntryConfiguration entry = factory.entry(
					        factory.entry(factory.entry(converter.translate(macdConfiguration)),
					                OperatorConfiguration.Selection.AND,
					                factory.entry(converter.translate(emaConfiguration))),
					        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));

					final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
					        maximumTrade);
					final ExitConfiguration exit = factory.exit();
					final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
					final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
					        exitPositionSizing);
					configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
				}
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDayMacdSma( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        MacdConfiguration.values().length * SmaUptrendConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {

				final EntryConfiguration entry = factory.entry(factory.entry(converter.translate(macdConfiguration)),
				        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(smaConfiguration)));
				final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
				        maximumTrade);
				final ExitConfiguration exit = factory.exit();
				final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
				final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
				        exitPositionSizing);
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDaySmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaUptrendConfiguration.values().length * RsiConfiguration.values().length);

		for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

				final EntryConfiguration entry = factory.entry(factory.entry(converter.translate(smaConfiguration)),
				        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));
				final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
				        maximumTrade);
				final ExitConfiguration exit = factory.exit();
				final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
				final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
				        exitPositionSizing);
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getSameDayEmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaUptrendConfiguration.values().length * RsiConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

				final EntryConfiguration entry = factory.entry(factory.entry(converter.translate(emaConfiguration)),
				        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));
				final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
				        maximumTrade);
				final ExitConfiguration exit = factory.exit();
				final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
				final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
				        exitPositionSizing);
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}
}