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
package com.systematic.trading.backtest.output.elastic;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.model.index.brokerage.ElasticBrokerageIndex;
import com.systematic.trading.backtest.output.elastic.model.index.cash.ElasticCashIndex;
import com.systematic.trading.backtest.output.elastic.model.index.equity.ElasticEquityIndex;
import com.systematic.trading.backtest.output.elastic.model.index.networth.ElasticNetworthIndex;
import com.systematic.trading.backtest.output.elastic.model.index.order.ElasticOrderIndex;
import com.systematic.trading.backtest.output.elastic.model.index.roi.ElasticReturnOnInvestmentIndex;
import com.systematic.trading.backtest.output.elastic.model.index.signal.analysis.ElasticSignalAnalysisIndex;
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
 * Puts the event data into Elastic Search using the rest HTTP end point.
 * 
 * @author CJ Hare
 */
public class ElasticBacktestOutput implements BacktestOutput {

	private final ElasticSignalAnalysisIndex signalAnalysisIndex;
	private final ElasticCashIndex cashIndex;
	private final ElasticOrderIndex orderIndex;
	private final ElasticBrokerageIndex brokerageIndex;
	private final ElasticReturnOnInvestmentIndex returnOnInvestmentIndex;
	private final ElasticNetworthIndex networthIndex;
	private final ElasticEquityIndex equityIndex;

	private final BacktestBatchId id;

	public ElasticBacktestOutput( final BacktestBatchId id ) {
		final ElasticDao dao = new ElasticDao();
		this.id = id;
		this.signalAnalysisIndex = new ElasticSignalAnalysisIndex(id, dao);
		this.cashIndex = new ElasticCashIndex(id, dao);
		this.orderIndex = new ElasticOrderIndex(id, dao);
		this.brokerageIndex = new ElasticBrokerageIndex(id, dao);
		this.returnOnInvestmentIndex = new ElasticReturnOnInvestmentIndex(id, dao);
		this.networthIndex = new ElasticNetworthIndex(id, dao);
		this.equityIndex = new ElasticEquityIndex(id, dao);
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {
		signalAnalysisIndex.event(event);
	}

	@Override
	public void event( final CashEvent event ) {
		cashIndex.event(event);
	}

	@Override
	public void event( final OrderEvent event ) {
		orderIndex.event(event);
	}

	@Override
	public void event( final BrokerageEvent event ) {
		brokerageIndex.event(event);
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		returnOnInvestmentIndex.event(event);
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {
		networthIndex.event(event);
	}

	@Override
	public void event( final EquityEvent event ) {
		equityIndex.event(event);
	}

	@Override
	public void init( final BacktestBootstrapConfiguration configuration, final TickerSymbolTradingData tradingData,
	        final BacktestSimulationDates simulationDates, final EventStatistics eventStatistics,
	        final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, final TradingDayPrices lastTradingDay )
	        throws BacktestInitialisationException {

		signalAnalysisIndex.init(id);
		cashIndex.init(id);
		orderIndex.init(id);
		brokerageIndex.init(id);
		returnOnInvestmentIndex.init(id);
		networthIndex.init(id);
		equityIndex.init(id);
	}
}