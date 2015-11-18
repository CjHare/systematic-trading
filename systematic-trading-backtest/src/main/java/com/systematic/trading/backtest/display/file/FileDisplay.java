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

import com.systematic.trading.backtest.analysis.networth.NetWorthEvent;
import com.systematic.trading.backtest.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEvent;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.brokerage.BrokerageEvent;
import com.systematic.trading.event.brokerage.BrokerageEventListener;
import com.systematic.trading.event.cash.CashEvent;
import com.systematic.trading.event.cash.CashEventListener;
import com.systematic.trading.event.data.TickerSymbolTradingRange;
import com.systematic.trading.event.order.OrderEvent;
import com.systematic.trading.event.order.OrderEventListener;

/**
 * Single entry point to output a simulation run into files.
 * 
 * @author CJ Hare
 */
public class FileDisplay implements BacktestDisplay {

	// TODO aggregate displays into arrays
	private final String parentDirectory;
	private ReturnOnInvestmentEventListener roiDisplay;
	private ReturnOnInvestmentEventListener roiDailyDisplay;
	private ReturnOnInvestmentEventListener roiMonthlyDisplay;
	private ReturnOnInvestmentEventListener roiYearlyDisplay;
	private FileEventDisplay eventDisplay;
	private CashEventListener cashEventDisplay;
	private BrokerageEventListener brokerageEventDisplay;
	private OrderEventListener ordertEventDisplay;
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

	final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, final TradingDayPrices lastTradingDay )
			throws Exception {

		final String returnOnInvestmentFilename = parentDirectory + "/return-on-investment.txt";
		this.roiDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentFilename,
				FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.ALL );

		final String returnOnInvestmentDailyFilename = parentDirectory + "/return-on-investment-daily.txt";
		this.roiDailyDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentDailyFilename,
				FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.DAILY );

		final String returnOnInvestmentMonthlyFilename = parentDirectory + "/return-on-investment-monthly.txt";
		this.roiMonthlyDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentMonthlyFilename,
				FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.MONTHLY );

		final String returnOnInvestmentYearlyFilename = parentDirectory + "/return-on-investment-yearly.txt";
		this.roiYearlyDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentYearlyFilename,
				FileReturnOnInvestmentDisplay.RETURN_ON_INVESTMENT_DISPLAY.YEARLY );

		final String eventFilename = parentDirectory + "/events.txt";
		this.eventDisplay = new FileEventDisplay( eventFilename, tickerSymbolTradingRange );

		final String cashEventFilename = parentDirectory + "/events-cash.txt";
		this.cashEventDisplay = new FileCashEventDisplay( cashEventFilename );

		final String orderEventFilename = parentDirectory + "/events-order.txt";
		this.ordertEventDisplay = new FileOrderEventDisplay( orderEventFilename );

		final String brokerageEventFilename = parentDirectory + "/events-brokerage.txt";
		this.brokerageEventDisplay = new FileBrokerageEventDisplay( brokerageEventFilename );

		final String statisticsFilename = parentDirectory + "/statistics.txt";
		this.statisticsDisplay = new FileEventStatisticsDisplay( eventStatistics, statisticsFilename );
		this.netWorthDisplay = new FileNetWorthSummaryDisplay( cumulativeRoi, statisticsFilename );
	}

	@Override
	public void event( final CashEvent event ) {
		eventDisplay.event( event );
		cashEventDisplay.event( event );
	}

	@Override
	public void event( final OrderEvent event ) {
		eventDisplay.event( event );
		ordertEventDisplay.event( event );
	}

	@Override
	public void event( final BrokerageEvent event ) {
		eventDisplay.event( event );
		brokerageEventDisplay.event( event );
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		roiDisplay.event( event );
		roiDailyDisplay.event( event );
		roiMonthlyDisplay.event( event );
		roiYearlyDisplay.event( event );
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		switch (transitionedState) {
			case COMPLETE:
				simulationCompleted();
			default:
				break;
		}
	}

	private void simulationCompleted() {
		statisticsDisplay.displayEventStatistics();
		netWorthDisplay.displayNetWorth();
	}

	@Override
	public void event( final NetWorthEvent event ) {
		netWorthDisplay.event( event );
	}
}
