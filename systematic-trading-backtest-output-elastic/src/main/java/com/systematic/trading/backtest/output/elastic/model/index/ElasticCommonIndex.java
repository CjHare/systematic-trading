/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfiguration;
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
 * Behaviour common for indexes put into Elastic Search
 * 
 * @author CJ Hare
 */
public abstract class ElasticCommonIndex {

	/** Bulk API action for creating document and generating it's ID. */
	private static final String ACTION_CREATE_GENERATE_DOCUMENT_ID = "index";

	/** Value to disable the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DISABLE = "-1";

	/** Default value for the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DEFAULT = "1s";

	/** Access to Elastic Search endpoint. */
	private final ElasticDao dao;

	/** Number of requests that are grouped together for the Bulk API. */
	private final int bulkApiQueueSize;

	/** Storage for the meta and source requests. */
	private List<Object> bulkApiQueue;

	/** Delegate worker threads that deal with performing sending to Elastic. */
	private final ExecutorService pool;

	/** Elastic Search primary shards. */
	private final int numberOfShards;

	/** Elastic Search replications of the primary shards. */
	private final int numberOfReplicas;

	public ElasticCommonIndex( final ElasticDao dao, final ExecutorService pool,
	        final BackestOutputElasticConfiguration config ) {

		this.dao = dao;
		this.pool = pool;

		// Each source request (document to created) is accompanied by a meta object
		this.bulkApiQueueSize = 2 * config.bulkApiQueueSize();
		this.bulkApiQueue = new ArrayList<>(this.bulkApiQueueSize);

		this.numberOfShards = config.numberOfShards();
		this.numberOfReplicas = config.numberOfReplicas();
	}

	/**
	 * Ensures the index and mapping are created, and verifies there is not existing data.
	 */
	public void init( final BacktestBatchId id ) {

		if (isIndexMappingMissing(id)) {
			putIndexMapping(id);
		} else {
			throw new ElasticException(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", id));
		}
	}

	public void ensureIndexExists() {

		if (isIndexMissing()) {
			putIndex();
		}
	}

	public void refreshInterval( final boolean enabled ) {

		dao.putSetting(indexName(), Entity.json(new ElasticIndexSettingsRequestResource(
		        enabled ? INDEX_SETTING_REFRESH_DEFAULT : INDEX_SETTING_REFRESH_DISABLE)));
	}

	public void flush() {

		if (!bulkApiQueue.isEmpty()) {
			send(bulkApiQueue);
		}
	}

	protected <T> void create( final BacktestBatchId id, final T requestResource ) {

		bulkApiQueue.add(createBulkApiMeta(id));
		bulkApiQueue.add(requestResource);

		if (isBulkApiBucketFull()) {
			send(bulkApiQueue);
			bulkApiQueue = new ArrayList<>(bulkApiQueueSize);

		}
	}

	private void send( final List<?> requests ) {

		pool.submit(() -> dao.postTypes(indexName(), Entity.json(requests)));
	}

	protected ElasticBulkApiMetaDataRequestResource createBulkApiMeta( final BacktestBatchId id ) {

		// TODO put this into a builder & move ACTION_CREATE_GENERATE_DOCUMENT_ID out of this class
		return new ElasticBulkApiMetaDataRequestResource(ACTION_CREATE_GENERATE_DOCUMENT_ID, null, id.name(), null);
	}

	protected ElasticIndex index() {

		return new ElasticIndex(numberOfShards, numberOfReplicas);
	}

	protected abstract ElasticIndexMapping indexMapping();

	protected abstract ElasticIndexName indexName();

	protected Pair<ElasticFieldName, ElasticFieldType> pair( final ElasticFieldName name,
	        final ElasticFieldType type ) {

		return new ImmutablePair<>(name, type);
	}

	private boolean isBulkApiBucketFull() {

		return bulkApiQueue.size() >= bulkApiQueueSize;
	}

	private boolean isIndexMissing() {

		final Response response = dao.index(indexName());
		return response.getStatus() != 200;
	}

	private boolean isIndexMappingMissing( final BacktestBatchId id ) {

		final Response response = dao.mapping(indexName(), id);

		// TODO mapping shouls be 200 & empty JSON - test with data present - currently exceptional

		return response.getStatus() != 200
		        || (response.getStatus() == 200 && response.readEntity(ElasticEmptyIndexMapping.class) != null);
	}

	private void putIndex() {

		dao.put(indexName(), Entity.json(index()));
	}

	private void putIndexMapping( final BacktestBatchId id ) {

		dao.putMapping(indexName(), id, Entity.json(indexMapping()));
	}
}