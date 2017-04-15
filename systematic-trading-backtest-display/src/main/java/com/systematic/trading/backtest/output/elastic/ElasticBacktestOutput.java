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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.display.BacktestOutput;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
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
 * Puts the event data into Elastic Search using the rest HTTP end point.
 * 
 * @author CJ Hare
 */
public class ElasticBacktestOutput implements BacktestOutput {

	/** Location of the elastic search end point. */
	private static final String ELASTIC_ENDPOINT_URL = "http://localhost:9200";

	/** Base of the elastic search Restful end point. */
	private final WebTarget root;

	private final ElasticSignalAnalysisIndex signalAnalysisIndex;
	private final ElasticCashIndex cashIndex;
	private final ElasticOrderIndex orderIndex;
	private final ElasticBrokerageIndex brokerageIndex;
	private final ElasticReturnOnInvestmentIndex returnOnInvestmentIndex;
	private final ElasticNetworthIndex networthIndex;
	private final ElasticEquityIndex equityIndex;

	private final BacktestBatchId id;
	//TODO use an exectuor pool for the Java-RS operations?
	// final ExecutorService pool

	public ElasticBacktestOutput(final BacktestBatchId id) {

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(ELASTIC_ENDPOINT_URL);

		this.id = id;
		this.signalAnalysisIndex = new ElasticSignalAnalysisIndex();
		this.cashIndex = new ElasticCashIndex();
		this.orderIndex = new ElasticOrderIndex();
		this.brokerageIndex = new ElasticBrokerageIndex();
		this.returnOnInvestmentIndex = new ElasticReturnOnInvestmentIndex();
		this.networthIndex = new ElasticNetworthIndex();
		this.equityIndex = new ElasticEquityIndex();
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {
		signalAnalysisIndex.event(event);
	}

	@Override
	public void event( final CashEvent event ) {
		cashIndex.event(root, id, event);
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

	//TODO why the init???? shouldn't have a constructor & init - only one
	@Override
	public void init( final BacktestBootstrapConfiguration configuration, final TickerSymbolTradingData tradingData,
	        final BacktestSimulationDates simulationDates, final EventStatistics eventStatistics,
	        final CulmativeTotalReturnOnInvestmentCalculator cumulativeRoi, final TradingDayPrices lastTradingDay )
	        throws BacktestInitialisationException {

		signalAnalysisIndex.init(root, id);
		cashIndex.init(root, id);
		orderIndex.init(root, id);
		brokerageIndex.init(root, id);
		returnOnInvestmentIndex.init(root, id);
		networthIndex.init(root, id);
		equityIndex.init(root, id);
	}
}