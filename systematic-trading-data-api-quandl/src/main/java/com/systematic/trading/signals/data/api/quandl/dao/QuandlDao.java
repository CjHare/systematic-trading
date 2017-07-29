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
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.WikisDatabase;
import com.systematic.trading.signals.data.api.quandl.configuration.QuandlConfiguration;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Data Access Object for retrieving data from the Quandl API.
 * 
 * DAO's responsibility is ensure the Quandl reply contains the expected JSON format, not the data integrity.
 * 
 * @author CJ Hare
 */
public class QuandlDao {

	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyMMdd");
	
	private static final Logger LOG = LogManager.getLogger(QuandlDao.class);
	private static final int HTTP_OK = 200;

	/** Base of the Restful end point. */
	private final WebTarget root;

	/** User key for accessing the Quandl end point.*/
	private final String apiKey;

	/** Base one, number of attempts per a Quandl call.*/
	private final int numberOfRetries;

	/** Staggered wait time between retry attempts.*/
	private final int retryBackoffMs;

	public QuandlDao( final QuandlConfiguration quandl ) {

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(quandl.getEndpoint());

		this.apiKey = quandl.getApiKey();
		this.numberOfRetries = quandl.getNumberOfRetries();
		this.retryBackoffMs = quandl.getRetryBackOffMs();
	}

	public QuandlResponseResource get( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		final WebTarget url = createUrl(tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		final Response response = get(url);

		return response.readEntity(QuandlResponseResource.class);
	}

	private WebTarget createUrl( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {
		return root.path(WikisDatabase.PATH)
		        .queryParam(WikisDatabase.COLUMN_NAMES_KEY, WikisDatabase.COLUMN_NAMES_VALUE)
		        .queryParam(WikisDatabase.START_DATE_KEY, inclusiveStartDate.format(QUANDL_DATE_FORMAT))
		        .queryParam(WikisDatabase.END_DATE_KEY, exclusiveEndDate.format(QUANDL_DATE_FORMAT))
		        .queryParam(WikisDatabase.TICKER_SYMBOL_KEY, tickerSymbol).queryParam(WikisDatabase.API_KEY, apiKey);
	}

	private Response get( final WebTarget url ) throws CannotRetrieveDataException {
		int attempt = 1;

		do {
			final Response response = url.request(MediaType.APPLICATION_JSON).get();

			if (isResponseOk(url, response)) {
				return response;
			} else {
				LOG.warn(String.format("Failed to retrieve data, HTTP code: %s, request: %s", response.getStatus(),
				        url));

				try {
					TimeUnit.MILLISECONDS.sleep(attempt * retryBackoffMs);
				} catch (InterruptedException e) {
				}
			}

		} while (++attempt <= numberOfRetries);

		throw new CannotRetrieveDataException(String.format("Failed to retrieve data forrequest: %s", url));
	}

	private boolean isResponseOk( final WebTarget url, final Response response ) {
		return response.getStatus() != HTTP_OK;
	}
}