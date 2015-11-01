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

import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.backtest.event.BrokerageEvent;
import com.systematic.trading.backtest.event.CashEvent;
import com.systematic.trading.backtest.event.OrderEvent;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.Event;
import com.systematic.trading.event.EventListener;
import com.systematic.trading.event.data.TickerSymbolTradingRange;

/**
 * Single entry point to output a simulation run into files.
 * 
 * @author CJ Hare
 */
public class FileDisplay implements EventListener {

	private final EventListener roiDisplay;
	private final EventListener eventDisplay;
	private final EventStatisticsDisplay statisticsDisplay;
	private final NetWorthSummaryDisplay netWorthDisplay;

	public FileDisplay( final TickerSymbolTradingRange tickerSymbolTradingRange, final EventStatistics eventStatistics,
			final Brokerage broker, final CashAccount cashAccount,
			final CulmativeReturnOnInvestmentCalculatorListener cumulativeRoi, final TradingDayPrices[] tradingData ) {
		final String eventStatisticsFilename = "a/event-statistics.txt";
		final String netWorthFilename = "a/net-worth.txt";
		final String returnOnInvestmentFilename = "a/return-on-investment.txt";
		final String eventFilename = "a/events.txt";

		this.roiDisplay = new FileReturnOnInvestmentDisplay( returnOnInvestmentFilename );
		this.eventDisplay = new FileEventDisplay( eventFilename, tickerSymbolTradingRange );
		this.statisticsDisplay = new FileEventStatisticsDisplay( eventStatistics, eventStatisticsFilename );
		this.netWorthDisplay = new FileNetWorthSummaryDisplay( broker, tradingData, cashAccount, cumulativeRoi,
				netWorthFilename );
	}

	@Override
	public void event( final Event event ) {
		roiDisplay.event( event );

		// TODO refactoring the events to use explicit types instead of a single higher level one is
		// needed

		if (event instanceof BrokerageEvent || event instanceof CashEvent || event instanceof OrderEvent) {
			eventDisplay.event( event );
		}
	}

	public void simulationCompleted() {
		statisticsDisplay.displayEventStatistics();
		netWorthDisplay.displayNetWorth();
	}
}
