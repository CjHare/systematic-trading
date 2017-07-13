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
package com.systematic.trading.signals.data.api.quandl.dao;

import java.time.LocalDate;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Data Access Object for retrieving data from the Quandl API.
 * 
 * @author CJ Hare
 */
public class QuandlDao {

	private static final int HTTP_OK = 200;

	//TODO inject this! - configuration value
	/** Location of the quandl end point. */
	private static final String QUANDL_ENDPOINT_URL = "https://www.quandl.com";

	//TODO move this into a local configuration file i.e. don't have it commited in the repo.
	/** User specific key for accessing the Quandl API. */
	private static final String QUANDL_API_KEY = "dZYJxfPu4E5U5-S26KEv";

	/** Base of the Restful end point. */
	private final WebTarget root;

	//TODO use an exectuor pool for the Java-RS operations?
	// final ExecutorService pool

	public QuandlDao() {

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(QUANDL_ENDPOINT_URL);
	}

	public QuandlResponseResource get( final String symbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		final WebTarget url = createUrl();
		final Response response = url.request(MediaType.APPLICATION_JSON).get();

		verifyResponse(url, response);

		return response.readEntity(QuandlResponseResource.class);
	}

	private WebTarget createUrl() {
		final String path = "api/v3/datatables/WIKI/PRICES.json";
		return root.path(path).queryParam("qopts.columns", "date,open,high,low,close")
		        .queryParam("date.gte", "20150115").queryParam("date.lt", "20150401").queryParam("ticker", "AAPL")
		        .queryParam("api_key", QUANDL_API_KEY);
	}

	private void verifyResponse( final WebTarget url, final Response response ) throws CannotRetrieveDataException {
		if (response.getStatus() != HTTP_OK) {
			throw new CannotRetrieveDataException(
			        String.format("Failed to retrieve data, HTTP code: %s, request: %s", response.getStatus(), url));
		}
	}
}