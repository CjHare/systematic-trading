/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest.output.elastic;

import java.util.concurrent.ExecutorService;

import com.systematic.trading.backtest.output.BacktestOutputPreparation;
import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfiguration;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.dao.impl.HttpElasticDao;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticBrokerageIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticCashIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticEquityIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticNetworthIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticOrderIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticReturnOnInvestmentIndex;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticSignalAnalysisIndex;

/**
 * Turns off the index refreshes during the bulk API operations.
 * 
 * @author CJ Hare
 */
public class ElasticBacktestOutputPreparation implements BacktestOutputPreparation {

	/** Pool is only used for Bulk API requests, which are not used during the preparation. */
	private static final ExecutorService NO_POOL = null;

	private final ElasticSignalAnalysisIndex signalAnalysisIndex;
	private final ElasticCashIndex cashIndex;
	private final ElasticOrderIndex orderIndex;
	private final ElasticBrokerageIndex brokerageIndex;
	private final ElasticReturnOnInvestmentIndex returnOnInvestmentIndex;
	private final ElasticNetworthIndex networthIndex;
	private final ElasticEquityIndex equityIndex;

	public ElasticBacktestOutputPreparation( final BackestOutputElasticConfiguration config ) {
		final ElasticDao dao = new HttpElasticDao();
		this.signalAnalysisIndex = new ElasticSignalAnalysisIndex(dao, NO_POOL, config);
		this.cashIndex = new ElasticCashIndex(dao, NO_POOL, config);
		this.orderIndex = new ElasticOrderIndex(dao, NO_POOL, config);
		this.brokerageIndex = new ElasticBrokerageIndex(dao, NO_POOL, config);
		this.returnOnInvestmentIndex = new ElasticReturnOnInvestmentIndex(dao, NO_POOL, config);
		this.networthIndex = new ElasticNetworthIndex(dao, NO_POOL, config);
		this.equityIndex = new ElasticEquityIndex(dao, NO_POOL, config);
	}

	@Override
	public void setUp() {
		ensureIndexesExist();
		setRefreshInterval(false);
	}

	@Override
	public void tearDown() {
		setRefreshInterval(true);
	}

	private void ensureIndexesExist() {
		signalAnalysisIndex.ensureIndexExists();
		cashIndex.ensureIndexExists();
		orderIndex.ensureIndexExists();
		brokerageIndex.ensureIndexExists();
		returnOnInvestmentIndex.ensureIndexExists();
		networthIndex.ensureIndexExists();
		equityIndex.ensureIndexExists();
	}

	private void setRefreshInterval( final boolean enabled ) {
		signalAnalysisIndex.setRefreshInterval(enabled);
		cashIndex.setRefreshInterval(enabled);
		orderIndex.setRefreshInterval(enabled);
		brokerageIndex.setRefreshInterval(enabled);
		returnOnInvestmentIndex.setRefreshInterval(enabled);
		networthIndex.setRefreshInterval(enabled);
		equityIndex.setRefreshInterval(enabled);
	}
}