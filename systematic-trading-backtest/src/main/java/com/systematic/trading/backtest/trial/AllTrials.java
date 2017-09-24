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
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdUptrendConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaGradientConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.configuration.BaseTrialConfiguration;
import com.systematic.trading.backtest.trial.configuration.TrialConfigurationBuilder;

/**
 * Executes over 1200 configurations, scatter gun approach.
 * 
 * @author CJ Hare
 */
public abstract class AllTrials extends BaseTrialConfiguration implements BacktestConfiguration {

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
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicFilterConfiguration.WEEKLY));
		configurations.add(getPeriod(equity, simulationDates, deposit, brokerage, PeriodicFilterConfiguration.MONTHLY));

		for (final Pair<MinimumTrade, MaximumTrade> tradeSize : tradeSizes) {
			final MinimumTrade minimumTrade = tradeSize.getLeft();
			final MaximumTrade maximumTrade = tradeSize.getRight();

			// Signal based buying
			configurations.addAll(
			        getAllMacdConfirmedByRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getAllSameDayMacdRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(getAllMacd(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getAllSameDayMacdSmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getAllSameDayMacdSma(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getAllSameDaySmaRsi(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations
			        .addAll(getAllSmaUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
			configurations.addAll(
			        getAllMacdUptrends(equity, simulationDates, deposit, brokerage, minimumTrade, maximumTrade));
		}

		return configurations;
	}

	private BacktestBootstrapConfiguration getConfiguration( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final EntryLogicConfiguration entryLogic ) {
		return new TrialConfigurationBuilder().withEquity(equity).withSimulationDates(simulationDates)
		        .withDeposit(deposit).withBrokerage(brokerage).withEntry(entryLogic).build();
	}

	private List<BacktestBootstrapConfiguration> getAllMacdConfirmedByRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
				for (final ConfirmationSignalFilterConfiguration.Type filterConfiguration : ConfirmationSignalFilterConfiguration.Type
				        .values()) {
					configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
					        new EntryLogicConfiguration(new ConfirmationSignalFilterConfiguration(filterConfiguration,
					                macdConfiguration, rsiConfiguration), maximumTrade, minimumTrade)));
				}
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSameDayMacdRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
				        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				                macdConfiguration, rsiConfiguration), maximumTrade, minimumTrade)));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllMacd( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
			                macdConfiguration, macdConfiguration), maximumTrade, minimumTrade)));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSameDayMacdSmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final SmaGradientConfiguration smaConfiguration : SmaGradientConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
					configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
					        new EntryLogicConfiguration(
					                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
					                        macdConfiguration, smaConfiguration, rsiConfiguration),
					                maximumTrade, minimumTrade)));
				}
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSameDayMacdSma( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final SmaGradientConfiguration smaConfiguration : SmaGradientConfiguration.values()) {
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
				        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				                macdConfiguration, smaConfiguration), maximumTrade, minimumTrade)));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSameDaySmaRsi( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length * RsiConfiguration.values().length);

		for (final SmaGradientConfiguration smaConfiguration : SmaGradientConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
				        new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				                smaConfiguration, rsiConfiguration), maximumTrade, minimumTrade)));
			}
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllSmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaGradientConfiguration.values().length);

		for (final SmaGradientConfiguration smaConfiguration : SmaGradientConfiguration.values()) {
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        new EntryLogicConfiguration(
			                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, smaConfiguration),
			                maximumTrade, minimumTrade)));
		}

		return configurations;
	}

	private List<BacktestBootstrapConfiguration> getAllMacdUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        MacdUptrendConfiguration.values().length);

		for (final MacdUptrendConfiguration macdConfiguration : MacdUptrendConfiguration.values()) {
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        new EntryLogicConfiguration(
			                new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, macdConfiguration),
			                maximumTrade, minimumTrade)));
		}

		return configurations;
	}
}