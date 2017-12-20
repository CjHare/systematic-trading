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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestSimulationDates;
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
import com.systematic.trading.backtest.configuration.strategy.indicator.EmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.MacdConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.RsiConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.SmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Contains generic configuration details, enabling lightweight concrete trials.
 * 
 * @author CJ Hare
 */
public abstract class BaseTrial {

	private final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();

	protected List<BacktestBootstrapConfiguration> macdConfirmedByRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
				for (final ConfirmaByConfiguration confirmBy : ConfirmaByConfiguration.values()) {

					final EntryConfiguration entry = factory.entry(
					        factory.entry(converter.translate(macdConfiguration)), confirmBy,
					        factory.entry(converter.translate(rsiConfiguration)));
					final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
					        maximumTrade);
					final ExitConfiguration exit = factory.exit();
					final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
					final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
					        exitPositionSizing);
					configurations
					        .add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
				}
			}
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> macd( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(converter.translate(macdConfiguration));
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> rsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(MacdConfiguration.values().length);

		for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(converter.translate(rsiConfiguration));
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
		}

		return configurations;
	}

	protected BacktestBootstrapConfiguration baseline( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		final EntryConfiguration entry = factory.entry(PeriodicConfiguration.WEEKLY);
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(MinimumTrade.ZERO,
		        MaximumTrade.ALL);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, openingFunds, deposit, new VanguardBrokerageFees(), strategy);
	}

	protected BacktestBootstrapConfiguration periodic( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final PeriodicConfiguration frequency ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();

		final EntryConfiguration entry = factory.entry(frequency);
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(MinimumTrade.ZERO,
		        MaximumTrade.ALL);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
		final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		return configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy);
	}

	protected BacktestBootstrapConfiguration configuration( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final StrategyConfiguration strategy ) {

		return new BacktestBootstrapConfigurationBuilder().withEquity(equity).withSimulationDates(simulationDates)
		        .withOpeningFunds(openingFunds).withDeposit(deposit).withBrokerage(brokerage).withStrategy(strategy)
		        .build();
	}

	protected List<BacktestBootstrapConfiguration> smaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaUptrendConfiguration.values().length);

		for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(converter.translate(smaConfiguration));
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> emaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {

			final EntryConfiguration entry = factory.entry(converter.translate(emaConfiguration));
			final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
			final ExitConfiguration exit = factory.exit();
			final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
			final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
			        exitPositionSizing);
			configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> smaOrEmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length * SmaUptrendConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
			for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {

				final EntryConfiguration entry = factory.entry(factory.entry(converter.translate(emaConfiguration)),
				        OperatorConfiguration.Selection.OR, factory.entry(converter.translate(smaConfiguration)));
				final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
				        maximumTrade);
				final ExitConfiguration exit = factory.exit();
				final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
				final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
				        exitPositionSizing);
				configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> smaEmaUptrendsAndRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length * SmaUptrendConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
			for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

					final EntryConfiguration entry = factory.entry(
					        factory.entry(factory.entry(converter.translate(emaConfiguration)),
					                OperatorConfiguration.Selection.OR,
					                factory.entry(converter.translate(smaConfiguration))),
					        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));
					final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade,
					        maximumTrade);
					final ExitConfiguration exit = factory.exit();
					final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();
					final StrategyConfiguration strategy = factory.strategy(entry, entryPositionSizing, exit,
					        exitPositionSizing);
					configurations
					        .add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
				}
			}
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> emaUptrendsAndRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length * SmaUptrendConfiguration.values().length);

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
				configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> smaUptrendsAndRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length * SmaUptrendConfiguration.values().length);

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
				configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
			}
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> longMacdConfirmedByRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit, final BrokerageTransactionFeeStructure brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

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
			configurations.add(configuration(equity, simulationDates, openingFunds, deposit, brokerage, strategy));
		}

		return configurations;
	}
}