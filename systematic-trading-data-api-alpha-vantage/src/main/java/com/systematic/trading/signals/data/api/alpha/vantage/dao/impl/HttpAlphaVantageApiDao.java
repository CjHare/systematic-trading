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
import com.systematic.trading.signals.data.api.alpha.vantage.dao.AlphaVantageApiFormatter;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.AlphaVantageTimeSeriesDailyResponseResource;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.AlphaVantageTimeSeriesDigitalCurrencyDailyAudResource;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.AlphaVantageTimeSeriesEntries;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.AlphaVantqgeErrorPossibleResponseResource;

/**
 * HttpAlphaVantageApiDao retrieves time series data from the Alpha Vantage API.
 * 
 * @author CJ Hare
 */
public class HttpAlphaVantageApiDao<T> implements AlphaVantageApiDao {

	/** Class' little logger, */
	private static final Logger LOG = LogManager.getLogger(HttpAlphaVantageApiDao.class);

	private static final int HTTP_OK = 200;

	/** Converts the resource into the Systematic Trading domain object. */
	private final AlphaVantageResponseConverter converter;

	/** Formatter for the URI of request to make to AlphaVantage. */
	private final AlphaVantageApiFormatter apiFormatter;

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
	        final AlphaVantageApiFormatter apiFormatter,
	        final AlphaVantageResponseConverter converter ) {

		this.numberOfRetries = configuration.numberOfRetries();
		this.retryBackoffMs = configuration.retryBackOffMs();

		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		this.root = ClientBuilder.newClient(clientConfig).target(configuration.endpoint());

		this.apiKey = configuration.apiKey();
		this.apiFormatter = apiFormatter;
		this.converter = converter;
	}

	@Override
	public TradingDayPrices[] get(
	        final String tickerDataset,
	        final String tickerSymbol,
	        final LocalDate startDateInclusive,
	        final LocalDate endDateExclusive,
	        final BlockingEventCount throttler ) throws CannotRetrieveDataException {

		final WebTarget url = apiFormatter.url(root, tickerDataset, tickerSymbol, apiKey);
		final Response response = get(url, throttler);

		final AlphaVantageTimeSeriesEntries resource;
		if ("AUD".equalsIgnoreCase(tickerDataset)) {
			resource = response.readEntity(AlphaVantageTimeSeriesDigitalCurrencyDailyAudResource.class);
		} else {
			resource = response.readEntity(AlphaVantageTimeSeriesDailyResponseResource.class);
		}

		errorCheck(url, resource);

		return converter.convert(tickerSymbol, startDateInclusive, endDateExclusive, resource.data());
	}

	private void errorCheck( final WebTarget url, final AlphaVantqgeErrorPossibleResponseResource resource )
	        throws CannotRetrieveDataException {

		final Optional<String> error = resource.error();
		if (error.isPresent()) {
			throw new CannotRetrieveDataException(String.format("Get call failed: %s%n%s", url, error.get()));
		}
	}

	// TODO duplicate code with HttpQuandlApiDao -> utility
	private Response get( final WebTarget url, final BlockingEventCount throttler ) throws CannotRetrieveDataException {

		int attempt = 1;

		do {
			logRequest(url);

			throttler.add();
			final Response response = url.request(MediaType.APPLICATION_JSON).get();

			if (isResponseOk(response)) {
				return response;

			} else {
				logRequestFailure(url, response);
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

	private void logRequest( final WebTarget url ) {

		LOG.debug("Retrieving from {}", url);
	}

	private void logRequestFailure( final WebTarget url, final Response response ) {

		LOG.warn("Failed to retrieve data, HTTP code: {}, request: {}", response.getStatus(), url);
	}
}
