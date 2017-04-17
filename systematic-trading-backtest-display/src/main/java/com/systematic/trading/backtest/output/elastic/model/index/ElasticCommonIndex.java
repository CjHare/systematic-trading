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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
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

	public void init( final WebTarget root, final BacktestBatchId id ) {

		//TODO separate the index from the mappings
		//TODO index needs to exist, include settings such as shards & replicas
		
		//TODO need to check whether the mapping already exists - if so error, existing data
		
		if (isIndexMissing(root, id)) {
			createIndex(root, id);
		}
	}

	public void post( final WebTarget root, final BacktestBatchId id, final Entity<?> requestBody ) {

		final WebTarget url = root.path(getPath(id));

		// Using the elastic search ID auto-generation, so we're using a post not a put
		final Response response = url.request().post(requestBody);

		if (response.getStatus() != 201) {
			throw new ElasticException(
			        String.format("Expecting a HTTP 201 instead receieved HTTP %s, URL: %s, body: %s",
			                response.getStatus(), url, requestBody));
		}

		final ElasticPostEventResponse eventResponse = response.readEntity(ElasticPostEventResponse.class);

		if (isInvalidResponse(id, eventResponse)) {
			throw new ElasticException(String.format("Unexpected response: %s, to request URL: %s, body: %s",
			        eventResponse, url, requestBody));
		}
	}

	//TODO - don't need the index, only use mapping?
	protected abstract ElasticIndex getIndex( BacktestBatchId id );

	//TODO - don't need the index, only use mapping?
	protected abstract ElasticIndexMapping getIndexMapping();

	protected abstract ElasticIndexName getIndexName();

	protected Pair<ElasticFieldName, ElasticFieldType> getPair( final ElasticFieldName name,
	        final ElasticFieldType type ) {
		return new ImmutablePair<ElasticFieldName, ElasticFieldType>(name, type);
	}

	private boolean isIndexMissing( final WebTarget root, final BacktestBatchId id ) {

		final String path = getPath(id);
		final Response response = root.path(path).request(MediaType.APPLICATION_JSON).get();

		System.out.println("Response code: " + response.getStatus());
		System.out.println("Response :" + response.readEntity(String.class));

		//TODO verify index has structure expected (index object is returned

		return response.getStatus() != 200;
	}

	private void createIndex( final WebTarget root, final BacktestBatchId id ) {

		final Entity<?> requestBody = Entity.json(getIndexMapping());

		final Response response = root.path(getPutMapping(id)).request().put(requestBody);

		System.out.println("Response code: " + response.getStatus());
		System.out.println("Response :" + response.readEntity(String.class));

		//TODO parse the response, only a problem if not 200

	}

	private String getPath( final BacktestBatchId id ) {
		return String.format("%s/%s", getIndexName().getName(), id.getName());
	}

	private String getPutMapping( final BacktestBatchId id ) {
		return String.format("%s/_mapping/%s", getIndexName().getName(), id.getName());
	}

	private boolean isInvalidResponse( final BacktestBatchId id, final ElasticPostEventResponse eventResponse ) {
		return !isValidResponse(id, eventResponse);
	}

	private boolean isValidResponse( final BacktestBatchId id, final ElasticPostEventResponse eventResponse ) {
		return eventResponse.isCreated() && eventResponse.isResultCreated()
		        && StringUtils.equals(getIndexName().getName(), eventResponse.getIndex())
		        && StringUtils.equals(id.getName(), eventResponse.getType());
	}
}