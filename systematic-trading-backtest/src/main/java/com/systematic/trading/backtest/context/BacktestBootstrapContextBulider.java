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
import java.util.Optional;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
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
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.filter.SimulationDatesRangeFilterDecorator;
import com.systematic.trading.signals.filter.TradingDaySignalRangeFilter;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.PeriodicEquityManagementFeeStructure;
import com.systematic.trading.simulation.equity.fee.management.ZeroEquityManagementFeeCalculator;
import com.systematic.trading.strategy.confirmation.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.strategy.confirmation.TradingStrategyConfirmedBy;
import com.systematic.trading.strategy.definition.Entry;
import com.systematic.trading.strategy.definition.EntrySize;
import com.systematic.trading.strategy.definition.Exit;
import com.systematic.trading.strategy.definition.ExitSize;
import com.systematic.trading.strategy.definition.ExpressionLanguageFactory;
import com.systematic.trading.strategy.definition.Indicator;
import com.systematic.trading.strategy.definition.Operator;
import com.systematic.trading.strategy.definition.Strategy;
import com.systematic.trading.strategy.entry.size.AbsoluteEntryPositionBounds;
import com.systematic.trading.strategy.entry.size.EntryPositionBounds;
import com.systematic.trading.strategy.entry.size.LargestPossibleEntryPosition;
import com.systematic.trading.strategy.entry.size.RelativeEntryPositionBounds;
import com.systematic.trading.strategy.exit.size.NeverExitPosition;
import com.systematic.trading.strategy.indicator.IndicatorGeneratorFactory;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;
import com.systematic.trading.strategy.operator.AnyOfIndicatorFilterConfiguration;
import com.systematic.trading.strategy.operator.SameDayFilterConfiguration;
import com.systematic.trading.strategy.operator.TradingStrategyAndOperator;
import com.systematic.trading.strategy.operator.TradingStrategyOrOperator;
import com.systematic.trading.strategy.periodic.TradingStrategyPeriodic;

/**
 * Creates the Bootstrap configurations for back testing.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapContextBulider {

	//TODO convert into an actual builder pattern

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
		return new ExpressionLanguageFactory().strategy(createEntry(), createEntryPositionSize(), createExit(),
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

		return new ExpressionLanguageFactory().entry(createEntry(operatorConfig.getLeftEntry(), signalRange), operator,
		        createEntry(operatorConfig.getRighEntry(), signalRange));
	}

	private Entry createEntry( final ConfirmedByEntryConfiguration confirmedByConfig,
	        final SignalRangeFilter signalRange ) {

		//TODO specific signals

		final ConfirmationConfiguration.Type by = confirmedByConfig.getConfirmBy();

		return new ExpressionLanguageFactory().entry(createEntry(confirmedByConfig.getAnchor(), signalRange),
		        new TradingStrategyConfirmedBy(by.getConfirmationDayRange(), by.getDelayUntilConfirmationRange()),
		        createEntry(confirmedByConfig.getConfirmation(), signalRange));
	}

	private Entry createEntry( final IndicatorEntryConfiguration indicatorConfig,
	        final SignalRangeFilter signalRange ) {
		return new ExpressionLanguageFactory().entry(new IndicatorGeneratorFactory()
		        .create(indicatorConfig.getIndicator(), signalRange, signalAnalysisListener));
	}

	private Entry createEntry( final PeriodicEntryConfiguration periodicConfig ) {
		return new ExpressionLanguageFactory().entry(new TradingStrategyPeriodic(simulationDates.getStartDate(),
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

	private SignalRangeFilter createSignalRangeFilter() {
		return createSignalRangeFilter(0);
	}

	private SignalRangeFilter createSignalRangeFilter( final int previousTradingDaySignalRange ) {
		return new SimulationDatesRangeFilterDecorator(simulationDates.getStartDate(), simulationDates.getEndDate(),
		        new TradingDaySignalRangeFilter(previousTradingDaySignalRange));
	}

	private SignalRangeFilter createSignalRangeFilter( final ConfirmedByEntryConfiguration entryConfig ) {
		final ConfirmationConfiguration.Type by = entryConfig.getConfirmBy();
		return createSignalRangeFilter(by.getDelayUntilConfirmationRange() + by.getConfirmationDayRange());

	}

	private Exit createExit() {
		return new ExpressionLanguageFactory().exit();
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

	private BacktestBootstrapContext periodic( final BrokerageFeesConfiguration brokerageType,
	        final Period purchaseFrequency ) {
		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final LocalDate startDate = simulationDates.getStartDate();
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit);
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.getEquityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage brokerage = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate);

		final ExpressionLanguageFactory factory = new ExpressionLanguageFactory();
		final Exit exitStrategy = factory.exit();
		final ExitSize exitPositionSizing = new NeverExitPosition();

		final Entry entryStrategy = factory
		        .entry(new TradingStrategyPeriodic(startDate, purchaseFrequency, SignalType.BULLISH));

		final EntrySize entryPositionSizing = new LargestPossibleEntryPosition(
		        new AbsoluteEntryPositionBounds(MinimumTrade.ZERO.getValue()),
		        new RelativeEntryPositionBounds(MaximumTrade.ALL.getValue()));

		final Strategy tradingStrategy = factory.strategy(entryStrategy, entryPositionSizing, exitStrategy,
		        exitPositionSizing, EquityClass.STOCK, EQUITY_SCALE);

		return new BacktestBootstrapContext(tradingStrategy, brokerage, cashAccount, simulationDates);
	}

	private BacktestBootstrapContext confirmationSignal( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EntryLogicConfiguration entry ) {

		final Optional<ConfirmationSignalFilterConfiguration> confirmationSignal = entry.getConfirmationSignal();

		if (!confirmationSignal.isPresent()) {
			throw new IllegalArgumentException("Cannot create a signal confirmation with a confirmation signal");
		}

		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final IndicatorConfiguration anchor = confirmationSignal.get().getAnchor();
		final IndicatorConfiguration confirmation = confirmationSignal.get().getConfirmation();
		final ConfirmationSignalFilterConfiguration.Type confirmBy = confirmationSignal.get().getType();
		final SignalRangeFilter signalRangeFilter = getSignalRangeFilter(entry);

		final ExpressionLanguageFactory entryFactory = new ExpressionLanguageFactory();
		final IndicatorGeneratorFactory indicatorFactory = new IndicatorGeneratorFactory();
		final Indicator anchorIndicator = indicatorFactory.create(anchor, signalRangeFilter, signalAnalysisListener);
		final Indicator confirmationIndicator = indicatorFactory.create(confirmation, signalRangeFilter,
		        signalAnalysisListener);
		//
		//		final Entry entryStrategy = entryFactory.entry(anchorIndicator,
		//		        new TradingStrategyConfirmedBy(confirmBy.getDelayUntilConfirmationRange(),
		//		                confirmBy.getConfirmationDayRange()),
		//		        confirmationIndicator);

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, null);
	}

	//TODO rename - something todo with same day and AND
	private BacktestBootstrapContext indicatorsOnSameDay( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EntryLogicConfiguration entry ) {

		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final IndicatorConfiguration[] indicators = getSameDaySignals(entry);
		final SignalRangeFilter signalRangeFilter = getSignalRangeFilter(entry);
		final ExpressionLanguageFactory entryFactory = new ExpressionLanguageFactory();
		final IndicatorGeneratorFactory indicatorFactory = new IndicatorGeneratorFactory();

		Entry entryStrategy = entryFactory
		        .entry(indicatorFactory.create(indicators[0], signalRangeFilter, signalAnalysisListener));

		for (int i = 1; i < indicators.length; i++) {
			entryStrategy = entryFactory.entry(entryStrategy, new TradingStrategyAndOperator(), entryFactory
			        .entry(indicatorFactory.create(indicators[i], signalRangeFilter, signalAnalysisListener)));
		}

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, entryStrategy);
	}

	//TODO rename - something todo with same day and OR
	private BacktestBootstrapContext anyIndicators( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
	        final BrokerageFeesConfiguration brokerageType, final EntryLogicConfiguration entry ) {

		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final IndicatorConfiguration[] indicators = getAnyOfSignals(entry);
		final SignalRangeFilter signalRangeFilter = getSignalRangeFilter(entry);
		final ExpressionLanguageFactory entryFactory = new ExpressionLanguageFactory();
		final IndicatorGeneratorFactory indicatorFactory = new IndicatorGeneratorFactory();

		Entry entryStrategy = entryFactory
		        .entry(indicatorFactory.create(indicators[0], signalRangeFilter, signalAnalysisListener));

		for (int i = 1; i < indicators.length; i++) {
			entryStrategy = entryFactory.entry(entryStrategy, new TradingStrategyOrOperator(), entryFactory
			        .entry(indicatorFactory.create(indicators[i], signalRangeFilter, signalAnalysisListener)));
		}

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, entryStrategy);
	}

	private IndicatorConfiguration[] getAnyOfSignals( final EntryLogicConfiguration entry ) {
		final Optional<AnyOfIndicatorFilterConfiguration> signals = entry.getAnyOfSignal();
		return signals.isPresent() ? signals.get().getSignals() : new IndicatorConfiguration[0];
	}

	private IndicatorConfiguration[] getSameDaySignals( final EntryLogicConfiguration entry ) {
		final Optional<SameDayFilterConfiguration> signals = entry.getSameDaySignals();
		return signals.isPresent() ? signals.get().getSignals() : new IndicatorConfiguration[0];
	}

	private SignalRangeFilter getSignalRangeFilter( final EntryLogicConfiguration entry ) {
		final LocalDate earliestSignal = simulationDates.getStartDate();
		final LocalDate latestSignal = simulationDates.getEndDate();

		final Optional<ConfirmationSignalFilterConfiguration> confirmationSignal = entry.getConfirmationSignal();

		if (confirmationSignal.isPresent()) {
			return new SimulationDatesRangeFilterDecorator(earliestSignal, latestSignal,
			        new TradingDaySignalRangeFilter(confirmationSignal.get().getType().getDelayUntilConfirmationRange()
			                + confirmationSignal.get().getType().getConfirmationDayRange()));
		}

		return new SimulationDatesRangeFilterDecorator(earliestSignal, latestSignal,
		        new TradingDaySignalRangeFilter(0));
	}

	private LocalDate getFirstDayOfYear( final LocalDate date ) {
		return LocalDate.of(date.getYear(), 1, 1);
	}

	private BacktestBootstrapContext getIndicatorConfiguration( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EquityManagementFeeCalculator feeCalculator, final Entry entryStrategy ) {

		final LocalDate startDate = simulationDates.getStartDate();
		final ExpressionLanguageFactory factory = new ExpressionLanguageFactory();
		final Exit exitStrategy = factory.exit();
		final ExitSize exitPositionSizing = new NeverExitPosition();

		final EntrySize entryPositionSizing = new LargestPossibleEntryPosition(
		        new AbsoluteEntryPositionBounds(minimumTrade.getValue()),
		        new RelativeEntryPositionBounds(maximumTrade.getValue()));

		final Strategy tradingStrategy = factory.strategy(entryStrategy, entryPositionSizing, exitStrategy,
		        exitPositionSizing, EquityClass.STOCK, EQUITY_SCALE);

		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.getEquityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage cmcMarkets = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate);
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit);

		return new BacktestBootstrapContext(tradingStrategy, cmcMarkets, cashAccount, simulationDates);
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