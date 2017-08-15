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
package com.systematic.trading.backtest.output.elastic.model.index.networth;

import java.util.Arrays;

import javax.ws.rs.client.Entity;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldName;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticCommonIndex;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;

/**
 * Elastic Search index for Net worth events.
 * 
 * @author CJ Hare
 */
public class ElasticNetworthIndex extends ElasticCommonIndex {

	public ElasticNetworthIndex( final ElasticDao dao ) {
		super(dao);
	}

	public void event( final BacktestBatchId id, final NetWorthEvent event ) {
		post(id, Entity.json(new ElasticNetWorthEventResource(event)));
	}

	@Override
	protected ElasticIndexName getIndexName() {
		return ElasticIndexName.NETWORTH;
	}

	@Override
	protected ElasticIndexMapping getIndexMapping() {
		return new ElasticIndexMapping(Arrays.asList(getPair(ElasticFieldName.EVENT, ElasticFieldType.TEXT),
		        getPair(ElasticFieldName.CASH_BALANCE, ElasticFieldType.FLOAT),
		        getPair(ElasticFieldName.EQUITY_BALANCE, ElasticFieldType.FLOAT),
		        getPair(ElasticFieldName.EQUITY_BALANCE_VALUE, ElasticFieldType.FLOAT),
		        getPair(ElasticFieldName.NETWORTH, ElasticFieldType.FLOAT),
		        getPair(ElasticFieldName.EVENT_DATE, ElasticFieldType.DATE)));
	}
}