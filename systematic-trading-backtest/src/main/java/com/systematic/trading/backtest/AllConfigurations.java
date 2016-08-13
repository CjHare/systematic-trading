/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfigurationBulider;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.display.DescriptionGenerator;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.ConfirmationIndicatorsSignalFilter;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.LadderedEquityManagementFeeCalculator;

/**
 * Executes all configurations.
 * 
 * @author CJ Hare
 */
public class AllConfigurations implements BacktestConfigurations {

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final Period WEEKLY = Period.ofWeeks(1);
	private static final Period MONTHLY = Period.ofMonths(1);

	public static void main( final String... args ) throws Exception {

		final BacktestApplication application = new BacktestApplication(MATH_CONTEXT);
		application.runTest(new AllConfigurations(), args);
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final DescriptionGenerator descriptionGenerator ) {

		final BacktestBootstrapConfigurationBulider configurationGenerator = new BacktestBootstrapConfigurationBulider(
		        equity, simulationDates, deposit, descriptionGenerator, MATH_CONTEXT);

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		// Vanguard Retail
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.VANGUARD_RETAIL,
		        WEEKLY, getVanguardRetailFeeCalculator()));

		// CMC Weekly
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.CMC_MARKETS,
		        WEEKLY, getVanguardEftFeeCalculator()));

		// CMC Monthly
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.CMC_MARKETS,
		        MONTHLY, getVanguardEftFeeCalculator()));

		// All signal based use the trading account
		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.CMC_MARKETS;

		for (final MaximumTrade maximumTrade : MaximumTrade.values()) {
			for (final MinimumTrade minimumTrade : MinimumTrade.values()) {
				configurations.addAll(getConfigurations(configurationGenerator, brokerage, minimumTrade, maximumTrade));
			}
		}

		return configurations;
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations(
	        final BacktestBootstrapConfigurationBulider configurationGenerator,
	        final BrokerageFeesConfiguration brokerage, final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

				// MACD & RSI
				configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
				        getVanguardEftFeeCalculator(), brokerage,
				        createSameDaySignalFilter(macdConfiguration, rsiConfiguration), macdConfiguration,
				        rsiConfiguration));

				// MACD & RSI - confirmation signals
				configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
				        getVanguardEftFeeCalculator(), brokerage,
				        createConfirmationSignalFilter(macdConfiguration, rsiConfiguration), macdConfiguration,
				        rsiConfiguration));
			}

			// MACD only
			configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
			        getVanguardEftFeeCalculator(), brokerage, createSameDaySignalFilter(macdConfiguration),
			        macdConfiguration));

			for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

					// MACD, SMA & RSI
					configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
					        getVanguardEftFeeCalculator(), brokerage,
					        createSameDaySignalFilter(macdConfiguration, smaConfiguration), macdConfiguration,
					        smaConfiguration, rsiConfiguration));
				}

				// MACD & SMA
				configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
				        getVanguardEftFeeCalculator(), brokerage,
				        createSameDaySignalFilter(macdConfiguration, smaConfiguration), macdConfiguration,
				        smaConfiguration));
			}
		}

		for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

				// SMA & RSI
				configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
				        getVanguardEftFeeCalculator(), brokerage,
				        createSameDaySignalFilter(smaConfiguration, rsiConfiguration), smaConfiguration,
				        rsiConfiguration));
			}
		}

		return configurations;
	}

	// TODO these fees should go somewhere, another configuration enum?
	private static EquityManagementFeeCalculator getVanguardEftFeeCalculator() {
		// new ZeroEquityManagementFeeStructure()
		return new FlatEquityManagementFeeCalculator(BigDecimal.valueOf(0.0018), MATH_CONTEXT);
	}

	private static EquityManagementFeeCalculator getVanguardRetailFeeCalculator() {
		final BigDecimal[] vanguardFeeRange = { BigDecimal.valueOf(50000), BigDecimal.valueOf(100000) };
		final BigDecimal[] vanguardPercentageFee = { BigDecimal.valueOf(0.009), BigDecimal.valueOf(0.006),
		        BigDecimal.valueOf(0.0035) };
		return new LadderedEquityManagementFeeCalculator(vanguardFeeRange, vanguardPercentageFee, MATH_CONTEXT);
	}

	private static SignalFilter createSameDaySignalFilter( final SignalConfiguration... entrySignals ) {

		final IndicatorSignalType[] passed = new IndicatorSignalType[entrySignals.length];
		for (int i = 0; i < entrySignals.length; i++) {
			passed[i] = entrySignals[i].getType();
		}
		return new IndicatorsOnSameDaySignalFilter(passed);
	}

	private static SignalFilter createConfirmationSignalFilter( final SignalConfiguration anchor,
	        final SignalConfiguration confirmation ) {
		final int daysUntilStartOfConfirmationRange = 1;
		final int confirmationDayRange = 3;
		return new ConfirmationIndicatorsSignalFilter(anchor.getType(), confirmation.getType(),
		        daysUntilStartOfConfirmationRange, confirmationDayRange);
	}
}