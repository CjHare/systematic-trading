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
package com.systematic.trading.backtest.context;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.TradingStrategyIndicatorFactory;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityManagementFeeConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityWithFeeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.ConfirmedByEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.IndicatorEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.OperatorEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.PeriodicEntryConfiguration;
import com.systematic.trading.model.equity.EquityClass;
import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.signal.range.SignalRangeFilter;
import com.systematic.trading.signal.range.SimulationDatesRangeFilterDecorator;
import com.systematic.trading.signal.range.TradingDaySignalRangeFilter;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
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
import com.systematic.trading.strategy.operator.Operator;
import com.systematic.trading.strategy.operator.TradingStrategyAndOperator;
import com.systematic.trading.strategy.operator.TradingStrategyOrOperator;
import com.systematic.trading.strategy.periodic.TradingStrategyPeriodic;
import com.systematic.trading.strategy.signal.SignalAnalysisListener;

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

	/** Cash account configuration details. */
	private CashAccountConfiguration cashAccount;

	/** First date to apply the management fee on. */
	private LocalDate managementFeeStartDate;

	/** The intended dates for the simulation. */
	private BacktestSimulationDates simulationDates;

	private StrategyConfiguration strategy;

	private BrokerageTransactionFeeStructure brokerageType;

	private SignalAnalysisListener signalAnalysisListener;

	public BacktestBootstrapContextBulider withConfiguration( final BacktestBootstrapConfiguration configuration ) {

		this.simulationDates = configuration.backtestDates();
		this.managementFeeStartDate = firstDayOfYear(simulationDates.startDateInclusive());
		this.cashAccount = configuration.cashAccount();
		this.equity = configuration.equity();
		this.brokerageType = configuration.brokerageFees();
		this.strategy = configuration.strategy();
		return this;
	}

	public BacktestBootstrapContextBulider withSignalAnalysisListeners( final SignalAnalysisListener listener ) {

		this.signalAnalysisListener = listener;
		return this;
	}

	public BacktestBootstrapContext build() {

		return new BacktestBootstrapContext(strategy(), brokerage(), cashAccount(), simulationDates);
	}

	private Strategy strategy() {

		return new TradingStrategyFactory()
		        .strategy(entry(), entryPositionSize(), exit(), exitPositionSize(), EquityClass.STOCK, EQUITY_SCALE);
	}

	private Entry entry() {

		final EntryConfiguration entryConfig = strategy.entry();

		return entry(
		        entryConfig,
		        signalRangeFilter(startConfirmationRange(entryConfig) + endConfirmationRange(entryConfig)),
		        priceDataRange(entryConfig));
	}

	private long priceDataRange( final EntryConfiguration entryConfig ) {

		return entryConfig.priceDataRange().get(ChronoUnit.DAYS);
	}

	/**
	 * @param signalRange
	 *            widest signal range, eventually will be overridden by a confirmation.
	 */
	private Entry entry(
	        final EntryConfiguration entryConfig,
	        final SignalRangeFilter signalRange,
	        final long priceDataRange ) {

		if (entryConfig instanceof PeriodicEntryConfiguration) { return periodicEntry(
		        (PeriodicEntryConfiguration) entryConfig); }

		if (entryConfig instanceof IndicatorEntryConfiguration) { return indicatorEntry(
		        (IndicatorEntryConfiguration) entryConfig,
		        signalRange,
		        priceDataRange); }

		if (entryConfig instanceof ConfirmedByEntryConfiguration) { return confirmByEntry(
		        (ConfirmedByEntryConfiguration) entryConfig,
		        signalRange,
		        priceDataRange); }

		if (entryConfig instanceof OperatorEntryConfiguration) { return operatorEntry(
		        (OperatorEntryConfiguration) entryConfig,
		        signalRange,
		        priceDataRange); }

		throw new IllegalArgumentException(String.format("Entry configuration not supported: %s", entryConfig));
	}

	private Entry operatorEntry(
	        final OperatorEntryConfiguration operatorConfig,
	        final SignalRangeFilter signalRange,
	        final long priceDataRange ) {

		final Operator operator;

		switch (operatorConfig.operator()) {
			case AND:
				operator = new TradingStrategyAndOperator();
				break;
			case OR:
			default:
				operator = new TradingStrategyOrOperator();
				break;
		}

		return new TradingStrategyFactory().entry(
		        entry(operatorConfig.leftEntry(), signalRange, priceDataRange),
		        operator,
		        entry(operatorConfig.righEntry(), signalRange, priceDataRange));
	}

	private Entry confirmByEntry(
	        final ConfirmedByEntryConfiguration confirmedByConfig,
	        final SignalRangeFilter signalRange,
	        final long priceDataRange ) {

		final ConfirmaByConfiguration by = confirmedByConfig.confirmBy();

		return new TradingStrategyFactory().entry(
		        entry(confirmedByConfig.anchor(), signalRange, priceDataRange),
		        new TradingStrategyConfirmedBy(by.confirmationDayRange(), by.delayUntilConfirmationRange()),
		        entry(confirmedByConfig.confirmation(), signalRange, priceDataRange));
	}

	private Entry indicatorEntry(
	        final IndicatorEntryConfiguration indicatorConfig,
	        final SignalRangeFilter signalRange,
	        final long priceDataRange ) {

		return new TradingStrategyFactory().entry(
		        new TradingStrategyIndicatorFactory().create(
		                indicatorConfig.indicator(),
		                signalRange,
		                signalAnalysisListener,
		                (int) priceDataRange));
	}

	private Entry periodicEntry( final PeriodicEntryConfiguration periodicConfig ) {

		return new TradingStrategyFactory().entry(
		        new TradingStrategyPeriodic(
		                simulationDates.startDateInclusive(),
		                (periodicConfig).frequency().frequency(),
		                SignalType.BULLISH));
	}

	/**
	 * Widest value for the confirmationDayRange, accounting for the latest delay until confirmation
	 * range begins.
	 */
	private int endConfirmationRange( final EntryConfiguration entryConfig ) {

		if (entryConfig instanceof ConfirmedByEntryConfiguration) {
			final ConfirmedByEntryConfiguration confirmedByConfig = (ConfirmedByEntryConfiguration) entryConfig;
			final int configEnd = confirmedByConfig.confirmBy().confirmationDayRange()
			        + confirmedByConfig.confirmBy().delayUntilConfirmationRange();

			return Math.max(
			        configEnd,
			        Math.max(
			                endConfirmationRange(confirmedByConfig.anchor()),
			                endConfirmationRange(confirmedByConfig.confirmation())));
		}

		if (entryConfig instanceof OperatorEntryConfiguration) {
			final OperatorEntryConfiguration operatorConfig = (OperatorEntryConfiguration) entryConfig;
			return Math.max(
			        endConfirmationRange(operatorConfig.leftEntry()),
			        endConfirmationRange(operatorConfig.righEntry()));
		}

		// No range, i.e. only the current trading date
		return 0;
	}

	private int startConfirmationRange( final EntryConfiguration entryConfig ) {

		if (entryConfig instanceof ConfirmedByEntryConfiguration) {
			final ConfirmedByEntryConfiguration confirmedByConfig = (ConfirmedByEntryConfiguration) entryConfig;

			return Math.min(
			        confirmedByConfig.confirmBy().delayUntilConfirmationRange(),
			        Math.min(
			                startConfirmationRange(confirmedByConfig.anchor()),
			                startConfirmationRange(confirmedByConfig.confirmation())));
		}

		if (entryConfig instanceof OperatorEntryConfiguration) {
			final OperatorEntryConfiguration operatorConfig = (OperatorEntryConfiguration) entryConfig;
			return Math.min(
			        startConfirmationRange(operatorConfig.leftEntry()),
			        startConfirmationRange(operatorConfig.righEntry()));
		}

		// No range, i.e. only the current trading date
		return 0;
	}

	private SignalRangeFilter signalRangeFilter( final int previousTradingDaySignalRange ) {

		return new SimulationDatesRangeFilterDecorator(
		        simulationDates.startDateInclusive(),
		        simulationDates.endDateExclusive(),
		        new TradingDaySignalRangeFilter(previousTradingDaySignalRange));
	}

	private Exit exit() {

		return new TradingStrategyFactory().exit();
	}

	private ExitSize exitPositionSize() {

		return new NeverExitPosition();
	}

	private EntrySize entryPositionSize() {

		final EntryPositionBounds minimum = new AbsoluteEntryPositionBounds(
		        strategy.entryPositionSizing().minimumTrade().value());
		final EntryPositionBounds maximum = new RelativeEntryPositionBounds(
		        strategy.entryPositionSizing().maximumTrade().value());

		return new LargestPossibleEntryPosition(minimum, maximum);
	}

	private CashAccount cashAccount() {

		return new CashAccountFactory().create(simulationDates.startDateInclusive(), cashAccount);
	}

	private Brokerage brokerage() {

		final EquityManagementFeeCalculator feeCalculator = feeCalculator(equity.managementFee());
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.equityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));

		return new BrokerageFactoroy().create(equityConfiguration, brokerageType, simulationDates.startDateInclusive());
	}

	private LocalDate firstDayOfYear( final LocalDate date ) {

		return LocalDate.of(date.getYear(), 1, 1);
	}

	private EquityManagementFeeCalculator feeCalculator( final EquityManagementFeeConfiguration managementFee ) {

		switch (managementFee) {
			case VGS:
				return new FlatEquityManagementFeeCalculator(managementFee.percentageFee()[0]);

			case NONE:
			default:
				return new ZeroEquityManagementFeeCalculator();
		}
	}
}
