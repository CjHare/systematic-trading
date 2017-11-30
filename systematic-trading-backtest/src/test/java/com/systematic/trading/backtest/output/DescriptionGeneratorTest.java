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
package com.systematic.trading.backtest.output;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * Verifying the DescriptionGenerator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class DescriptionGeneratorTest {

	/** Description generator instance being tested. */
	private DescriptionGenerator descriptions;

	@Before
	public void setUp() {
		descriptions = new DescriptionGenerator();
	}

	@Test
	public void minMaxs() {
		final String description = descriptions.getDescription(MinimumTrade.FIVE_HUNDRED, MaximumTrade.HALF);

		assertEquals("Minimum_500_Maximum_50_percent", description);
	}

	@Test
	public void bootstrapConfiguration() {
		final BacktestBootstrapConfiguration configuration = setUpBootstrapConfiguration();

		final String description = descriptions.getDescription(configuration);

		assertEquals("ZXY_SelfWealth_InterestDaily_sTrategy-deScription", description);
	}

	@Test
	public void strategy() {
		final EntryConfiguration entry = setUpEntry("eNTry");
		final EntrySizeConfiguration entryPositionSizing = setUpEntrySizing("EntrySizing");
		final ExitConfiguration exit = setUpExit("exIT");
		final ExitSizeConfiguration exitPositionSizing = setUpExitSizing("eXItSizing");

		final String description = descriptions.getDescription(entry, entryPositionSizing, exit, exitPositionSizing);

		assertEquals("eNTry_EntrySizing_exIT_eXItSizing", description);
	}

	private ExitSizeConfiguration setUpExitSizing( final String description ) {
		final ExitSizeConfiguration entry = mock(ExitSizeConfiguration.class);
		when(entry.getDescription()).thenReturn(description);
		return entry;
	}

	private ExitConfiguration setUpExit( final String description ) {
		final ExitConfiguration exit = mock(ExitConfiguration.class);
		when(exit.getDescription()).thenReturn(description);
		return exit;
	}

	private EntrySizeConfiguration setUpEntrySizing( final String description ) {
		final EntrySizeConfiguration entry = mock(EntrySizeConfiguration.class);
		when(entry.getDescription()).thenReturn(description);
		return entry;
	}

	private EntryConfiguration setUpEntry( final String description ) {
		final EntryConfiguration entry = mock(EntryConfiguration.class);
		when(entry.getDescription()).thenReturn(description);
		return entry;
	}

	private BacktestBootstrapConfiguration setUpBootstrapConfiguration() {
		final BacktestSimulationDates backtestDates = mock(BacktestSimulationDates.class);
		final BrokerageTransactionFeeStructure brokerageFees = mock(SelfWealthBrokerageFees.class);
		final CashAccountConfiguration cashAccount = CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY;
		final DepositConfiguration deposit = DepositConfiguration.WEEKLY_200;
		final StrategyConfiguration strategy = mock(StrategyConfiguration.class);
		when(strategy.getDescription()).thenReturn("sTrategy-deScription");
		final EquityConfiguration equity = mock(EquityConfiguration.class);
		when(equity.getEquityIdentity()).thenReturn(new EquityIdentity("ZXY", null, 0));

		return new BacktestBootstrapConfiguration(backtestDates, brokerageFees, cashAccount, deposit, strategy, equity);
	}
}