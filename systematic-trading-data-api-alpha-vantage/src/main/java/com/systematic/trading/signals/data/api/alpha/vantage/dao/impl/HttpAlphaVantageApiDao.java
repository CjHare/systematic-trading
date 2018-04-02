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
package com.systematic.trading.signals.data.api.alpha.vantage.dao.impl;

import java.time.LocalDate;
import java.util.Optional;
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
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.signals.data.api.alpha.vantage.converter.AlphaVantageResponseConverter;
import com.systematic.trading.signals.data.api.alpha.vantage.dao.AlphaVantageApiDao;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.AlphaVantageResponseResource;

/**
 * HttpAlphaVantageApiDao retrieves time series data from the Alpha Vantage API.
 * 
 * @author CJ Hare
 */
public class HttpAlphaVantageApiDao implements AlphaVantageApiDao {

	/** Class' little logger, */
	private static final Logger LOG = LogManager.getLogger(HttpAlphaVantageApiDao.class);

	private static final String PATH = "query";

	private static final String FUNCTION_SIZE_KEY = "function";
	private static final String FUNCTION_SIZE_VALUE = "TIME_SERIES_DAILY";

	private static final String OUTPUT_SIZE_KEY = "outputsize";
	private static final String OUTPUT_SIZE_VALUE = "full";

	private static final String TICKER_SYMBOL_KEY = "symbol";
	private static final String API_KEY = "apikey";

	private static final int HTTP_OK = 200;

	/** Converts the resource into the Systematic Trading domain object. */
	private final AlphaVantageResponseConverter converter;

	/** Base one, number of attempts per a Alpha Vantage call. */
	private final int numberOfRetries;

	/** Staggered wait time between retry attempts. */
	private final int retryBackoffMs;

	/** Base of the Restful end point. */
	private final WebTarget root;

	/** User key for accessing the Quandl end point. */
	private final String apiKey;

	public HttpAlphaVantageApiDao(
	        final EquityApiConfiguration configuration,
	        final AlphaVantageResponseConverter converter ) {

		this.numberOfRetries = configuration.numberOfRetries();
		this.retryBackoffMs = configuration.retryBackOffMs();

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(configuration.endpoint());

		this.apiKey = configuration.apiKey();
		this.converter = converter;
	}

	@Override
	public TradingDayPrices[] get(
	        final String equityDataset,
	        final String tickerSymbol,
	        final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate,
	        final BlockingEventCount throttler ) throws CannotRetrieveDataException {

		final WebTarget url = url(equityDataset, tickerSymbol);
		final Response response = get(url, throttler);
		final AlphaVantageResponseResource resource = response.readEntity(AlphaVantageResponseResource.class);

		errorCheck(url, resource);

		return converter.convert(tickerSymbol, inclusiveStartDate, exclusiveEndDate, resource.dataset());
	}

	private void errorCheck( final WebTarget url, final AlphaVantageResponseResource resource )
	        throws CannotRetrieveDataException {

		final Optional<String> error = resource.error();
		if (error.isPresent()) { throw new CannotRetrieveDataException(
		        String.format("Get call failed: %s%n%s", url, error.get())); }
	}

	// TODO duplicate code with HttpQuandlApiDao -> utility

	private WebTarget url( final String equityDataset, final String tickerSymbol ) {

		return root.path(PATH).queryParam(FUNCTION_SIZE_KEY, FUNCTION_SIZE_VALUE)
		        .queryParam(OUTPUT_SIZE_KEY, OUTPUT_SIZE_VALUE)
		        .queryParam(TICKER_SYMBOL_KEY, String.format("%s.%s", tickerSymbol, equityDataset))
		        .queryParam(API_KEY, apiKey);
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
				LOG.warn(
				        String.format(
				                "Failed to retrieve data, HTTP code: %s, request: %s",
				                response.getStatus(),
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
