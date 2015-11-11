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

import com.systematic.trading.backtest.analysis.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEvent;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.brokerage.BrokerageEvent;
import com.systematic.trading.event.cash.CashEvent;
import com.systematic.trading.event.data.TickerSymbolTradingRange;
import com.systematic.trading.event.order.OrderEvent;

/**
 * Single entry point to output a simulation run into files.
 * 
 * @author CJ Hare
 */
public class FileDisplay implements BacktestDisplay {

	private final String parentDirectory;
	private ReturnOnInvestmentEventListener roiDisplay;
	private FileEventDisplay cashDisplay;
	private FileEventDisplay brokerageDisplay;
	private FileEventDisplay ordertDisplay;
	private EventStatisticsDisplay statisticsDisplay;
	private NetWorthSummaryDisplay netWorthDisplay;

	public FileDisplay( final String outputDirectory ) throws IOException {

		// Ensure the directory exists
		final File outputDirectoryFile = new File( outputDirectory );
		if (!outputDirectoryFile.exists()) {
			if (!outputDirectoryFile.mkdirs()) {
				throw new IllegalArgumentException( String.format( "Failed to create / access directory: %s",
						outputDirectory ) );
			}
		}

		// Ensure the directory is empty
		for (final File file : outputDirectoryFile.listFiles()) {
			file.delete();
		}

		parentDirectory = outputDirectoryFile.getCanonicalPath();
	}

	@Override
	public void init( final TickerSymbolTradingRange tickerSymbolTradingRange, final EventStatistics eventStatistics,
			final Brokerage broker, final CashAccount cashAccount,
			final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, final TradingDayPrices lastTradingDay )
			throws Exception {

		final String statisticsFilename = parentDirectory + "/statistics.txt";
		final String returnOnInvestmentFilename = parentDirectory + "/return-on-investment.txt";
		final String eventFilename = parentDirectory + "/events.txt";

		this.roiDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentFilename );
		final FileEventDisplay displays = new FileEventDisplay( eventFilename, tickerSymbolTradingRange );
		this.cashDisplay = displays;
		this.ordertDisplay = displays;
		this.brokerageDisplay = displays;
		this.statisticsDisplay = new FileEventStatisticsDisplay( eventStatistics, statisticsFilename );
		this.netWorthDisplay = new FileNetWorthSummaryDisplay( broker, lastTradingDay, cashAccount, cumulativeRoi,
				statisticsFilename );
	}

	public void simulationCompleted() {
		statisticsDisplay.displayEventStatistics();
		netWorthDisplay.displayNetWorth();
	}

	@Override
	public void event( final CashEvent event ) {
		cashDisplay.event( event );
	}

	@Override
	public void event( final OrderEvent event ) {
		ordertDisplay.event( event );
	}

	@Override
	public void event( final BrokerageEvent event ) {
		brokerageDisplay.event( event );
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		roiDisplay.event( event );
	}
}
