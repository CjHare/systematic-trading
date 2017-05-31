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
package com.systematic.trading.backtest;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.input.OutputLaunchArgument;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;

/**
 * Executes all configurations.
 * 
 * @author CJ Hare
 */
public class AllConfigurations implements BacktestConfiguration {

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	public static void main( final String... args ) throws Exception {
		new BacktestApplication(MATH_CONTEXT).runTest(new AllConfigurations(), new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), args));
	}

	@Override
	public List<BacktestBootstrapConfiguration> get( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		final EntryLogicConfiguration weeklyBuy = new EntryLogicConfiguration(PeriodicFilterConfiguration.WEEKLY,
		        MaximumTrade.ALL, MinimumTrade.ZERO);
		final EntryLogicConfiguration monthlyBuy = new EntryLogicConfiguration(PeriodicFilterConfiguration.MONTHLY,
		        MaximumTrade.ALL, MinimumTrade.ZERO);

		// Vanguard Retail
		configurations.add(new BacktestBootstrapConfiguration(simulationDates,
		        BrokerageFeesConfiguration.VANGUARD_RETAIL, CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY,
		        deposit, weeklyBuy, equity, ExitLogicConfiguration.HOLD_FOREVER));

		// CMC Weekly
		configurations.add(new BacktestBootstrapConfiguration(simulationDates, BrokerageFeesConfiguration.CMC_MARKETS,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, weeklyBuy, equity,
		        ExitLogicConfiguration.HOLD_FOREVER));

		// CMC Monthly
		configurations.add(new BacktestBootstrapConfiguration(simulationDates, BrokerageFeesConfiguration.CMC_MARKETS,
		        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, monthlyBuy, equity,
		        ExitLogicConfiguration.HOLD_FOREVER));

		// All signal based use the trading account
		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.CMC_MARKETS;

		for (final MaximumTrade maximumTrade : MaximumTrade.values()) {
			for (final MinimumTrade minimumTrade : MinimumTrade.values()) {
				configurations.addAll(
				        getConfigurations(simulationDates, deposit, equity, brokerage, minimumTrade, maximumTrade));
			}
		}

		return configurations;
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations(
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final EquityConfiguration equity, final BrokerageFeesConfiguration brokerage,
	        final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<>();

		//TODO need to fix the configurations, result of refactoring while code commented out
		/*
		
		EntryLogicConfiguration entry;
		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
		
				// MACD & RSI
				entry = new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				        macdConfiguration, rsiConfiguration), maximumTrade, minimumTrade);
				configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
				        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
				        ExitLogicConfiguration.HOLD_FOREVER));
		
				for (final ConfirmationSignalFilterConfiguration.Type filterConfigurations : ConfirmationSignalFilterConfiguration.Type
				        .values()) {
		
					// MACD & RSI - confirmation signals
					entry = new EntryLogicConfiguration(new ConfirmationSignalFilterConfiguration(filterConfigurations,
					        rsiConfiguration, macdConfiguration), maximumTrade, minimumTrade);
					configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
					        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
					        ExitLogicConfiguration.HOLD_FOREVER));
				}
			}
		
			// MACD only
			entry = new EntryLogicConfiguration(
			        new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, macdConfiguration),
			        maximumTrade, minimumTrade);
			configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
			        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
			        ExitLogicConfiguration.HOLD_FOREVER));
		
			for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
				for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
		
					// MACD, SMA & RSI
					entry = new EntryLogicConfiguration(
					        new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL, macdConfiguration,
					                smaConfiguration, rsiConfiguration),
					        maximumTrade, minimumTrade);
					configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
					        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
					        ExitLogicConfiguration.HOLD_FOREVER));
				}
		
				// MACD & SMA
				entry = new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				        macdConfiguration, smaConfiguration), maximumTrade, minimumTrade);
				configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
				        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
				        ExitLogicConfiguration.HOLD_FOREVER));
			}
		}
		
		for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
			for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
		
				// SMA & RSI			
				entry = new EntryLogicConfiguration(new SameDayFilterConfiguration(SameDayFilterConfiguration.Type.ALL,
				        smaConfiguration, rsiConfiguration), maximumTrade, minimumTrade);
				configurations.add(new BacktestBootstrapConfiguration(simulationDates, brokerage,
				        CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, deposit, entry, equity,
				        ExitLogicConfiguration.HOLD_FOREVER));
			}
		}
		 */

		return configurations;
	}
}