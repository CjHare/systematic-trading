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
package com.systematic.trading.signals.data.api.quandl.dao.impl;

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
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlApiDao;
import com.systematic.trading.signals.data.api.quandl.resource.QuandlResponseResource;

/**
 * HTTP connection to the Quandl Tables API.
 * 
 * The tables API allows different parameters to the time-series API.
 * 
 * @author CJ Hare
 */
public class HttpQuandlTablesApiDao implements QuandlApiDao {

	private static final String PATH = "api/v3/datatables/WIKI/PRICES.json";
	private static final String COLUMN_NAMES_KEY = "qopts.columns";
	private static final String COLUMN_NAMES_VALUE = "date,open,high,low,close";
	private static final String START_DATE_KEY = "date.gte";
	private static final String END_DATE_KEY = "date.lt";
	private static final String TICKER_SYMBOL_KEY = "ticker";
	private static final String API_KEY = "api_key";
	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyMMdd");

	private static final Logger LOG = LogManager.getLogger(QuandlApiDao.class);
	private static final int HTTP_OK = 200;

	/** Base of the Restful end point. */
	private final WebTarget root;

	/** User key for accessing the Quandl end point.*/
	private final String apiKey;

	/** Base one, number of attempts per a Quandl call.*/
	private final int numberOfRetries;

	/** Staggered wait time between retry attempts.*/
	private final int retryBackoffMs;

	public HttpQuandlTablesApiDao( final EquityApiConfiguration configuration ) {

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(configuration.getEndpoint());

		this.apiKey = configuration.getApiKey();
		this.numberOfRetries = configuration.getNumberOfRetries();
		this.retryBackoffMs = configuration.getRetryBackOffMs();
	}

	@Override
	public QuandlResponseResource get( final String dataset, final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate, final BlockingEventCount throttler )
	        throws CannotRetrieveDataException {
		final WebTarget url = createUrl(tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		final Response response = get(url, throttler);

		return response.readEntity(QuandlResponseResource.class);
	}

	private WebTarget createUrl( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {
		return root.path(PATH).queryParam(COLUMN_NAMES_KEY, COLUMN_NAMES_VALUE)
		        .queryParam(START_DATE_KEY, inclusiveStartDate.format(QUANDL_DATE_FORMAT))
		        .queryParam(END_DATE_KEY, exclusiveEndDate.format(QUANDL_DATE_FORMAT))
		        .queryParam(TICKER_SYMBOL_KEY, tickerSymbol).queryParam(API_KEY, apiKey);
	}

	private Response get( final WebTarget url, final BlockingEventCount throttler ) throws CannotRetrieveDataException {
		int attempt = 1;

		do {
			LOG.info("Retrieving from {}", url);

			throttler.add();
			final Response response = url.request(MediaType.APPLICATION_JSON).get();

			if (isResponseOk(response)) {
				return response;

			} else {
				LOG.warn(String.format("Failed to retrieve data, HTTP code: %s, request: %s", response.getStatus(),
				        url));

				waitBackOffDuration(attempt);
			}

		} while (++attempt <= numberOfRetries);

		throw new CannotRetrieveDataException(String.format("Failed to retrieve data for request: %s", url));
	}

	private void waitBackOffDuration( final long attempt ) {
		try {
			TimeUnit.MILLISECONDS.sleep(attempt * retryBackoffMs);
		} catch (InterruptedException e) {
			LOG.warn("Wait between retrieval calls interrupted", e);

			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
	}

	private boolean isResponseOk( final Response response ) {
		return response.getStatus() == HTTP_OK;
	}
}