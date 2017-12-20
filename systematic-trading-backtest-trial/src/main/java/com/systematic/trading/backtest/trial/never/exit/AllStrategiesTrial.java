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
package com.systematic.trading.backtest.trial.never.exit;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.BacktestTrial;
import com.systematic.trading.backtest.brokerage.fee.CmcMarketsBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.AllTrials;
import com.systematic.trading.input.BacktestLaunchArguments;
import com.systematic.trading.input.BigDecimalLaunchArgument;
import com.systematic.trading.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.input.DataServiceTypeLaunchArgument;
import com.systematic.trading.input.EndDateLaunchArgument;
import com.systematic.trading.input.EquityArguments;
import com.systematic.trading.input.EquityDatasetLaunchArgument;
import com.systematic.trading.input.FileBaseDirectoryLaunchArgument;
import com.systematic.trading.input.LaunchArgument;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;
import com.systematic.trading.input.LaunchArgumentValidator;
import com.systematic.trading.input.OutputLaunchArgument;
import com.systematic.trading.input.StartDateLaunchArgument;
import com.systematic.trading.input.TickerSymbolLaunchArgument;

/**
 * All strategies, all sizing combinations with actual brokerage.
 * <p/>
 * Purpose being to remove the position sizing and brokerage variables when comparing. 
 * 
 * @author CJ Hare
 */
public class AllStrategiesTrial extends AllTrials implements BacktestConfiguration {

	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();
		final Map<ArgumentKey, String> arguments = new CommandLineLaunchArgumentsParser().parse(args);
		final BacktestLaunchArguments launchArgs = new BacktestLaunchArguments(new OutputLaunchArgument(validator),
		        new EquityArguments(new DataServiceTypeLaunchArgument(), new EquityDatasetLaunchArgument(validator),
		                new TickerSymbolLaunchArgument(validator), arguments),
		        new BigDecimalLaunchArgument(validator, LaunchArgument.ArgumentKey.OPENING_FUNDS),
		        new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		        new FileBaseDirectoryLaunchArgument(validator), arguments);

		new BacktestTrial(launchArgs.dataService()).runBacktest(new AllStrategiesTrial(), launchArgs);
	}

	private static Set<Pair<MinimumTrade, MaximumTrade>> getPositionSizing() {

		final Set<Pair<MinimumTrade, MaximumTrade>> tradeSizes = new HashSet<>();

		for (final MinimumTrade minimum : MinimumTrade.values()) {
			for (final MaximumTrade maximum : MaximumTrade.values()) {
				tradeSizes.add(new ImmutablePair<MinimumTrade, MaximumTrade>(minimum, maximum));
			}
		}

		return tradeSizes;
	}

	public AllStrategiesTrial() {
		super(new CmcMarketsBrokerageFees(), getPositionSizing());
	}

	@Override
	public List<BacktestBootstrapConfiguration> configuration( final EquityConfiguration equity,
	        final BacktestSimulationDates simulationDates, final BigDecimal openingFunds,
	        final DepositConfiguration deposit ) {

		List<BacktestBootstrapConfiguration> configurations = super.configuration(equity, simulationDates, openingFunds,
		        deposit);

		// Vanguard Retail - baseline
		configurations.add(baseline(equity, simulationDates, openingFunds, deposit));

		return configurations;
	}
}