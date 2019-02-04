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

import javax.ws.rs.client.WebTarget;

import com.systematic.trading.signals.data.api.alpha.vantage.dao.AlphaVantageApiFormatter;

/**
 * HttpAlphaVantageApiDigitalCurrencyDaily formats the HTTP request for retrieving data from the
 * daily digital currency to fiat currency API.
 * 
 * @author CJ Hare
 */
public class HttpAlphaVantageApiFormatterDigitalCurrencyDaily implements AlphaVantageApiFormatter {

	private static final String PATH = "query";

	private static final String FUNCTION_SIZE_KEY = "function";
	private static final String FUNCTION_SIZE_VALUE = "DIGITAL_CURRENCY_DAILY";

	private static final String TICKER_SYMBOL_KEY = "symbol";
	private static final String MARKET_KEY = "market";

	private static final String API_KEY = "apikey";

	@Override
	public WebTarget url(
	        final WebTarget contextRoot,
	        final String tickerDataset,
	        final String tickerSymbol,
	        final String apiKey ) {

		return contextRoot.path(PATH).queryParam(FUNCTION_SIZE_KEY, FUNCTION_SIZE_VALUE)
		        .queryParam(TICKER_SYMBOL_KEY, tickerSymbol).queryParam(MARKET_KEY, tickerDataset)
		        .queryParam(API_KEY, apiKey);
	}
}
