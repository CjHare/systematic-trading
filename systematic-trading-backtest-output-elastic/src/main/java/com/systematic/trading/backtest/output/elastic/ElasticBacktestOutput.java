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

import java.util.concurrent.ExecutorService;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.dao.impl.HttpElasticDao;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticBrokerageIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticCashIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticEquityIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticNetworthIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticOrderIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticReturnOnInvestmentIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticSignalAnalysisIndex;
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
 * A Facade for getting the event data into Elastic Search using the rest HTTP end point.
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
	private final BacktestBatchId batchId;

	public ElasticBacktestOutput( final BacktestBatchId batchId, final ExecutorService pool ) {

		//TODO configuration value - input!
		final int bulkApiBucketSize = 24000;

		final ElasticDao dao = new HttpElasticDao();
		this.signalAnalysisIndex = new ElasticSignalAnalysisIndex(dao, pool, bulkApiBucketSize);
		this.cashIndex = new ElasticCashIndex(dao, pool, bulkApiBucketSize);
		this.orderIndex = new ElasticOrderIndex(dao, pool, bulkApiBucketSize);
		this.brokerageIndex = new ElasticBrokerageIndex(dao, pool, bulkApiBucketSize);
		this.returnOnInvestmentIndex = new ElasticReturnOnInvestmentIndex(dao, pool, bulkApiBucketSize);
		this.networthIndex = new ElasticNetworthIndex(dao, pool, bulkApiBucketSize);
		this.equityIndex = new ElasticEquityIndex(dao, pool, bulkApiBucketSize);
		this.batchId = batchId;
	}

	@Override
	public void init( TickerSymbolTradingData tradingData, BacktestSimulationDates dates,
	        EventStatistics eventStatistics, CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi,
	        TradingDayPrices lastTradingDay ) {
		signalAnalysisIndex.init(batchId);
		cashIndex.init(batchId);
		orderIndex.init(batchId);
		brokerageIndex.init(batchId);
		returnOnInvestmentIndex.init(batchId);
		networthIndex.init(batchId);
		equityIndex.init(batchId);
	}

	@Override
	public void flush() {
		signalAnalysisIndex.flush();
		cashIndex.flush();
		orderIndex.flush();
		brokerageIndex.flush();
		returnOnInvestmentIndex.flush();
		networthIndex.flush();
		equityIndex.flush();
	}

	//TODO move the pool into the index
	//TODO configuration for # of concurrent connections?
	@Override
	public void event( final SignalAnalysisEvent event ) {
		signalAnalysisIndex.event(batchId, event);
	}

	@Override
	public void event( final CashEvent event ) {
		cashIndex.event(batchId, event);
	}

	@Override
	public void event( final OrderEvent event ) {
		orderIndex.event(batchId, event);
	}

	@Override
	public void event( final BrokerageEvent event ) {
		brokerageIndex.event(batchId, event);
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		returnOnInvestmentIndex.event(batchId, event);
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {
		networthIndex.event(batchId, event);
	}

	@Override
	public void event( final EquityEvent event ) {
		equityIndex.event(batchId, event);
	}
}