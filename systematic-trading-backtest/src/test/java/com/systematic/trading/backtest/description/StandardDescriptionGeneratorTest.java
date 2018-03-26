/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.description;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.cash.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.input.DepositFrequency;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.model.equity.EquityIdentity;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.strategy.indicator.IndicatorId;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;

/**
 * Verifying the DescriptionGenerator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class StandardDescriptionGeneratorTest {

	/** Description generator instance being tested. */
	private DescriptionGenerator descriptions;

	@Before
	public void setUp() {

		descriptions = new StandardDescriptionGenerator();
	}

	@Test
	public void minMaxs() {

		final String description = descriptions.positionSize(MinimumTrade.FIVE_HUNDRED, MaximumTrade.HALF);

		assertEquals("Minimum_500_Maximum_50_percent", description);
	}

	@Test
	public void bootstrapConfiguration() {

		final BacktestBootstrapConfiguration configuration = setUpBootstrapConfiguration();

		final String description = descriptions.bootstrapConfiguration(configuration);

		assertEquals("ZXY_SelfWealth_sTrategy-deScription", description);
	}

	@Test
	public void bootstrapConfigurationWithDeposit() {

		final BacktestBootstrapConfiguration configuration = setUpBootstrapConfiguration();
		final DepositConfiguration depositAmount = new DepositConfiguration(
		        BigDecimal.valueOf(150),
		        DepositFrequency.WEEKLY);

		final String description = descriptions.bootstrapConfigurationWithDeposit(configuration, depositAmount);

		assertEquals("ZXY_SelfWealth_Deposit_150_Weekly_sTrategy-deScription", description);
	}

	@Test
	public void strategy() {

		final EntryConfiguration entry = setUpEntry("eNTry");
		final EntrySizeConfiguration entryPositionSizing = setUpEntrySizing("EntrySizing");
		final ExitConfiguration exit = setUpExit("exIT");
		final ExitSizeConfiguration exitPositionSizing = setUpExitSizing("eXItSizing");

		final String description = descriptions.strategy(entry, entryPositionSizing, exit, exitPositionSizing);

		assertEquals("eNTry_EntrySizing_exIT_eXItSizing", description);
	}

	@Test
	public void monthlyPeriodicEntry() {

		final PeriodicConfiguration frequency = PeriodicConfiguration.MONTHLY;

		final String description = descriptions.periodicEntry(frequency);

		assertEquals("Buy-Monthly", description);
	}

	@Test
	public void weeklyPeriodicEntry() {

		final PeriodicConfiguration frequency = PeriodicConfiguration.WEEKLY;

		final String description = descriptions.periodicEntry(frequency);

		assertEquals("Buy-Weekly", description);
	}

	@Test
	public void indicator() {

		final IndicatorConfiguration indicator = setUpIndicator();

		final String description = descriptions.indicator(indicator);

		assertEquals("Indicator", description);
	}

	@Test
	public void entryLogicalOr() {

		final EntryConfiguration leftEntry = setUpEntry("LEFT-Entry");
		final EntryConfiguration rightEntry = setUpEntry("RIGHT-Entry");

		final String description = descriptions.entry(leftEntry, OperatorConfiguration.Selection.OR, rightEntry);

		assertEquals("LEFT-Entry_OR_RIGHT-Entry", description);
	}

	@Test
	public void entryLogicalOrLeftSubEntry() {

		final EntryConfiguration leftEntry = setUpEntryWithSubEntry("LEFT-Entry");
		final EntryConfiguration rightEntry = setUpEntryWithSubEntry("RIGHT-Entry");

		final String description = descriptions.entry(leftEntry, OperatorConfiguration.Selection.OR, rightEntry);

		assertEquals("(LEFT-Entry)_OR_(RIGHT-Entry)", description);
	}

	@Test
	public void entryConfirmation() {

		final EntryConfiguration anchor = setUpEntry("anCHor");
		final EntryConfiguration confirmation = setUpEntry("CONfirmaTION");

		final String description = descriptions
		        .entry(anchor, ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_FOUR_DAYS, confirmation);

		assertEquals("anCHor_confirmedBy_CONfirmaTION_in_1_to_5_days", description);
	}

	@Test
	public void entryConfirmationSubEntries() {

		final EntryConfiguration anchor = setUpEntryWithSubEntry("anCHor");
		final EntryConfiguration confirmation = setUpEntryWithSubEntry("CONfirmaTION");

		final String description = descriptions
		        .entry(anchor, ConfirmaByConfiguration.DELAY_ONE_DAY_RANGE_FOUR_DAYS, confirmation);

		assertEquals("(anCHor)_confirmedBy_(CONfirmaTION)_in_1_to_5_days", description);
	}

	private IndicatorConfiguration setUpIndicator() {

		final IndicatorConfiguration indicator = mock(IndicatorConfiguration.class);
		when(indicator.id()).thenReturn(new IndicatorId("Indicator"));
		return indicator;
	}

	private ExitSizeConfiguration setUpExitSizing( final String description ) {

		final ExitSizeConfiguration entry = mock(ExitSizeConfiguration.class);
		when(entry.description(any(DescriptionGenerator.class))).thenReturn(description);
		return entry;
	}

	private ExitConfiguration setUpExit( final String description ) {

		final ExitConfiguration exit = mock(ExitConfiguration.class);
		when(exit.description(any(DescriptionGenerator.class))).thenReturn(description);
		return exit;
	}

	private EntrySizeConfiguration setUpEntrySizing( final String description ) {

		final EntrySizeConfiguration entry = mock(EntrySizeConfiguration.class);
		when(entry.description(any(DescriptionGenerator.class))).thenReturn(description);
		return entry;
	}

	private EntryConfiguration setUpEntryWithSubEntry( final String description ) {

		final EntryConfiguration entry = mock(EntryConfiguration.class);
		when(entry.description(any(DescriptionGenerator.class))).thenReturn(description);
		when(entry.hasSubEntry()).thenReturn(true);
		return entry;
	}

	private EntryConfiguration setUpEntry( final String description ) {

		final EntryConfiguration entry = mock(EntryConfiguration.class);
		when(entry.description(any(DescriptionGenerator.class))).thenReturn(description);
		return entry;
	}

	private BacktestBootstrapConfiguration setUpBootstrapConfiguration() {

		final BacktestSimulationDates backtestDates = mock(BacktestSimulationDates.class);
		final BrokerageTransactionFeeStructure brokerageFees = mock(SelfWealthBrokerageFees.class);
		final CashAccountConfiguration cashAccount = mock(CashAccountConfiguration.class);
		final StrategyConfiguration strategy = mock(StrategyConfiguration.class);
		when(strategy.description(any(DescriptionGenerator.class))).thenReturn("sTrategy-deScription");
		final EquityConfiguration equity = mock(EquityConfiguration.class);
		when(equity.gquityIdentity()).thenReturn(new EquityIdentity("ZXY", null, 0));

		return new BacktestBootstrapConfiguration(backtestDates, brokerageFees, cashAccount, strategy, equity);
	}
}
