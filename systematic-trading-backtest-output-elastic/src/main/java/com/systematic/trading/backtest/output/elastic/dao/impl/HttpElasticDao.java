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
package com.systematic.trading.backtest.output.elastic.dao.impl;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticPostEventResponse;

/**
 * Connection to Elastic search over the HTTP API.
 * 
 * @author CJ Hare
 */
public class HttpElasticDao implements ElasticDao {

	//TODO inject this! - configuration value
	/** Location of the elastic search end point. */
	private static final String ELASTIC_ENDPOINT_URL = "http://localhost:9200";

	/** Base of the elastic search Restful end point. */
	private final WebTarget root;

	//TODO use an exectuor pool for the Java-RS operations?
	// final ExecutorService pool

	public HttpElasticDao() {

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(ELASTIC_ENDPOINT_URL);

	}

	@Override
	public Response get( final ElasticIndexName indexName ) {
		final String path = indexName.getName();
		return root.path(path).request(MediaType.APPLICATION_JSON).get();
	}

	@Override
	public Response get( final ElasticIndexName indexName, final BacktestBatchId id ) {
		final String path = getMappingPath(indexName, id);
		return root.path(path).request(MediaType.APPLICATION_JSON).get();
	}

	public void post( final ElasticIndexName indexName, final BacktestBatchId id, final Entity<?> requestBody ) {
		final WebTarget url = root.path(getMappingPath(indexName, id));

		// Using the elastic search ID auto-generation, so we're using a post not a put
		final Response response = url.request().post(requestBody);

		if (response.getStatus() != 201) {
			throw new ElasticException(
			        String.format("Expecting a HTTP 201 instead receieved HTTP %s, URL: %s, body: %s",
			                response.getStatus(), url, requestBody));
		}

		final ElasticPostEventResponse eventResponse = response.readEntity(ElasticPostEventResponse.class);

		if (isInvalidResponse(indexName, id, eventResponse)) {
			throw new ElasticException(String.format("Unexpected response: %s, to request URL: %s, body: %s",
			        eventResponse, url, requestBody));
		}
	}

	@Override
	public void put( final ElasticIndexName indexName, final BacktestBatchId id, final Entity<?> requestBody ) {

		final String path = getMappingPath(indexName, id);
		final Response response = root.path(path).request().put(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(String.format("Failed to put the mapping to: %s", path));
		}
	}

	@Override
	public void put( final ElasticIndexName indexName, final Entity<?> requestBody ) {

		final String path = indexName.getName();
		final Response response = root.path(path).request().put(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(String.format("Failed to put the index to: %s", path));
		}

	}

	//TODO need without the _mapping for poating / putting data
	//TODO only need the _mapping for inserting / updating mapping
	private String getMappingPath( final ElasticIndexName indexName, final BacktestBatchId id ) {
		return String.format("%s/_mapping/%s/", indexName.getName(), id.getName());
	}

	private boolean isInvalidResponse( final ElasticIndexName indexName, final BacktestBatchId id,
	        final ElasticPostEventResponse eventResponse ) {
		return !isValidResponse(indexName, id, eventResponse);
	}

	private boolean isValidResponse( final ElasticIndexName indexName, final BacktestBatchId id,
	        final ElasticPostEventResponse eventResponse ) {
		return eventResponse.isCreated() && eventResponse.isResultCreated()
		        && StringUtils.equals(indexName.getName(), eventResponse.getIndex())
		        && StringUtils.equals(id.getName(), eventResponse.getType());
	}
}