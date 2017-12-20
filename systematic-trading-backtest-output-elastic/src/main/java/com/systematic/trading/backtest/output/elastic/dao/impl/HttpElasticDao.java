/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiResponseResource;
import com.systematic.trading.backtest.output.elastic.serialize.ElasticSearchBulkApiMetaDataSerializer;
import com.systematic.trading.backtest.output.elastic.serialize.NdjsonListSerializer;

/**
 * Connection to Elastic search over the HTTP API.
 * 
 * @author CJ Hare
 */
public class HttpElasticDao implements ElasticDao {

	// TODO inject this! - configuration value
	/** Location of the elastic search end point. */
	private static final String ELASTIC_ENDPOINT_URL = "http://localhost:9200";

	/** Base of the elastic search Restful end point. */
	private final WebTarget root;

	/** Base of the elastic search Restful end point, serializes with Nd-json. */
	private final WebTarget bulkApiRoot;

	public HttpElasticDao() {

		// Registering the provider for POJO -> JSON
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final JacksonJsonProvider provider = new JacksonJsonProvider();
		provider.setMapper(mapper);
		final ClientConfig config = new ClientConfig(provider);

		// End point target root
		this.root = ClientBuilder.newClient(config).target(ELASTIC_ENDPOINT_URL);

		// Registering the provider for POJO -> NDJSON
		final ObjectMapper ndjsonMapper = new ObjectMapper();
		final SimpleModule ndjsonModule = new SimpleModule("Ndjson List Serializer");
		ndjsonModule.addSerializer(new NdjsonListSerializer());
		ndjsonModule.addSerializer(new ElasticSearchBulkApiMetaDataSerializer());
		ndjsonMapper.registerModule(ndjsonModule);
		ndjsonMapper.registerModule(new JavaTimeModule());
		final JacksonJsonProvider ndjsonProvider = new JacksonJsonProvider();
		ndjsonProvider.setMapper(ndjsonMapper);

		final ClientConfig ndjsonConfig = new ClientConfig(ndjsonProvider);

		this.bulkApiRoot = ClientBuilder.newClient(ndjsonConfig).target(ELASTIC_ENDPOINT_URL);
	}

	@Override
	public Response index( final ElasticIndexName indexName ) {

		final String path = indexName.indexName();
		return root.path(path).request(MediaType.APPLICATION_JSON).get();
	}

	@Override
	public Response mapping( final ElasticIndexName indexName, final BacktestBatchId id ) {

		final String path = getMappingPath(indexName, id);
		return root.path(path).request(MediaType.APPLICATION_JSON).get();
	}

	@Override
	public void postTypes( final ElasticIndexName indexName, final Entity<?> requestBody ) {

		final WebTarget url = bulkApiRoot.path(getIndexBulkApiPath(indexName));

		// Bulk API uses only HTTP POST for all operations
		final Response response = url.request(MediaType.APPLICATION_JSON).post(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Expecting a HTTP 200 instead receieved HTTP %s, URL: %s, body: %s",
			                response.getStatus(), url, requestBody));
		}

		final ElasticBulkApiResponseResource eventResponse = response.readEntity(ElasticBulkApiResponseResource.class);

		if (isInvalidResponse(eventResponse)) {
			throw new ElasticException(String.format("Unexpected response: %s, to request URL: %s, body: %s",
			        eventResponse, url, requestBody));
		}
	}

	@Override
	public void putMapping( final ElasticIndexName indexName, final BacktestBatchId id, final Entity<?> requestBody ) {

		final String path = getMappingPath(indexName, id);
		final Response response = root.path(path).request().put(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(String.format("Failed to put the mapping to: %s", path));
		}
	}

	@Override
	public void put( final ElasticIndexName indexName, final Entity<?> requestBody ) {

		final String path = indexName.indexName();
		final Response response = root.path(path).request().put(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Failed to put the index to: %s, http status: %s", path, response.getStatus()));
		}
	}

	// TODO move the Entity.json() operations into this DAO

	@Override
	public void putSetting( final ElasticIndexName indexName, final Entity<?> requestBody ) {

		final String path = getSettingPath(indexName);
		final Response response = root.path(path).request().put(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(String.format("Failed to put the index setting to: %s", path));
		}

	}

	private String getSettingPath( final ElasticIndexName indexName ) {

		return String.format("%s/_settings", indexName.indexName());
	}

	/**
	 * Path for retrieving or putting mapping data to elastic search.
	 */
	private String getMappingPath( final ElasticIndexName indexName, final BacktestBatchId id ) {

		return String.format("%s/_mapping/%s/", indexName.indexName(), id.name());
	}

	private String getIndexBulkApiPath( final ElasticIndexName indexName ) {

		return String.format("%s/_bulk", indexName.indexName());
	}

	private boolean isInvalidResponse( final ElasticBulkApiResponseResource eventResponse ) {

		return eventResponse.hasErrors();
	}
}