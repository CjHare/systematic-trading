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
package com.systematic.trading.backtest.output.file;

import java.io.IOException;
import java.math.MathContext;
import java.util.concurrent.ExecutorService;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.file.dao.BrokerageEventFileDao;
import com.systematic.trading.backtest.output.file.dao.CashEventFileDao;
import com.systematic.trading.backtest.output.file.dao.ComparisonFileDao;
import com.systematic.trading.backtest.output.file.dao.EquityEventFileDao;
import com.systematic.trading.backtest.output.file.dao.EventStatisticsDao;
import com.systematic.trading.backtest.output.file.dao.EventStatisticsFileDao;
import com.systematic.trading.backtest.output.file.dao.NetWorthSummaryDao;
import com.systematic.trading.backtest.output.file.dao.NetWorthSummaryFileDao;
import com.systematic.trading.backtest.output.file.dao.OrderEventFileDao;
import com.systematic.trading.backtest.output.file.dao.ReturnOnInvestmentFileDao;
import com.systematic.trading.backtest.output.file.dao.SignalAnalysisFileDao;
import com.systematic.trading.backtest.output.file.model.ReturnOnInvestmentPeriod;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;
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
public class CompleteFileOutputService extends FileOutput implements BacktestOutput {

	private final MathContext mathContext;

	private final String baseDirectory;
	private ReturnOnInvestmentEventListener roiDisplay;
	private ReturnOnInvestmentEventListener roiDailyDisplay;
	private ReturnOnInvestmentEventListener roiMonthlyDisplay;
	private ReturnOnInvestmentEventListener roiYearlyDisplay;
	private EventListenerOutput eventDisplay;
	private CashEventListener cashEventDisplay;
	private BrokerageEventListener brokerageEventDisplay;
	private OrderEventListener ordertEventDisplay;
	private SignalAnalysisListener signalAnalysisDisplay;
	private EventStatisticsDao statisticsDisplay;
	private NetWorthSummaryDao netWorthDisplay;
	private NetWorthEventListener netWorthComparisonDisplay;
	private EquityEventListener equityEventDisplay;
	private final ExecutorService pool;

	private final BacktestBatchId batchId;

	public CompleteFileOutputService( final BacktestBatchId batchId, final String outputDirectory,
	        final ExecutorService pool, final MathContext mathContext ) throws IOException {
		this.baseDirectory = getVerifiedDirectory(outputDirectory);
		this.mathContext = mathContext;
		this.pool = pool;
		this.batchId = batchId;
	}

	@Override
	public void init( final TickerSymbolTradingData tradingData, final BacktestSimulationDates dates,
	        final EventStatistics eventStatistics, final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi,
	        final TradingDayPrices lastTradingDay ) {

		final FileMultithreading returnOnInvestmentFile = getFileDisplay("/return-on-investment.txt");
		this.roiDisplay = new ReturnOnInvestmentFileDao(ReturnOnInvestmentPeriod.ALL, returnOnInvestmentFile);

		final FileMultithreading returnOnInvestmentDailyFilen = getFileDisplay("/return-on-investment-daily.txt");
		this.roiDailyDisplay = new ReturnOnInvestmentFileDao(ReturnOnInvestmentPeriod.DAILY,
		        returnOnInvestmentDailyFilen);

		final FileMultithreading returnOnInvestmentMonthlyFile = getFileDisplay("/return-on-investment-monthly.txt");
		this.roiMonthlyDisplay = new ReturnOnInvestmentFileDao(ReturnOnInvestmentPeriod.MONTHLY,
		        returnOnInvestmentMonthlyFile);

		final FileMultithreading returnOnInvestmentYearlyFile = getFileDisplay("/return-on-investment-yearly.txt");
		this.roiYearlyDisplay = new ReturnOnInvestmentFileDao(ReturnOnInvestmentPeriod.YEARLY,
		        returnOnInvestmentYearlyFile);

		final FileMultithreading eventFile = getFileDisplay("/events.txt");
		this.eventDisplay = new EventListenerOutput(tradingData, dates, eventFile);

		final FileMultithreading cashEventFile = getFileDisplay("/events-cash.txt");
		this.cashEventDisplay = new CashEventFileDao(cashEventFile);

		final FileMultithreading orderEventFile = getFileDisplay("/events-order.txt");
		this.ordertEventDisplay = new OrderEventFileDao(orderEventFile);

		final FileMultithreading brokerageEventFile = getFileDisplay("/events-brokerage.txt");
		this.brokerageEventDisplay = new BrokerageEventFileDao(brokerageEventFile);

		final FileMultithreading equityEventFile = getFileDisplay("/events-equity.txt");
		this.equityEventDisplay = new EquityEventFileDao(equityEventFile);

		final FileMultithreading statisticsFile = getFileDisplay("/statistics.txt");
		this.statisticsDisplay = new EventStatisticsFileDao(eventStatistics, statisticsFile);
		this.netWorthDisplay = new NetWorthSummaryFileDao(cumulativeRoi, statisticsFile);

		final FileMultithreading signalAnalysisFile = getFileDisplay("/signals.txt");
		this.signalAnalysisDisplay = new SignalAnalysisFileDao(signalAnalysisFile);

		final FileMultithreading comparisonFile = getFileDisplay("/../summary.csv");
		netWorthComparisonDisplay = new ComparisonFileDao(batchId, dates, eventStatistics, comparisonFile, mathContext);
	}

	private FileMultithreading getFileDisplay( final String suffix ) {
		return new FileMultithreading(baseDirectory + suffix, pool);
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
	public void event( final SignalAnalysisEvent event ) {
		signalAnalysisDisplay.event(event);
	}

	@Override
	public void event( final EquityEvent event ) {
		equityEventDisplay.event(event);
	}

	@Override
	protected EventStatisticsDao getEventStatisticsDao() {
		return statisticsDisplay;
	}

	@Override
	protected NetWorthSummaryDao getNetWorthSummaryDao() {
		return netWorthDisplay;
	}

	@Override
	protected NetWorthEventListener getNetWorthEventListener() {
		return netWorthComparisonDisplay;
	}
}