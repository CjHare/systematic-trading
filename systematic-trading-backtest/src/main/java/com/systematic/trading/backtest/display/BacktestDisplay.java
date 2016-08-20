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
package com.systematic.trading.backtest.display;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;
import com.systematic.trading.simulation.SimulationStateListener;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Output from back testing.
 * 
 * @author CJ Hare
 */
public interface BacktestDisplay
        extends CashEventListener, OrderEventListener, BrokerageEventListener, ReturnOnInvestmentEventListener,
        SimulationStateListener, NetWorthEventListener, SignalAnalysisListener, EquityEventListener {

	/**
	 * All the interesting data points for displaying.
	 * 
	 * @param configuration set up for the simulation being displayed.
	 * @param tradingData summary of the data set analysed.
	 * @param eventStatistics record of various event occurrences.
	 * @param cumulativeRoi sum of the return on investment over the course of back testing.
	 * @param lastTradingDay prices from the last day in the back test.
	 * @throws Exception problem encountered during the initialisation of the display.
	 */
	void init( BacktestBootstrapConfiguration configuration, TickerSymbolTradingData tradingData,
	        BacktestSimulationDates simulationDates, EventStatistics eventStatistics,
	        CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, TradingDayPrices lastTradingDay )
	                throws BacktestInitialisationException;
}