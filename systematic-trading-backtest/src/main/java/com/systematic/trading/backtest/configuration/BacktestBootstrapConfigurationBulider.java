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
package com.systematic.trading.backtest.configuration;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFactory;
import com.systematic.trading.backtest.configuration.equity.EquityWithFeeConfiguration;
import com.systematic.trading.backtest.configuration.signals.IndicatorSignalGeneratorFactory;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.display.DescriptionGenerator;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.PeriodicEquityManagementFeeStructure;
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
public class BacktestBootstrapConfigurationBulider {

	/** How long one year is as a period of time/ */
	private static final Period ONE_YEAR = Period.ofYears(1);

	/** Accuracy for BigDecimal operations. */
	private final MathContext mathContext;

	/** Single equity to create the configuration on. */
	private final EquityIdentity equityIdentity;

	/** Weekly deposit amount into the cash account. */
	private final DepositConfiguration deposit;

	/** Generator for the display description. */
	private final DescriptionGenerator descriptions;

	/** First date to apply the management fee on. */
	private final LocalDate managementFeeStartDate;

	/** The intended dates for the simulation. */
	private final BacktestSimulationDates simulationDates;

	public BacktestBootstrapConfigurationBulider(final EquityIdentity equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final DescriptionGenerator descriptions, final MathContext mathContext) {
		this.managementFeeStartDate = getFirstDayOfYear(simulationDates.getSimulationStartDate());
		this.simulationDates = simulationDates;
		this.descriptions = descriptions;
		this.mathContext = mathContext;
		this.deposit = deposit;
		this.equityIdentity = equity;

	}

	private LocalDate getFirstDayOfYear( final LocalDate date ) {
		return LocalDate.of(date.getYear(), 1, 1);
	}

	private ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}

	public BacktestBootstrapConfiguration getPeriodicConfiguration( final BrokerageFeesConfiguration brokerageType,
	        final Period purchaseFrequency, final EquityManagementFeeCalculator feeCalculator ) {

		final LocalDate startDate = simulationDates.getSimulationStartDate();
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit, mathContext);
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(equityIdentity,
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage brokerage = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate, mathContext);
		final EntryLogic entryLogic = EntryLogicFactory.getInstance().create(equityIdentity, startDate,
		        purchaseFrequency, mathContext);
		final String description = descriptions.getDescription(brokerageType, purchaseFrequency);

		return new BacktestBootstrapConfiguration(entryLogic, getExitLogic(), brokerage, cashAccount, simulationDates,
		        description);
	}

	public BacktestBootstrapConfiguration getIndicatorConfiguration( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final EquityManagementFeeCalculator feeCalculator,
	        final BrokerageFeesConfiguration brokerageType, final SignalFilter filter,
	        final SignalConfiguration... indicators ) {

		final IndicatorSignalGenerator[] entrySignals = new IndicatorSignalGenerator[indicators.length];

		for (int i = 0; i < entrySignals.length; i++) {
			entrySignals[i] = IndicatorSignalGeneratorFactory.getInstance().create(indicators[i], mathContext);
		}

		final String description = descriptions.getDescription(filter, indicators);

		return getIndicatorConfiguration(minimumTrade, maximumTrade, brokerageType, feeCalculator, filter, description,
		        entrySignals);
	}

	private BacktestBootstrapConfiguration getIndicatorConfiguration( final MinimumTrade minimumTrade,
	        final MaximumTrade maximumTrade, final BrokerageFeesConfiguration brokerageType,
	        final EquityManagementFeeCalculator feeCalculator, final SignalFilter filter, final String description,
	        final IndicatorSignalGenerator... entrySignals ) {

		final LocalDate startDate = simulationDates.getSimulationStartDate();
		final BoundedTradeValue tradeValue = new BoundedTradeValue(
		        new AbsoluteTradeValueCalculator(minimumTrade.getValue()),
		        new RelativeTradeValueCalculator(maximumTrade.getValue(), mathContext));

		final EntryLogic entryLogic = EntryLogicFactory.getInstance().create(equityIdentity, tradeValue,
		        simulationDates, filter, mathContext, entrySignals);
		final EquityWithFeeConfiguration equityConfiguration = new EquityWithFeeConfiguration(equityIdentity,
		        new PeriodicEquityManagementFeeStructure(managementFeeStartDate, feeCalculator, ONE_YEAR));
		final Brokerage cmcMarkets = BrokerageFactoroy.getInstance().create(equityConfiguration, brokerageType,
		        startDate, mathContext);
		final CashAccount cashAccount = CashAccountFactory.getInstance().create(startDate, deposit, mathContext);

		return new BacktestBootstrapConfiguration(entryLogic, getExitLogic(), cmcMarkets, cashAccount, simulationDates,
		        description);
	}
}