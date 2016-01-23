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

import java.time.Period;

import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;

/**
 * Single entry point to output a simulation run into files, displaying only the summary and
 * comparisons.
 * <p/>
 * Produces no IO operations.
 * 
 * @author CJ Hare
 */
public class FileNoDisplay implements BacktestDisplay {

	@Override
	public void init( final TickerSymbolTradingData tradingData, final EventStatistics eventStatistics,
			final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, final TradingDayPrices lastTradingDay,
			final Period duration ) throws Exception {
	}

	@Override
	public void event( final CashEvent event ) {
	}

	@Override
	public void event( final OrderEvent event ) {
	}

	@Override
	public void event( final BrokerageEvent event ) {
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {
	}

	@Override
	public void event( EquityEvent event ) {
	}
}
