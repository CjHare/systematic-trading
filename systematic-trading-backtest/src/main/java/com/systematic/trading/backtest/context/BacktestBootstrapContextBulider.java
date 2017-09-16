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

import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFactory;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityManagementFeeConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityWithFeeConfiguration;
import com.systematic.trading.backtest.configuration.signals.IndicatorSignalGeneratorFactory;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.filter.TradingDaySignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.ConfirmationIndicatorsSignalFilter;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.LadderedEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.PeriodicEquityManagementFeeStructure;
import com.systematic.trading.simulation.equity.fee.management.ZeroEquityManagementFeeCalculator;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.logic.HoldForeverExitLogic;
import com.systematic.trading.simulation.logic.trade.AbsoluteTradeValueCalculator;
import com.systematic.trading.simulation.logic.trade.BoundedTradeValue;
import com.systematic.trading.simulation.logic.trade.RelativeTradeValueCalculator;

/**
 * Creates the Bootstrap configurations for back testing.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapContextBulider {

	//TODO convert into an actual builder pattern

	/** How long one year is as a period of time/ */
	private static final Period ONE_YEAR = Period.ofYears(1);

	/** Accuracy for BigDecimal operations. */
	private final MathContext mathContext;

	/** Single equity to create the configuration on. */
	private EquityConfiguration equity;

	/** Weekly deposit amount into the cash account. */
	private DepositConfiguration deposit;

	/** First date to apply the management fee on. */
	private LocalDate managementFeeStartDate;

	/** The intended dates for the simulation. */
	private BacktestSimulationDates simulationDates;

	private EntryLogicConfiguration entryLogic;

	private BrokerageFeesConfiguration brokerageType;

	public BacktestBootstrapContextBulider( final MathContext mathContext ) {
		this.mathContext = mathContext;
	}

	public BacktestBootstrapContextBulider withConfiguration( final BacktestBootstrapConfiguration configuration ) {
		this.simulationDates = configuration.getBacktestDates();
		this.managementFeeStartDate = getFirstDayOfYear(simulationDates.getStartDate());
		this.simulationDates = configuration.getBacktestDates();
		this.deposit = configuration.getDeposit();
		this.equity = configuration.getEquity();
		this.brokerageType = configuration.getBrokerageFees();
		this.entryLogic = configuration.getEntryLogic();
		return this;
	}

	public BacktestBootstrapContext build() {

		switch (entryLogic.getType()) {
			case PERIODIC:
				final Period purchaseFrequency = entryLogic.getPeriodic().getFrequency();
				return periodic(brokerageType, purchaseFrequency);

			case CONFIRMATION_SIGNAL:
				return confirmationSignal(entryLogic.getMinimumTrade(), entryLogic.getMaximumTrade(), brokerageType,
				        entryLogic);

			case SAME_DAY_SIGNALS:
				return indicatorsOnSameDay(entryLogic.getMinimumTrade(), entryLogic.getMaximumTrade(), brokerageType,
				        entryLogic);

			default:
				throw new IllegalArgumentException(
				        String.format("Unexpected entry logic type: %s", entryLogic.getType()));
		}
	}

	private BacktestBootstrapContext periodic( final BrokerageFeesConfiguration brokerageType,
	        final Period purchaseFrequency ) {
		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final LocalDate startDate = simulationDates.getStartDate();
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit, mathContext);
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.getEquityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage brokerage = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate, mathContext);
		final EntryLogic entry = EntryLogicFactory.getInstance().create(equity.getEquityIdentity(), startDate,
		        purchaseFrequency, mathContext);

		return new BacktestBootstrapContext(entry, getExitLogic(), brokerage, cashAccount, simulationDates);
	}

	private BacktestBootstrapContext confirmationSignal( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EntryLogicConfiguration entry ) {

		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final SignalConfiguration anchor = entry.getConfirmationSignal().getAnchor();
		final SignalConfiguration confirmation = entry.getConfirmationSignal().getConfirmation();

		final SignalFilter filter = new ConfirmationIndicatorsSignalFilter(anchor.getType(), confirmation.getType(),
		        entry.getConfirmationSignal().getType().getDelayUntilConfirmationRange(),
		        entry.getConfirmationSignal().getType().getConfirmationDayRange());

		final SignalRangeFilter signalRangeFilter = new TradingDaySignalRangeFilter(
		        entry.getConfirmationSignal().getType().getDelayUntilConfirmationRange()
		                + entry.getConfirmationSignal().getType().getConfirmationDayRange());

		final IndicatorSignalGenerator[] indicatorGenerators = {
		        IndicatorSignalGeneratorFactory.getInstance().create(anchor, signalRangeFilter, mathContext),
		        IndicatorSignalGeneratorFactory.getInstance().create(confirmation, signalRangeFilter, mathContext) };

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, filter,
		        indicatorGenerators);
	}

	private BacktestBootstrapContext indicatorsOnSameDay( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EntryLogicConfiguration entry ) {

		final EquityManagementFeeCalculator feeCalculator = createFeeCalculator(equity.getManagementFee());
		final SignalConfiguration[] indicators = entry.getSameDaySignals().getSignals();
		final IndicatorSignalType[] indicatorTypes = new IndicatorSignalType[indicators.length];
		for (int i = 0; i < indicators.length; i++) {
			indicatorTypes[i] = indicators[i].getType();
		}

		final SignalFilter filter = new IndicatorsOnSameDaySignalFilter(indicatorTypes);
		final IndicatorSignalGenerator[] indicatorGenerators = new IndicatorSignalGenerator[indicators.length];

		final SignalRangeFilter signalRangeFilter = new TradingDaySignalRangeFilter(
		        entry.getConfirmationSignal().getType().getDelayUntilConfirmationRange()
		                + entry.getConfirmationSignal().getType().getConfirmationDayRange());

		for (int i = 0; i < indicatorGenerators.length; i++) {
			indicatorGenerators[i] = IndicatorSignalGeneratorFactory.getInstance().create(indicators[i],
			        signalRangeFilter, mathContext);
		}

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, filter,
		        indicatorGenerators);
	}

	private LocalDate getFirstDayOfYear( final LocalDate date ) {
		return LocalDate.of(date.getYear(), 1, 1);
	}

	private ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}

	private BacktestBootstrapContext getIndicatorConfiguration( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EquityManagementFeeCalculator feeCalculator, final SignalFilter filter,
	        final IndicatorSignalGenerator... indicators ) {

		final LocalDate startDate = simulationDates.getStartDate();
		final BoundedTradeValue tradeValue = new BoundedTradeValue(
		        new AbsoluteTradeValueCalculator(minimumTrade.getValue()),
		        new RelativeTradeValueCalculator(maximumTrade.getValue(), mathContext));

		final EntryLogic entry = EntryLogicFactory.getInstance().create(equity.getEquityIdentity(), tradeValue,
		        simulationDates, filter, mathContext, indicators);
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(
		        equity.getEquityIdentity(),
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage cmcMarkets = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate, mathContext);
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit, mathContext);

		return new BacktestBootstrapContext(entry, getExitLogic(), cmcMarkets, cashAccount, simulationDates);
	}

	private EquityManagementFeeCalculator createFeeCalculator( final EquityManagementFeeConfiguration managementFee ) {
		switch (managementFee) {
			case VANGUARD_MSCI_INT_RETAIL:
				return new LadderedEquityManagementFeeCalculator(managementFee.getFeeRange(),
				        managementFee.getPercentageFee(), mathContext);

			case VGS:
				return new FlatEquityManagementFeeCalculator(managementFee.getPercentageFee()[0], mathContext);

			case NONE:
			default:
				return new ZeroEquityManagementFeeCalculator();
		}
	}
}