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
package com.systematic.trading.backtest.output.elastic.model.index;

import java.util.Arrays;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.model.ElasticCommonIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldName;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.simulation.cash.event.CashEvent;

/**
 * Elastic Search index for cash events.
 * 
 * @author CJ Hare
 */
public class ElasticCashIndex extends ElasticCommonIndex {

	public void event( final WebTarget root, final BacktestBatchId id, final CashEvent event ) {

		final ElasticCashEvent elasticEvent = new ElasticCashEvent(event);

		final String indexName = getIndexName().getName() + "/" + id.getName();

		//TODO this part is generic - move to the common index, given an entity

		// Using ID auto-generation, so we're using a post not a put
		final Response response = root.path(indexName).request().post(Entity.json(elasticEvent));

		System.out.println("Response code: " + response.getStatus());
		System.out.println("Response :" + response.readEntity(String.class));

		//TODO move this and the bean into it's own package, name the bean a resource
		System.out.println("Did putting the cash work?");
	}

	@Override
	protected ElasticIndex getIndex( final BacktestBatchId id ) {

		return new ElasticIndex(id.getName(),
		        Arrays.asList(getPair(ElasticFieldName.EVENT, ElasticFieldType.TEXT),
		                getPair(ElasticFieldName.AMOUNT, ElasticFieldType.FLOAT),
		                getPair(ElasticFieldName.FUNDS_BEFORE, ElasticFieldType.FLOAT),
		                getPair(ElasticFieldName.FUNDS_AFTER, ElasticFieldType.FLOAT)));
	}

	@Override
	protected ElasticIndexName getIndexName() {
		return ElasticIndexName.CASH;
	}
}