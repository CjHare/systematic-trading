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
import com.systematic.trading.backtest.output.file.dao.impl.FileEventStatisticsDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileNetWorthSummaryDao;
import com.systematic.trading.backtest.output.file.dao.impl.FileNetworthComparisonDao;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.simulation.analysis.roi.CumulativeReturnOnInvestment;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.strategy.signal.SignalAnalysisEvent;

/**
 * Single entry point to output a simulation run into files, displaying only the summary and
 * comparisons.
 * <p/>
 * Substantially reduces the number of logged events, hence side stepping the IO bottleneck.
 * 
 * @author CJ Hare
 */
public class MinimalFileOutputService extends FileOutput {

	private final BacktestBatchId batchId;

	public MinimalFileOutputService( final BacktestBatchId batchId, final String outputDirectory,
	        final ExecutorService pool ) throws IOException {
		super(outputDirectory, pool);
		this.batchId = batchId;
	}

	@Override
	public void init( final TickerSymbolTradingData tradingData, final BacktestSimulationDates dates,
	        final EventStatistics eventStatistics, final CumulativeReturnOnInvestment cumulativeRoi,
	        final TradingDayPrices lastTradingDay ) {

		final FileMultithreading statisticsFile = fileDisplay("/statistics.txt");
		eventStatisticsDao(new FileEventStatisticsDao(eventStatistics, statisticsFile));
		netWorthSummaryDao(new FileNetWorthSummaryDao(cumulativeRoi, statisticsFile));

		final FileMultithreading comparisonFile = fileDisplay("/../summary.txt");
		netWorthEventListener(new FileNetworthComparisonDao(batchId, dates, eventStatistics, comparisonFile));
	}

	@Override
	public void event( final CashEvent event ) {

		// Recording of this event is not required for minimal display
	}

	@Override
	public void event( final OrderEvent event ) {

		// Recording of this event is not required for minimal display
	}

	@Override
	public void event( final BrokerageEvent event ) {

		// Recording of this event is not required for minimal display
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {

		// Recording of this event is not required for minimal display
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {

		// Recording of this event is not required for minimal display
	}

	@Override
	public void event( EquityEvent event ) {

		// Recording of this event is not required for minimal display
	}
}