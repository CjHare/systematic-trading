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
package com.systematic.trading.backtest.display.file;

import java.io.File;
import java.io.IOException;
import java.math.MathContext;
import java.time.Period;
import java.util.concurrent.ExecutorService;

import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Single entry point to output a simulation run into files, displaying all events.
 * 
 * @author CJ Hare
 */
public class FileDisplay implements BacktestDisplay {

	private final MathContext mathContext;

	// TODO aggregate displays into arrays
	private final String baseDirectory;
	private ReturnOnInvestmentEventListener roiDisplay;
	private ReturnOnInvestmentEventListener roiDailyDisplay;
	private ReturnOnInvestmentEventListener roiMonthlyDisplay;
	private ReturnOnInvestmentEventListener roiYearlyDisplay;
	private FileEventDisplay eventDisplay;
	private CashEventListener cashEventDisplay;
	private BrokerageEventListener brokerageEventDisplay;
	private OrderEventListener ordertEventDisplay;
	private SignalAnalysisListener signalAnalysisDisplay;
	private EventStatisticsDisplay statisticsDisplay;
	private NetWorthSummaryDisplay netWorthDisplay;
	private NetWorthEventListener netWorthComparisonDisplay;
	private EquityEventListener equityEventDisplay;
	private final ExecutorService pool;

	public FileDisplay(final String outputDirectory, final ExecutorService pool, final MathContext mathContext)
	        throws IOException {

		// Ensure the directory exists
		final File outputDirectoryFile = new File(outputDirectory);
		if (!outputDirectoryFile.exists()) {
			if (!outputDirectoryFile.mkdirs()) {
				throw new IllegalArgumentException(
				        String.format("Failed to create / access directory: %s", outputDirectory));
			}
		}

		this.baseDirectory = outputDirectoryFile.getCanonicalPath();
		this.mathContext = mathContext;
		this.pool = pool;
	}

	@Override
	public void init( final TickerSymbolTradingData tradingData, final BacktestSimulationDates dates,
	        final EventStatistics eventStatistics, final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi,
	        final TradingDayPrices lastTradingDay, final Period duration ) throws BacktestInitialisationException {

		final FileDisplayMultithreading returnOnInvestmentFile = getFileDisplay("/return-on-investment.txt");
		this.roiDisplay = new FileReturnOnInvestmentDisplay(
		        FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.ALL, returnOnInvestmentFile);

		final FileDisplayMultithreading returnOnInvestmentDailyFilen = getFileDisplay(
		        "/return-on-investment-daily.txt");
		this.roiDailyDisplay = new FileReturnOnInvestmentDisplay(
		        FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.DAILY, returnOnInvestmentDailyFilen);

		final FileDisplayMultithreading returnOnInvestmentMonthlyFile = getFileDisplay(
		        "/return-on-investment-monthly.txt");
		this.roiMonthlyDisplay = new FileReturnOnInvestmentDisplay(
		        FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.MONTHLY, returnOnInvestmentMonthlyFile);

		final FileDisplayMultithreading returnOnInvestmentYearlyFile = getFileDisplay(
		        "/return-on-investment-yearly.txt");
		this.roiYearlyDisplay = new FileReturnOnInvestmentDisplay(
		        FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.YEARLY, returnOnInvestmentYearlyFile);

		final FileDisplayMultithreading eventFile = getFileDisplay("/events.txt");
		this.eventDisplay = new FileEventDisplay(tradingData, dates, eventFile);

		final FileDisplayMultithreading cashEventFile = getFileDisplay("/events-cash.txt");
		this.cashEventDisplay = new FileCashEventDisplay(cashEventFile);

		final FileDisplayMultithreading orderEventFile = getFileDisplay("/events-order.txt");
		this.ordertEventDisplay = new FileOrderEventDisplay(orderEventFile);

		final FileDisplayMultithreading brokerageEventFile = getFileDisplay("/events-brokerage.txt");
		this.brokerageEventDisplay = new FileBrokerageEventDisplay(brokerageEventFile);

		final FileDisplayMultithreading equityEventFile = getFileDisplay("/events-equity.txt");
		this.equityEventDisplay = new FileEquityEventDisplay(equityEventFile);

		final FileDisplayMultithreading statisticsFile = getFileDisplay("/statistics.txt");
		this.statisticsDisplay = new FileEventStatisticsDisplay(eventStatistics, statisticsFile);
		this.netWorthDisplay = new FileNetWorthSummaryDisplay(cumulativeRoi, statisticsFile);

		final FileDisplayMultithreading signalAnalysisFile = getFileDisplay("/signals.txt");
		this.signalAnalysisDisplay = new FileSignalAnalysisDisplay(signalAnalysisFile);

		final FileDisplayMultithreading comparisonFile = getFileDisplay("/../summary.txt");
		netWorthComparisonDisplay = new FileComparisonDisplay(duration, eventStatistics, comparisonFile, mathContext);
	}

	private FileDisplayMultithreading getFileDisplay( final String suffix ) {
		return new FileDisplayMultithreading(baseDirectory + suffix, pool);
	}

	@Override
	public void event( final CashEvent event ) {
		eventDisplay.event(event);
		cashEventDisplay.event(event);
	}

	@Override
	public void event( final OrderEvent event ) {
		eventDisplay.event(event);
		ordertEventDisplay.event(event);
	}

	@Override
	public void event( final BrokerageEvent event ) {
		eventDisplay.event(event);
		brokerageEventDisplay.event(event);
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		roiDisplay.event(event);
		roiDailyDisplay.event(event);
		roiMonthlyDisplay.event(event);
		roiYearlyDisplay.event(event);
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		if (SimulationState.COMPLETE.equals(transitionedState)) {
			simulationCompleted();
		}
	}

	private void simulationCompleted() {
		statisticsDisplay.displayEventStatistics();
		netWorthDisplay.displayNetWorth();
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {
		netWorthDisplay.event(event, state);
		netWorthComparisonDisplay.event(event, state);
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {
		signalAnalysisDisplay.event(event);
	}

	@Override
	public void event( final EquityEvent event ) {
		equityEventDisplay.event(event);
	}
}
