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
package com.systematic.trading.backtest;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.Simulation;
import com.systematic.trading.simulation.SimulationStateListener;
import com.systematic.trading.simulation.analysis.networth.NetWorthSummaryEventGenerator;
import com.systematic.trading.simulation.analysis.roi.CulmativeReturnOnInvestment;
import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestment;
import com.systematic.trading.simulation.analysis.roi.PeriodicCulmativeReturnOnInvestment;
import com.systematic.trading.simulation.analysis.roi.ReturnOnInvestmentListener;
import com.systematic.trading.simulation.analysis.statistics.CumulativeEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Deals with the wiring together of the surrounding classes for correct propagation of events.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrap {

	/** Display information from the back testing. */
	private final BacktestEventListener output;

	private final BacktestBootstrapContext context;

	/** Unmodifiable trading data for input to the back test. */
	private final TickerSymbolTradingData tradingData;

	public BacktestBootstrap( final BacktestBootstrapContext context, final BacktestEventListener output,
	        final TickerSymbolTradingData tradingData ) {
		this.context = context;
		this.tradingData = tradingData;
		this.output = output;
	}

	public void run() {

		// First data point may not be the requested start date
		final LocalDate earliestDate = tradingData.earliestDate();

		// Final trading day data point, may not be be requested end date
		final LocalDate latestDate = tradingData.latestDate();

		final TradingDayPrices lastTradingDay = tradingData.tradingPrices().get(latestDate);

		// Cumulative recording of investment progression
		final ReturnOnInvestmentListener roi = new CulmativeReturnOnInvestment();

		final PeriodicCulmativeReturnOnInvestment dailyRoi = new PeriodicCulmativeReturnOnInvestment(earliestDate,
		        Period.ofDays(1));
		roi.addListener(dailyRoi);

		final PeriodicCulmativeReturnOnInvestment monthlyRoi = new PeriodicCulmativeReturnOnInvestment(earliestDate,
		        Period.ofMonths(1));
		roi.addListener(monthlyRoi);

		final PeriodicCulmativeReturnOnInvestment yearlyRoi = new PeriodicCulmativeReturnOnInvestment(earliestDate,
		        Period.ofYears(1));
		roi.addListener(yearlyRoi);

		final CulmativeTotalReturnOnInvestment cumulativeRoi = new CulmativeTotalReturnOnInvestment();
		roi.addListener(cumulativeRoi);

		final Brokerage broker = context.broker();

		final CashAccount cashAccount = context.cashAccount();
		cashAccount.addListener(roi);

		// Engine dealing with the event flow
		final Simulation simulation = new Simulation(tradingData, broker, cashAccount, roi,
		        context.tradingStrategy());

		// Statistics recorder for the various cash account, brokerage and order events
		final EventStatistics eventStatistics = new CumulativeEventStatistics();
		simulation.addListener(eventStatistics);
		broker.addListener((BrokerageEventListener) eventStatistics);
		broker.addListener((EquityEventListener) eventStatistics);

		cashAccount.addListener(eventStatistics);

		// Creates the net worth events
		final NetWorthSummaryEventGenerator networthSummay = new NetWorthSummaryEventGenerator(broker, lastTradingDay,
		        cashAccount);
		simulation.addListener(networthSummay);

		// Display for simulation output
		output.init(tradingData, context.simulationDates(), eventStatistics, cumulativeRoi, lastTradingDay);
		simulation.addListener((OrderEventListener) output);
		simulation.addListener((SimulationStateListener) output);
		networthSummay.addListener(output);
		cashAccount.addListener(output);
		yearlyRoi.addListener(output);
		monthlyRoi.addListener(output);
		dailyRoi.addListener(output);
		broker.addListener((BrokerageEventListener) output);
		broker.addListener((EquityEventListener) output);

		// Run the simulation until completion
		simulation.run();
	}
}