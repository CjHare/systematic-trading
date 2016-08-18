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
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.context.BacktestBootstrapContextBulider;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;

/**
 * MACD signals @ 150 weekly deposit.
 * 
 * @author CJ Hare
 */
public class SingleConfigurationSignals implements BacktestConfigurations {

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	public static void main( final String... args ) throws Exception {

		final BacktestApplication application = new BacktestApplication(MATH_CONTEXT);
		application.runTest(new SingleConfigurationSignals(), args);
	}

	@Override
	public List<BacktestBootstrapContext> get( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {

		final BacktestBootstrapContextBulider configurationGenerator = new BacktestBootstrapContextBulider(
		        equity, simulationDates, deposit, MATH_CONTEXT);

		final List<BacktestBootstrapContext> configurations = new ArrayList<>();

		// All signal based use the trading account
		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.CMC_MARKETS;

		final MaximumTrade maximumTrade = MaximumTrade.ALL;
		final MinimumTrade minimumTrade = MinimumTrade.FIVE_HUNDRED;
		final MacdConfiguration macdConfiguration = MacdConfiguration.MEDIUM;

		configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
		        getVanguardEftFeeCalculator(), brokerage, creatSameDaySignalFilter(macdConfiguration),
		        macdConfiguration));

		return configurations;
	}

	// TODO these fees should go somewhere, another configuration enum?
	private static EquityManagementFeeCalculator getVanguardEftFeeCalculator() {
		// return new ZeroEquityManagementFeeStructure()
		return new FlatEquityManagementFeeCalculator(BigDecimal.valueOf(0.0018), MATH_CONTEXT);
	}

	private static SignalFilter creatSameDaySignalFilter( final SignalConfiguration... entrySignals ) {

		final IndicatorSignalType[] passed = new IndicatorSignalType[entrySignals.length];
		for (int i = 0; i < entrySignals.length; i++) {
			passed[i] = entrySignals[i].getType();
		}
		return new IndicatorsOnSameDaySignalFilter(passed);
	}
}