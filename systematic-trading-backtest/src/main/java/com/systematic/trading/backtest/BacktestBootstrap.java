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
package com.systematic.trading.backtest;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.Simulation;
import com.systematic.trading.simulation.SimulationStateListener;
import com.systematic.trading.simulation.analysis.networth.NetWorthSummaryEventGenerator;
import com.systematic.trading.simulation.analysis.roi.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.PeriodicCulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.statistics.CumulativeEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Bootstraps the back test.
 * <p/>
 * Deals with the wiring together of the surrounding classes for correct propagation of events.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrap {

	/** Context for BigDecimal operations. */
	private final MathContext mathContext;

	/** Display information from the back testing. */
	private final BacktestDisplay display;

	/** Configuration for the back test. */
	private final BacktestBootstrapConfiguration configuration;

	/** Unmodifiable trading data for input to the back test. */
	private final TickerSymbolTradingData tradingData;

	public BacktestBootstrap(final TickerSymbolTradingData tradingData,
	        final BacktestBootstrapConfiguration configuration, final BacktestDisplay display,
	        final MathContext mathContext) {
		this.configuration = configuration;
		this.mathContext = mathContext;
		this.tradingData = tradingData;
		this.display = display;
	}

	public void run() throws BacktestInitialisationException {

		// First data point may not be the requested start date
		final LocalDate earliestDate = tradingData.getEarliestDate();

		// Final trading day data point, may not be be requested end date
		final LocalDate latestDate = tradingData.getLatestDate();

		final TradingDayPrices lastTradingDay = tradingData.getTradingDayPrices().get(latestDate);

		// TODO stuff about the start date != earliest date, find the closest to start date maybe?

		// TODO run with a full output & check no deposits & signals

		// Cumulative recording of investment progression
		final CulmativeReturnOnInvestmentCalculator roi = new CulmativeReturnOnInvestmentCalculator(mathContext);

		final PeriodicCulmativeReturnOnInvestmentCalculator dailyRoi = new PeriodicCulmativeReturnOnInvestmentCalculator(
		        earliestDate, Period.ofDays(1), mathContext);
		roi.addListener(dailyRoi);

		final PeriodicCulmativeReturnOnInvestmentCalculator monthlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculator(
		        earliestDate, Period.ofMonths(1), mathContext);
		roi.addListener(monthlyRoi);

		final PeriodicCulmativeReturnOnInvestmentCalculator yearlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculator(
		        earliestDate, Period.ofYears(1), mathContext);
		roi.addListener(yearlyRoi);

		final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi = new CulmativeTotalReturnOnInvestmentCalculator(
		        mathContext);
		roi.addListener(cumulativeRoi);

		final EntryLogic entry = configuration.getEntryLogic();
		entry.addListener(display);

		final ExitLogic exit = configuration.getExitLogic();

		final Brokerage broker = configuration.getBroker();

		final CashAccount cashAccount = configuration.getCashAccount();
		cashAccount.addListener(roi);

		// Engine dealing with the event flow
		final Simulation simulation = new Simulation(tradingData, broker, cashAccount, roi, entry, exit);

		// Statistics recorder for the various cash account, brokerage and order events
		final EventStatistics eventStatistics = new CumulativeEventStatistics();
		simulation.addListener(eventStatistics);
		broker.addListener((BrokerageEventListener) eventStatistics);
		broker.addListener((EquityEventListener) eventStatistics);

		cashAccount.addListener(eventStatistics);

		// Creates the net worth events
		final NetWorthSummaryEventGenerator networthSummay = new NetWorthSummaryEventGenerator(broker, lastTradingDay,
		        cashAccount, configuration.getDescription());
		simulation.addListener(networthSummay);

		// Display for simulation output
		final Period duration = Period.between(earliestDate, latestDate);
		display.init(tradingData, configuration.getSimulationDates(), eventStatistics, cumulativeRoi, lastTradingDay,
		        duration);
		simulation.addListener((OrderEventListener) display);
		simulation.addListener((SimulationStateListener) display);
		networthSummay.addListener(display);
		cashAccount.addListener(display);
		yearlyRoi.addListener(display);
		monthlyRoi.addListener(display);
		dailyRoi.addListener(display);
		broker.addListener((BrokerageEventListener) display);
		broker.addListener((EquityEventListener) display);

		// Run the simulation until completion
		simulation.run();
	}
}
