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
package com.systematic.trading.backtest.context;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityManagementFeeConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityWithFeeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmationConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.ConfirmedByEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.IndicatorEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.OperatorEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.PeriodicEntryConfiguration;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signal.range.SignalRangeFilter;
import com.systematic.trading.signal.range.SimulationDatesRangeFilterDecorator;
import com.systematic.trading.signal.range.TradingDaySignalRangeFilter;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.PeriodicEquityManagementFeeStructure;
import com.systematic.trading.simulation.equity.fee.management.ZeroEquityManagementFeeCalculator;
import com.systematic.trading.strategy.Strategy;
import com.systematic.trading.strategy.TradingStrategyFactory;
import com.systematic.trading.strategy.confirmation.TradingStrategyConfirmedBy;
import com.systematic.trading.strategy.entry.Entry;
import com.systematic.trading.strategy.entry.size.AbsoluteEntryPositionBounds;
import com.systematic.trading.strategy.entry.size.EntryPositionBounds;
import com.systematic.trading.strategy.entry.size.EntrySize;
import com.systematic.trading.strategy.entry.size.LargestPossibleEntryPosition;
import com.systematic.trading.strategy.entry.size.RelativeEntryPositionBounds;
import com.systematic.trading.strategy.exit.Exit;
import com.systematic.trading.strategy.exit.size.ExitSize;
import com.systematic.trading.strategy.exit.size.NeverExitPosition;
import com.systematic.trading.strategy.indicator.IndicatorGeneratorFactory;
import com.systematic.trading.strategy.operator.Operator;
import com.systematic.trading.strategy.operator.TradingStrategyAndOperator;
import com.systematic.trading.strategy.operator.TradingStrategyOrOperator;
import com.systematic.trading.strategy.periodic.TradingStrategyPeriodic;

/**
 * Creates the Bootstrap configurations for back testing.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapContextBulider {

	/** How long one year is as a period of time/ */
	private static final Period ONE_YEAR = Period.ofYears(1);

	/** Decimal places for the equity. */
	private static final int EQUITY_SCALE = 4;

	/** Single equity to create the configuration on. */
	private EquityConfiguration equity;

	/** Weekly deposit amount into the cash account. */
	private DepositConfiguration deposit;

	/** First date to apply the management fee on. */
	private LocalDate managementFeeStartDate;

	/** The intended dates for the simulation. */
	private BacktestSimulationDates simulationDates;

	private StrategyConfiguration strategy;

	private BrokerageFeesConfiguration brokerageType;

	private SignalAnalysisListener signalAnalysisListener;

	public BacktestBootstrapContextBulider withConfiguration( final BacktestBootstrapConfiguration configuration ) {
		this.simulationDates = configuration.getBacktestDates();
		this.managementFeeStartDate = getFirstDayOfYear(simulationDates.getStartDate());
		this.simulationDates = configuration.getBacktestDates();
		this.deposit = configuration.getDeposit();
		this.equity = configuration.getEquity();
		this.brokerageType = configuration.getBrokerageFees();
		this.strategy = configuration.getStrategy();
		return this;
	}

	public BacktestBootstrapContextBulider withSignalAnalysisListeners( final SignalAnalysisListener listener ) {
		this.signalAnalysisListener = listener;
		return this;
	}

	public BacktestBootstrapContext build() {
		return new BacktestBootstrapContext(createStrategy(), createBrokerage(), createCashAccount(), simulationDates);
	}

	private Strategy createStrategy() {
		return new TradingStrategyFactory().strategy(createEntry(), createEntryPositionSize(), createExit(),
		        createExitPositionSize(), EquityClass.STOCK, EQUITY_SCALE);
	}

	private Entry createEntry() {
		final EntryConfiguration entryConfig = strategy.getEntry();

		return createEntry(entryConfig,
		        createSignalRangeFilter(startConfirmationRange(entryConfig) + endConfirmationRange(entryConfig)));
	}

	/**
	 * @param signalRange widest signal range, eventually will be overridden by a confirmation.
	 */
	private Entry createEntry( final EntryConfiguration entryConfig, final SignalRangeFilter signalRange ) {

		if (entryConfig instanceof PeriodicEntryConfiguration) {
			return createEntry((PeriodicEntryConfiguration) entryConfig);
		}

		if (entryConfig instanceof IndicatorEntryConfiguration) {
			return createEntry((IndicatorEntryConfiguration) entryConfig, signalRange);
		}

		if (entryConfig instanceof ConfirmedByEntryConfiguration) {
			return createEntry((ConfirmedByEntryConfiguration) entryConfig, signalRange);
		}

		if (entryConfig instanceof OperatorEntryConfiguration) {
			return createEntry((OperatorEntryConfiguration) entryConfig, signalRange);
		}

		throw new IllegalArgumentException(String.format("Entry configuration not supported: %s", entryConfig));
	}

	private Entry createEntry( final OperatorEntryConfiguration operatorConfig, final SignalRangeFilter signalRange ) {
		final Operator operator;

		switch (operatorConfig.getOp()) {
			case AND:
				operator = new TradingStrategyAndOperator();
			break;
			default:
			case OR:
				operator = new TradingStrategyOrOperator();
			break;
		}

		return new TradingStrategyFactory().entry(createEntry(operatorConfig.getLeftEntry(), signalRange), operator,
		        createEntry(operatorConfig.getRighEntry(), signalRange));
	}

	private Entry createEntry( final ConfirmedByEntryConfiguration confirmedByConfig,
	        final SignalRangeFilter signalRange ) {
		final ConfirmationConfiguration.Type by = confirmedByConfig.getConfirmBy();

		return new TradingStrategyFactory().entry(createEntry(confirmedByConfig.getAnchor(), signalRange),
		        new TradingStrategyConfirmedBy(by.getConfirmationDayRange(), by.getDelayUntilConfirmationRange()),
		        createEntry(confirmedByConfig.getConfirmation(), signalRange));
	}

	private Entry createEntry( final IndicatorEntryConfiguration indicatorConfig,
	        final SignalRangeFilter signalRange ) {
		return new TradingStrategyFactory().entry(new IndicatorGeneratorFactory()
		        .create(indicatorConfig.getIndicator(), signalRange, signalAnalysisListener));
	}

	private Entry createEntry( final PeriodicEntryConfiguration periodicConfig ) {
		return new TradingStrategyFactory().entry(new TradingStrategyPeriodic(simulationDates.getStartDate(),
		        (periodicConfig).getFrequency().getFrequency(), SignalType.BULLISH));
	}

	/**
	 * Widest value for the confirmationDayRange, accounting for the latest delay until confirmation range begins. 
	 */
	private int endConfirmationRange( final EntryConfiguration entryConfig ) {

		if (entryConfig instanceof ConfirmedByEntryConfiguration) {
			final ConfirmedByEntryConfiguration confirmedByConfig = (ConfirmedByEntryConfiguration) entryConfig;
			final int configEnd = confirmedByConfig.getConfirmBy().getConfirmationDayRange()
			        + confirmedByConfig.getConfirmBy().getDelayUntilConfirmationRange();

			return Math.max(configEnd, Math.max(endConfirmationRange(confirmedByConfig.getAnchor()),
			        endConfirmationRange(confirmedByConfig.getConfirmation())));
		}

		if (entryConfig instanceof OperatorEntryConfiguration) {
			final OperatorEntryConfiguration operatorConfig = (OperatorEntryConfiguration) entryConfig;
			return Math.max(endConfirmationRange(operatorConfig.getLeftEntry()),
			        endConfirmationRange(operatorConfig.getRighEntry()));
		}

		// No range, i.e. only the current trading date
		return 0;
	}

	private int startConfirmationRange( final EntryConfiguration entryConfig ) {

		if (entryConfig instanceof ConfirmedByEntryConfiguration) {
			final ConfirmedByEntryConfiguration confirmedByConfig = (ConfirmedByEntryConfiguration) entryConfig;

			return Math.min(confirmedByConfig.getConfirmBy().getDelayUntilConfirmationRange(),
			        Math.min(startConfirmationRange(confirmedByConfig.getAnchor()),
			                startConfirmationRange(confirmedByConfig.getConfirmation())));
		}

		if (entryConfig instanceof OperatorEntryConfiguration) {
			final OperatorEntryConfiguration operatorConfig = (OperatorEntryConfiguration) entryConfig;
			return Math.min(startConfirmationRange(operatorConfig.getLeftEntry()),
			        startConfirmationRange(operatorConfig.getRighEntry()));
		}

		// No range, i.e. only the current trading date
		return 0;
	}

	private SignalRangeFilter createSignalRangeFilter( final int previousTradingDaySignalRange ) {
		return new SimulationDatesRangeFilterDecorator(simulationDates.getStartDate(), simulationDates.getEndDate(),
		        new TradingDaySignalRangeFilter(previousTradingDaySignalRange));
	}

	private Exit createExit() {
		return new TradingStrategyFactory().exit();
	}

	private ExitSize createExitPositionSize() {
		return new NeverExitPosition();
	}

	private EntrySize createEntryPositionSize() {
		final EntryPositionBounds minimum = new AbsoluteEntryPositionBounds(
		        strategy.getEntryPositionSizing().getMinimumTrade().getValue());
		final EntryPositionBounds maximum = new RelativeEntryPositionBounds(
		        strategy.getEntryPositionSizing().getMaximumTrade().getValue());

		return new LargestPossibleEntryPosition(minimum, maximum);
	}

	private CashAccount createCashAccount() {
		return CashAccountFactory.getInstance().create(simulationDates.getStartDate(), deposit);
	}

	private Brokerage createBrokerage() {
		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.getEquityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));

		return BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        simulationDates.getStartDate());
	}

	private LocalDate getFirstDayOfYear( final LocalDate date ) {
		return LocalDate.of(date.getYear(), 1, 1);
	}

	private EquityManagementFeeCalculator createFeeCalculator( final EquityManagementFeeConfiguration managementFee ) {
		switch (managementFee) {
			case VGS:
				return new FlatEquityManagementFeeCalculator(managementFee.getPercentageFee()[0]);

			case NONE:
			default:
				return new ZeroEquityManagementFeeCalculator();
		}
	}
}