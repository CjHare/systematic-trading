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
package com.systematic.trading.backtest.trial.never.exit.same.brokerage;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.BacktestApplication;
import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.brokerage.fee.VanguardBrokerageFees;
import com.systematic.trading.backtest.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.backtest.input.DataServiceTypeLaunchArgument;
import com.systematic.trading.backtest.input.EndDateLaunchArgument;
import com.systematic.trading.backtest.input.EquityDatasetLaunchArgument;
import com.systematic.trading.backtest.input.FileBaseDirectoryLaunchArgument;
import com.systematic.trading.backtest.input.LaunchArgumentValidator;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.input.OutputLaunchArgument;
import com.systematic.trading.backtest.input.StartDateLaunchArgument;
import com.systematic.trading.backtest.input.TickerSymbolLaunchArgument;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.backtest.trial.AllTrials;

/**
 * All strategies using the same Vanguard brokerage.
 * <p/>
 * Purpose being to remove the position sizing and brokerage variables when comparing. 
 * 
 * @author CJ Hare
 */
public class AllStratgiesAgnosticSizingBrokerageTrial extends AllTrials implements BacktestConfiguration {

	public static void main( final String... args ) throws Exception {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();

		final LaunchArguments launchArgs = new LaunchArguments(new CommandLineLaunchArgumentsParser(),
		        new OutputLaunchArgument(validator), new DataServiceTypeLaunchArgument(),
		        new StartDateLaunchArgument(validator), new EndDateLaunchArgument(validator),
		        new EquityDatasetLaunchArgument(), new TickerSymbolLaunchArgument(validator),
		        new FileBaseDirectoryLaunchArgument(validator), args);

		new BacktestApplication(launchArgs.getDataService()).runBacktest(new AllStratgiesAgnosticSizingBrokerageTrial(),
		        launchArgs);
	}

	private static Set<Pair<MinimumTrade, MaximumTrade>> getPositionSizing() {
		final Set<Pair<MinimumTrade, MaximumTrade>> tradeSizes = new HashSet<>();
		tradeSizes.add(new ImmutablePair<MinimumTrade, MaximumTrade>(MinimumTrade.ZERO, MaximumTrade.ALL));
		return tradeSizes;
	}

	public AllStratgiesAgnosticSizingBrokerageTrial() {
		super(new VanguardBrokerageFees(), getPositionSizing());
	}
}