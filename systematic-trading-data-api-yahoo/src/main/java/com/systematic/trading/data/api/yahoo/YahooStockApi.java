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
package com.systematic.trading.data.api.yahoo;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.model.price.ClosingPrice;
import com.systematic.trading.model.price.HighestPrice;
import com.systematic.trading.model.price.LowestPrice;
import com.systematic.trading.model.price.OpeningPrice;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.model.price.impl.TradingDayPricesImpl;

public class YahooStockApi implements EquityApi {

	private static final Logger LOG = LogManager.getLogger(YahooStockApi.class);

	private static final String API_PART_ONE = "http://query.yahooapis.com/v1/public/yql?q=select%20Date,Open,High,Low,Close%20from%20yahoo.finance.historicaldata%20where%20symbol=%22";
	private static final String API_PART_TWO = "%22%20and%20startDate=%22";
	private static final String API_PART_THREE = "%22%20and%20endDate=%22";
	private static final String API_PART_FOUR = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

	private static final int NUMBER_CONCURRENT_CONNECTIONS = 1;
	private static final int MAXIMUM_RETRIEVAL_TIME = 5000;
	private static final int MAXIMUM_CONNECTION_PER_SECOND = 10;

	// Dividend API
	// http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.dividendhistory%20where%20symbol=%22VGS.AX%22%20and%20startDate=%222015-01-01%22%20and%20endDate=%222015-02-01%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys
	@Override
	public TradingDayPrices[] stockData(
	        final String dataset,
	        final String tickerSymbol,
	        final LocalDate startDateInclusive,
	        final LocalDate endDateExclusive,
	        final BlockingEventCount throttler ) throws CannotRetrieveDataException {

		final String uri = jsonUrl(tickerSymbol, startDateInclusive, endDateExclusive);
		logRequest(tickerSymbol, uri);

		throttler.add();
		final String json = HTTP_UTILS.get(uri);

		return parseJson(tickerSymbol, json);
	}

	@Override
	public Period maximumDurationPerConnection() {

		return Period.ofYears(1);
	}

	@Override
	public int maximumConcurrentConnections() {

		return NUMBER_CONCURRENT_CONNECTIONS;
	}

	@Override
	public int maximumRetrievalTimeSeconds() {

		return MAXIMUM_RETRIEVAL_TIME;
	}

	@Override
	public int maximumConnectionsPerSecond() {

		return MAXIMUM_CONNECTION_PER_SECOND;
	}

	private static final HttpUtil HTTP_UTILS = new HttpUtil();

	private String jsonUrl( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate )
	        throws CannotRetrieveDataException {

		try {
			final StringBuilder query = new StringBuilder();
			query.append(API_PART_ONE);
			query.append(URLEncoder.encode(tickerSymbol, StandardCharsets.UTF_8.name()));
			query.append(API_PART_TWO);
			query.append(startDate.toString());
			query.append(API_PART_THREE);
			query.append(endDate.minus(Period.ofDays(1)).toString());
			query.append(API_PART_FOUR);
			return query.toString();
		} catch (final UnsupportedEncodingException e) {
			throw new CannotRetrieveDataException("URL encoding failed", e);
		}
	}

	private TradingDayPrices[] parseJson( final String tickerSymbol, final String result )
	        throws CannotRetrieveDataException {

		List<TradingDayPrices> data = new ArrayList<>();

		try {
			final JSONObject json = new JSONObject(result);
			final JSONObject query = json.getJSONObject("query");
			final int numberOfQuotes = query.getInt("count");

			logResponse(numberOfQuotes, tickerSymbol);

			switch (numberOfQuotes) {
				case 0:
					// No parsing possible, as there are no results
					break;
				case 1:
					data = parseQuoteAsJsonObject(data, query, tickerSymbol);
					break;
				default:
					data = parseQuoteAsJsonArray(data, query, numberOfQuotes, tickerSymbol);
					break;
			}

		} catch (final JSONException e) {
			final String message = String.format("Failed in parsing JSON for: %s", tickerSymbol);
			logParsingFailure(message, e, result);
			throw new CannotRetrieveDataException(message, e);
		}

		return data.toArray(new TradingDayPrices[0]);
	}

	private List<TradingDayPrices> parseQuoteAsJsonObject(
	        List<TradingDayPrices> data,
	        final JSONObject query,
	        final String tickerSymbol ) {

		final JSONObject result = query.getJSONObject("results");
		data.add(parseQuote(tickerSymbol, result.getJSONObject("quote")));
		return data;
	}

	private List<TradingDayPrices> parseQuoteAsJsonArray(
	        List<TradingDayPrices> data,
	        final JSONObject query,
	        final int numberOfQuotes,
	        final String tickerSymbol ) {

		final JSONObject results = query.getJSONObject("results");
		final JSONArray quote = results.getJSONArray("quote");
		for (int i = 0; i < numberOfQuotes; i++) {
			data.add(parseQuote(tickerSymbol, quote.getJSONObject(i)));
		}

		return data;
	}

	private TradingDayPricesImpl parseQuote( final String tickerSymbol, final JSONObject quote ) {

		final String unparseDdate = quote.getString("Date");
		final LocalDate date = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(unparseDdate));
		final ClosingPrice closingPrice = ClosingPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Close")));
		final LowestPrice lowestPrice = LowestPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Low")));
		final HighestPrice highestPrice = HighestPrice.valueOf(BigDecimal.valueOf(quote.getDouble("High")));
		final OpeningPrice openingPrice = OpeningPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Open")));

		return new TradingDayPricesImpl(tickerSymbol, date, openingPrice, lowestPrice, highestPrice, closingPrice);
	}

	private void logRequest( final String tickerSymbol, final String uri ) {

		LOG.info("{} API call to: {}", tickerSymbol, uri);
	}

	private void logResponse( final int numberOfQuotes, final String tickerSymbol ) {

		LOG.info("{} data points returned for ticker symbol {}", numberOfQuotes, tickerSymbol);
	}

	private void logParsingFailure( final String message, final JSONException e, final String result ) {

		LOG.error(message, e);
		LOG.error(result);
	}
}
