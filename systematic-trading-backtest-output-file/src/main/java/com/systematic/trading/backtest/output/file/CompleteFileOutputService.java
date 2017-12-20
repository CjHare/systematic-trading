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
import java.util.concurrent.ExecutorService;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.backtest.output.file.dao.BrokerageEventDao;
import com.systematic.trading.backtest.output.file.dao.CashEventDao;
import com.systematic.trading.backtest.output.file.dao.EquityEventDao;
import com.systematic.trading.backtest.output.file.dao.ReturnOnInvestmentDao;
import com.systematic.trading.backtest.output.file.dao.SignalAnalysisDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileBrokerageEventDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileCashEventDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileEquityEventDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileEventStatisticsDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileNetWorthSummaryDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileNetworthComparisonDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileOrderEventFileDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileReturnOnInvestmentDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileSignalAnalysisDao;
import com.systematic.trading.backtest.output.file.model.ReturnOnInvestmentPeriod;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signal.event.SignalAnalysisEvent;
import com.systematic.trading.simulation.analysis.roi.CumulativeReturnOnInvestment;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Single entry point to output a simulation run into files, displaying all events.
 * 
 * @author CJ Hare
 */
public class CompleteFileOutputService extends FileOutput implements BacktestEventListener {

	private final BacktestBatchId batchId;

	private ReturnOnInvestmentDao roiDisplay;
	private ReturnOnInvestmentDao roiDailyDisplay;
	private ReturnOnInvestmentDao roiMonthlyDisplay;
	private ReturnOnInvestmentDao roiYearlyDisplay;
	private EventListenerOutput eventDisplay;
	private CashEventDao cashEventDisplay;
	private BrokerageEventDao brokerageEventDisplay;
	private OrderEventListener ordertEventDisplay;
	private SignalAnalysisDao signalAnalysisDisplay;
	private EquityEventDao equityEventDisplay;

	public CompleteFileOutputService( final BacktestBatchId batchId, final String outputDirectory,
	        final ExecutorService pool ) throws IOException {
		super(outputDirectory, pool);
		this.batchId = batchId;
	}

	@Override
	public void init( final TickerSymbolTradingData tradingData, final BacktestSimulationDates dates,
	        final EventStatistics eventStatistics, final CumulativeReturnOnInvestment cumulativeRoi,
	        final TradingDayPrices lastTradingDay ) {

		final FileMultithreading returnOnInvestmentFile = fileDisplay("/return-on-investment.txt");
		this.roiDisplay = new FileReturnOnInvestmentDao(ReturnOnInvestmentPeriod.ALL, returnOnInvestmentFile);

		final FileMultithreading returnOnInvestmentDailyFilen = fileDisplay("/return-on-investment-daily.txt");
		this.roiDailyDisplay = new FileReturnOnInvestmentDao(ReturnOnInvestmentPeriod.DAILY,
		        returnOnInvestmentDailyFilen);

		final FileMultithreading returnOnInvestmentMonthlyFile = fileDisplay("/return-on-investment-monthly.txt");
		this.roiMonthlyDisplay = new FileReturnOnInvestmentDao(ReturnOnInvestmentPeriod.MONTHLY,
		        returnOnInvestmentMonthlyFile);

		final FileMultithreading returnOnInvestmentYearlyFile = fileDisplay("/return-on-investment-yearly.txt");
		this.roiYearlyDisplay = new FileReturnOnInvestmentDao(ReturnOnInvestmentPeriod.YEARLY,
		        returnOnInvestmentYearlyFile);

		final FileMultithreading eventFile = fileDisplay("/events.txt");
		this.eventDisplay = new EventListenerOutput(tradingData, dates, eventFile);

		final FileMultithreading cashEventFile = fileDisplay("/events-cash.txt");
		this.cashEventDisplay = new FileCashEventDao(cashEventFile);

		final FileMultithreading orderEventFile = fileDisplay("/events-order.txt");
		this.ordertEventDisplay = new FileOrderEventFileDao(orderEventFile);

		final FileMultithreading brokerageEventFile = fileDisplay("/events-brokerage.txt");
		this.brokerageEventDisplay = new FileBrokerageEventDao(brokerageEventFile);

		final FileMultithreading equityEventFile = fileDisplay("/events-equity.txt");
		this.equityEventDisplay = new FileEquityEventDao(equityEventFile);

		final FileMultithreading statisticsFile = fileDisplay("/statistics.txt");
		eventStatisticsDao(new FileEventStatisticsDao(eventStatistics, statisticsFile));
		netWorthSummaryDao(new FileNetWorthSummaryDao(cumulativeRoi, statisticsFile));

		final FileMultithreading signalAnalysisFile = fileDisplay("/signals.txt");
		this.signalAnalysisDisplay = new FileSignalAnalysisDao(signalAnalysisFile);

		final FileMultithreading comparisonFile = fileDisplay("/../summary.csv");
		netWorthEventListener(new FileNetworthComparisonDao(batchId, dates, eventStatistics, comparisonFile));
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
}