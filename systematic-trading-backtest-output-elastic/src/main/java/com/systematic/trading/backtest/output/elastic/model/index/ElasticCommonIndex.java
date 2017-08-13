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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticEmptyIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldName;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;

/**
 * Behaviour  common for indexes put into Elastic Search
 * 
 * @author CJ Hare
 */
public abstract class ElasticCommonIndex {

	/** 
	 * The number of primary shards that an index should have, which defaults to 5. 
	 * This setting cannot be changed after index creation.
	 */
	private static final int DEFAULT_NUMBER_OF_SHARDS = 5;

	/** The number of replica shards (copies) that each primary shard should have, which defaults to 1. 	 */
	private static final int DEFAULT_NUMBER_OF_REPLICAS = 1;

	/** Access to Elastic Search endpoint.*/
	private final ElasticDao dao;

	/** Identity of the back testing  run.*/
	private final BacktestBatchId id;

	public ElasticCommonIndex( final BacktestBatchId id, final ElasticDao dao ) {
		this.id = id;
		this.dao = dao;

		init(id);
	}

	//TODO make this private & fix up the tests
	public void init( final BacktestBatchId id ) {

		if (isIndexMissing()) {
			createIndex();
		}

		if (isIndexMappingMissing(id)) {
			createIndexMapping(id);
		} else {
			throw new ElasticException(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", id));
		}
	}

	protected BacktestBatchId getBacktestBatchId() {
		return id;
	}

	protected void post( final BacktestBatchId id, final Entity<?> requestBody ) {
		dao.post(getIndexName(), id, requestBody);
	}

	protected ElasticIndex getIndex() {
		return new ElasticIndex(DEFAULT_NUMBER_OF_SHARDS, DEFAULT_NUMBER_OF_REPLICAS);
	}

	protected abstract ElasticIndexMapping getIndexMapping();

	protected abstract ElasticIndexName getIndexName();

	protected Pair<ElasticFieldName, ElasticFieldType> getPair( final ElasticFieldName name,
	        final ElasticFieldType type ) {
		return new ImmutablePair<ElasticFieldName, ElasticFieldType>(name, type);
	}

	private boolean isIndexMissing() {
		final Response response = dao.get(getIndexName());
		return response.getStatus() != 200;
	}

	private boolean isIndexMappingMissing( final BacktestBatchId id ) {
		final Response response = dao.get(getIndexName(), id);

		//TODO mapping shouls be 200 & empty JSON

		return response.getStatus() != 200
		        || (response.getStatus() == 200 && response.readEntity(ElasticEmptyIndexMapping.class) != null);
	}

	private void createIndex() {
		dao.put(getIndexName(), Entity.json(getIndex()));
	}

	private void createIndexMapping( final BacktestBatchId id ) {
		dao.put(getIndexName(), id, Entity.json(getIndexMapping()));
	}
}