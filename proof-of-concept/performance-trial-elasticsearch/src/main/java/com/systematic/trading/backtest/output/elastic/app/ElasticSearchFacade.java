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
package com.systematic.trading.backtest.output.elastic.app;

import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.DATE_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.FLOAT_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.INDEX_NAME;
import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.MAPPING_NAME;
import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.TEXT_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.PerformanceTrialFields.TYPE;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.backtest.output.elastic.app.configuration.ElasticSearchConfiguration;
import com.systematic.trading.backtest.output.elastic.app.model.index.ElasticIndexSettingsResource;
import com.systematic.trading.backtest.output.elastic.app.resource.ElasticSearchPerformanceTrialRequestResource;
import com.systematic.trading.backtest.output.elastic.app.serializer.ElasticSearchBulkApiMetaDataSerializer;
import com.systematic.trading.backtest.output.elastic.app.serializer.NdjsonListSerializer;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticPostEventResponse;
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiResponseResource;

/**
 * Facade to Elastic Search, abstracting the index and mappings.
 * 
 * Facilitates posting to a single index with mapping, with a text, float and date field, mirroring the structure 
 * and field types that will be used for a back test run.
 * 
 * @author CJ Hare
 */
public class ElasticSearchFacade {

	private static final String INDEX_SETTING_DISABLE_REFRESH = "-1";
	private static final String INDEX_SETTING_DEFAULT_REFRESH = "1s";

	/** Base of the elastic search Restful end point. */
	private final WebTarget root;

	/** Base of the elastic search Restful end point, serializes with Ndjson. */
	private final WebTarget bulkApiRoot;

	private final int numberOfShards;
	private final int numberOfReplicas;

	private final static MediaType APPLICATION_NDJSON_TYPE = new MediaType("application", "x-ndjson");

	public ElasticSearchFacade( final ElasticSearchConfiguration elasticConfig ) {

		// Registering the provider for POJO -> JSON
		final ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.registerModule(new JavaTimeModule());
		final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		jsonProvider.setMapper(jsonMapper);
		final ClientConfig jsonConfig = new ClientConfig(jsonProvider);

		this.root = ClientBuilder.newClient(jsonConfig).target(elasticConfig.getEndpoint());

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

		this.bulkApiRoot = ClientBuilder.newClient(ndjsonConfig).target(elasticConfig.getEndpoint());

		this.numberOfShards = elasticConfig.getNumberOfShards();
		this.numberOfReplicas = elasticConfig.getNumberOfReplicas();
	}

	/**
	 * Deletes any existing index, mapping and data.
	 */
	public void delete() {
		final Response response = root.path(INDEX_NAME).request(MediaType.APPLICATION_JSON).delete();
		final int code = response.getStatus();

		if (code != 404 && code != 200) {
			throw new ElasticException(
			        String.format("Failed to delete the index: %s, status: %s", INDEX_NAME, response.getStatus()));
		}
	}

	public void putIndex() {
		final Response response = root.path(INDEX_NAME).request().put(getIndexRequestBody());

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Failed to put the index: %s, status: %s", INDEX_NAME, response.getStatus()));
		}
	}

	public void putMapping() {
		final Map<String, Object> message = new HashMap<>();
		message.put(TEXT_FIELD_NAME, getType(ElasticFieldType.TEXT.getName()));
		message.put(DATE_FIELD_NAME, getType(ElasticFieldType.DATE.getName()));
		message.put(FLOAT_FIELD_NAME, getType(ElasticFieldType.FLOAT.getName()));
		final Response response = root.path(getMappingPath()).request()
		        .put(Entity.json(new ElasticIndexMapping(message)));

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Failed to put the mapping: %s, status: %s", MAPPING_NAME, response.getStatus()));
		}
	}

	public void postType( final ElasticSearchPerformanceTrialRequestResource request ) {
		final Entity<?> requestBody = Entity.json(request);
		final WebTarget url = root.path(getTypePath());

		// Using the elastic search ID auto-generation, so we're using a post not a put
		final Response response = url.request(MediaType.APPLICATION_JSON).post(requestBody);

		if (response.getStatus() != 201) {
			throw new ElasticException(
			        String.format("Expecting a HTTP 201 instead receieved HTTP %s, URL: %s, body: %s",
			                response.getStatus(), url, requestBody));
		}

		final ElasticPostEventResponse eventResponse = response.readEntity(ElasticPostEventResponse.class);

		if (isInvalidResponse(eventResponse)) {
			throw new ElasticException(String.format("Unexpected response: %s, to request URL: %s, body: %s",
			        eventResponse, url, requestBody));
		}
	}

	public void postTypes( final List<?> request ) {
		final Entity<?> requestBody = Entity.json(request);
		final WebTarget url = bulkApiRoot.path(getTypePath()).path("_bulk");

		// Bulk API uses only HTTP POST for all operations
		final Response response = url.request(APPLICATION_NDJSON_TYPE).post(requestBody);

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Expecting a HTTP 200 instead receieved HTTP %s, URL: %s, body: %s",
			                response.getStatus(), url, requestBody));
		}

		final ElasticBulkApiResponseResource eventResponse = response
		        .readEntity(ElasticBulkApiResponseResource.class);

		if (isInvalidResponse(eventResponse)) {
			throw new ElasticException(String.format("Unexpected response: %s, to request URL: %s, body: %s",
			        eventResponse, url, requestBody));
		}
	}

	public void disableIndexRefresh() {
		final Response response = root.path(INDEX_NAME).path(ElasticSearchFields.SETTINGS).request()
		        .put(getDisableRefreshRequestBody());

		if (response.getStatus() != 200) {
			throw new ElasticException(
			        String.format("Failed to put the disable index settings to index: %s, status: %s", INDEX_NAME,
			                response.getStatus()));
		}
	}

	public void enableIndexRefresh() {
		final Response response = root.path(INDEX_NAME).path(ElasticSearchFields.SETTINGS).request()
		        .put(getEnableRefreshRequestBody());

		if (response.getStatus() != 200) {
			throw new ElasticException(String.format("Failed to put the enable index settings to index: %s, status: %s",
			        INDEX_NAME, response.getStatus()));
		}
	}

	private Entity<?> getIndexRequestBody() {
		return Entity.json(new ElasticIndex(numberOfShards, numberOfReplicas));
	}

	private Entity<?> getDisableRefreshRequestBody() {
		return Entity.json(new ElasticIndexSettingsResource(INDEX_SETTING_DISABLE_REFRESH));
	}

	private Entity<?> getEnableRefreshRequestBody() {
		return Entity.json(new ElasticIndexSettingsResource(INDEX_SETTING_DEFAULT_REFRESH));
	}

	private String getMappingPath() {
		return String.format("%s/_mapping/%s/", INDEX_NAME, MAPPING_NAME);
	}

	private String getTypePath() {
		return String.format("%s/%s/", INDEX_NAME, MAPPING_NAME);
	}

	private Map.Entry<String, String> getType( final String field ) {
		return new SimpleEntry<String, String>(TYPE, field);
	}

	private boolean isInvalidResponse( final ElasticBulkApiResponseResource eventResponse ) {
		return eventResponse.hasErrors();
	}

	private boolean isInvalidResponse( final ElasticPostEventResponse eventResponse ) {
		return !isValidResponse(eventResponse);
	}

	private boolean isValidResponse( final ElasticPostEventResponse eventResponse ) {
		return eventResponse.isCreated() && eventResponse.isResultCreated()
		        && StringUtils.equals(INDEX_NAME, eventResponse.getIndex())
		        && StringUtils.equals(MAPPING_NAME, eventResponse.getType());
	}
}