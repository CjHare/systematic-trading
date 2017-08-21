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

import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.DATE_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.FLOAT_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.INDEX_NAME;
import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.MAPPING_NAME;
import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.TEXT_FIELD_NAME;
import static com.systematic.trading.backtest.output.elastic.app.ElasticSearchPerformanceTrialFields.TYPE;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.backtest.output.elastic.app.configuration.ElasticSearchConfiguration;
import com.systematic.trading.backtest.output.elastic.app.resource.ElasticSearchPerformanceTrialResource;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.index.ElasticPostEventResponse;

/**
 * Facade to Elastic Search, abstracting the index and mappings.
 * 
 * Facilitates posting to a single index with mapping, with a text, float and date field, mirroring the structure 
 * and field types that will be used for a back test run.
 * 
 * @author CJ Hare
 */
public class ElasticSearchFacade {

	/** Base of the elastic search Restful end point. */
	private final WebTarget root;

	private final ElasticSearchConfiguration configuration;

	public ElasticSearchFacade( final ElasticSearchConfiguration elasticConfig ) {

		// Registering the provider for POJO -> JSON
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final JacksonJsonProvider provider = new JacksonJsonProvider();
		provider.setMapper(mapper);
		final ClientConfig config = new ClientConfig(provider);

		// End point target root
		this.root = ClientBuilder.newClient(config).target(elasticConfig.getEndpoint());

		this.configuration = elasticConfig;
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

	public void postType( final ElasticSearchPerformanceTrialResource request ) {
		final Entity<?> requestBody = Entity.json(request);
		final WebTarget url = root.path(getTypePath());

		// Using the elastic search ID auto-generation, so we're using a post not a put
		final Response response = url.request().post(requestBody);

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

	private Entity<?> getIndexRequestBody() {
		return Entity.json(new ElasticIndex(configuration.getNumberOfShards(), configuration.getNumberOfReplicas()));
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

	private boolean isInvalidResponse( final ElasticPostEventResponse eventResponse ) {
		return !isValidResponse(eventResponse);
	}

	private boolean isValidResponse( final ElasticPostEventResponse eventResponse ) {
		return eventResponse.isCreated() && eventResponse.isResultCreated()
		        && StringUtils.equals(INDEX_NAME, eventResponse.getIndex())
		        && StringUtils.equals(MAPPING_NAME, eventResponse.getType());
	}
}