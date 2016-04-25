/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.signals.yahoo;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
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

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestEquityPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.data.price.OpeningPrice;
import com.systematic.trading.data.stock.api.StockApi;
import com.systematic.trading.data.stock.api.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.yahoo.data.TradingDayPricesImpl;
import com.systematic.trading.signals.yahoo.util.HttpUtil;

public class YahooStockApi implements StockApi {
	private static final Logger LOG = LogManager.getLogger(YahooStockApi.class);

	private static final String API_PART_ONE = "http://query.yahooapis.com/v1/public/yql?q=select%20Date,Open,High,Low,Close%20from%20yahoo.finance.historicaldata%20where%20symbol=%22";
	private static final String API_PART_TWO = "%22%20and%20startDate=%22";
	private static final String API_PART_THREE = "%22%20and%20endDate=%22";
	private static final String API_PART_FOUR = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

	// Dividend API
	//	http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.dividendhistory%20where%20symbol=%22VGS.AX%22%20and%20startDate=%222015-01-01%22%20and%20endDate=%222015-02-01%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys

	private static final HttpUtil HTTP_UTILS = new HttpUtil();

	private String getJsonUrl( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate )
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
		List<TradingDayPrices> data = new ArrayList<TradingDayPrices>();

		try {
			final JSONObject json = new JSONObject(result);
			final JSONObject query = json.getJSONObject("query");
			final int numberOfQuotes = query.getInt("count");

			LOG.info(String.format("%s data points returned for ticker symbol %s", numberOfQuotes, tickerSymbol));

			final JSONObject results;

			switch (numberOfQuotes) {
				// No parsing possible with no results
				case 0:
				break;
				// Single result, parse as JSON object
				case 1:
					results = query.getJSONObject("results");
					data.add(parseQuote(tickerSymbol, results.getJSONObject("quote")));
				break;
				// Two or more results, parse as JSON array
				default:
					results = query.getJSONObject("results");
					final JSONArray quote = results.getJSONArray("quote");
					for (int i = 0; i < numberOfQuotes; i++) {
						data.add(parseQuote(tickerSymbol, quote.getJSONObject(i)));
					}
				break;
			}

		} catch (final JSONException | ParseException e) {
			final String message = String.format("Failed in parsing JSON for: %s", tickerSymbol);
			LOG.error(message, e);
			LOG.error(result);
			throw new CannotRetrieveDataException(message, e);
		}

		return data.toArray(new TradingDayPrices[0]);
	}

	private TradingDayPricesImpl parseQuote( final String tickerSymbol, final JSONObject quote ) throws ParseException {

		final String unparseDdate = quote.getString("Date");
		final LocalDate date = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(unparseDdate));
		final ClosingPrice closingPrice = ClosingPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Close")));
		final LowestPrice lowestPrice = LowestPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Low")));
		final HighestEquityPrice highestPrice = HighestEquityPrice.valueOf(BigDecimal.valueOf(quote.getDouble("High")));
		final OpeningPrice openingPrice = OpeningPrice.valueOf(BigDecimal.valueOf(quote.getDouble("Open")));

		return new TradingDayPricesImpl(tickerSymbol, date, openingPrice, lowestPrice, highestPrice, closingPrice);
	}

	@Override
	public TradingDayPrices[] getStockData( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		final String uri = getJsonUrl(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
		LOG.info(String.format("%s API call to: %s", tickerSymbol, uri));

		final String json = HTTP_UTILS.httpGet(uri);

		return parseJson(tickerSymbol, json);
	}

	@Override
	public Period getMaximumDurationInSingleUpdate() {
		return Period.ofYears(1);
	}
}
