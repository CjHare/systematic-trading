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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiMetaDataRequestResource;
import com.systematic.trading.backtest.output.elastic.resource.ElasticIndexSettingsRequestResource;

/**
 * Behaviour  common for indexes put into Elastic Search
 * 
 * @author CJ Hare
 */
public abstract class ElasticCommonIndex {

	/** Bulk API action for creating document and generating it's ID. */
	private static final String ACTION_CREATE_GENERATE_DOCUMENT_ID = "index";

	//TODO move these into configuration values
	/** Value to disable the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DISABLE = "-1";

	/** Default value for the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DEFAULT = "1s";

	/** 
	 * The number of primary shards that an index should have, which defaults to 5. 
	 * This setting cannot be changed after index creation.
	 */
	private static final int DEFAULT_NUMBER_OF_SHARDS = 5;

	/** The number of replica shards (copies) that each primary shard should have, which defaults to 1. 	 */
	private static final int DEFAULT_NUMBER_OF_REPLICAS = 1;

	/** Access to Elastic Search endpoint.*/
	private final ElasticDao dao;

	/** Number of requests that are grouped together for the Bulk API.*/
	private final int bulkApiBucketSize;

	/** Storage for the meta and source requests. */
	private List<Object> bulkApiBucket;

	/** Delegate worker threads that deal with performing sending to Elastic. */
	private final ExecutorService pool;

	//TODO refactor the pool & size  into a configuration object
	public ElasticCommonIndex( final ElasticDao dao, final ExecutorService pool, final int bulkApiBucketSize ) {
		this.dao = dao;
		this.pool = pool;

		// Each source request (document to created) is accompanied by a meta object
		this.bulkApiBucketSize = 2 * bulkApiBucketSize;
		this.bulkApiBucket = new ArrayList<>(this.bulkApiBucketSize);
	}

	/**
	 * Ensures the index and mapping are created, and verifies there is not existing data.
	 */
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

	public void setRefreshInterval( final boolean enabled ) {
		dao.putSetting(getIndexName(), Entity.json(new ElasticIndexSettingsRequestResource(
		        enabled ? INDEX_SETTING_REFRESH_DEFAULT : INDEX_SETTING_REFRESH_DISABLE)));
	}

	public void flush() {
		send(bulkApiBucket);
	}

	protected <T> void create( final BacktestBatchId id, final T requestResource ) {
		bulkApiBucket.add(createBulkApiMeta(id));
		bulkApiBucket.add(requestResource);

		if (isBulkApiBucketFull()) {
			send(bulkApiBucket);
			bulkApiBucket = new ArrayList<>(bulkApiBucketSize);

		}
	}

	private void send( final List<?> requests ) {
		pool.submit(() -> dao.postTypes(getIndexName(), Entity.json(requests)));
	}

	protected ElasticBulkApiMetaDataRequestResource createBulkApiMeta( final BacktestBatchId id ) {
		return new ElasticBulkApiMetaDataRequestResource(ACTION_CREATE_GENERATE_DOCUMENT_ID, null, id.getName(), null);
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

	private boolean isBulkApiBucketFull() {
		return bulkApiBucket.size() >= bulkApiBucketSize;
	}

	private boolean isIndexMissing() {
		final Response response = dao.getIndex(getIndexName());
		return response.getStatus() != 200;
	}

	private boolean isIndexMappingMissing( final BacktestBatchId id ) {
		final Response response = dao.getMapping(getIndexName(), id);

		//TODO mapping shouls be 200 & empty JSON - test with data present - currently exceptional

		return response.getStatus() != 200
		        || (response.getStatus() == 200 && response.readEntity(ElasticEmptyIndexMapping.class) != null);
	}

	private void createIndex() {
		dao.put(getIndexName(), Entity.json(getIndex()));
	}

	private void createIndexMapping( final BacktestBatchId id ) {
		dao.putMapping(getIndexName(), id, Entity.json(getIndexMapping()));
	}
}