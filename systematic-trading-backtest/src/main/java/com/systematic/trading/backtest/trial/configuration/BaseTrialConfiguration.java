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
package com.systematic.trading.backtest.trial.configuration;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.EmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.SmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.strategy.operator.AnyOfIndicatorFilterConfiguration;
import com.systematic.trading.strategy.operator.SameDayFilterConfiguration;

/**
 * Contains generic configuration details, enabling lightweight concrete trials.
 * 
 * @author CJ Hare
 */
public abstract class BaseTrialConfiguration {

	private final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();

	protected BacktestBootstrapConfiguration getBaseline( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		return new TrialConfigurationBuilder().withEquity(equity).withSimulationDates(simulationDates)
		        .withDeposit(deposit).withBrokerage(BrokerageFeesConfiguration.VANGUARD_RETAIL)
		        .withEntry(new EntryLogicConfiguration(PeriodicFilterConfiguration.WEEKLY, MaximumTrade.ALL,
		                MinimumTrade.ZERO))
		        .build();
	}

	protected BacktestBootstrapConfiguration getPeriod( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final PeriodicFilterConfiguration frequency ) {
		return new TrialConfigurationBuilder().withEquity(equity).withSimulationDates(simulationDates)
		        .withDeposit(deposit).withBrokerage(brokerage)
		        .withEntry(new EntryLogicConfiguration(frequency, MaximumTrade.ALL, MinimumTrade.ZERO)).build();
	}

	protected BacktestBootstrapConfiguration getConfiguration( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final EntryLogicConfiguration entryLogic ) {
		return new TrialConfigurationBuilder().withEquity(equity).withSimulationDates(simulationDates)
		        .withDeposit(deposit).withBrokerage(brokerage).withEntry(entryLogic).build();
	}

	protected List<BacktestBootstrapConfiguration> getSmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        SmaUptrendConfiguration.values().length);

		for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        new EntryLogicConfiguration(
			                new SameDayFilterConfiguration(converter.translate(smaConfiguration)),
			                maximumTrade, minimumTrade)));
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> getEmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
			configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
			        new EntryLogicConfiguration(
			                new SameDayFilterConfiguration(converter.translate(emaConfiguration)),
			                maximumTrade, minimumTrade)));
		}

		return configurations;
	}

	protected List<BacktestBootstrapConfiguration> getSmaOrEmaUptrends( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>(
		        EmaUptrendConfiguration.values().length * SmaUptrendConfiguration.values().length);

		for (final EmaUptrendConfiguration emaConfiguration : EmaUptrendConfiguration.values()) {
			for (final SmaUptrendConfiguration smaConfiguration : SmaUptrendConfiguration.values()) {
				configurations.add(getConfiguration(equity, simulationDates, deposit, brokerage,
				        new EntryLogicConfiguration(
				                new AnyOfIndicatorFilterConfiguration(
				                        converter.translate(emaConfiguration),
				                        converter.translate(smaConfiguration)),
				                maximumTrade, minimumTrade)));
			}
		}

		return configurations;
	}
}