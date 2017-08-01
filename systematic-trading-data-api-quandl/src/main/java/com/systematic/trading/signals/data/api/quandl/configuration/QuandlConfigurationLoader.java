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
package com.systematic.trading.signals.data.api.quandl.configuration;

import java.io.IOException;
import java.util.Properties;

import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.configuration.ConfigurationLoader;
import com.systematic.trading.data.configuration.KeyLoader;

/**
 * Deals with the loading and validation of the Quandl configuration.
 * 
 * @author CJ Hare
 */
public class QuandlConfigurationLoader {

	private static final String QUANDL_PROPERTIES_FILE = "quandl.properties";
	private static final String QUANDL_API_KEY_FILE = "quandl.key";

	public EquityApiConfiguration load() throws IOException {
		final String apiKey = new KeyLoader().load(QUANDL_API_KEY_FILE);
		final Properties quandlProperties = new ConfigurationLoader().load(QUANDL_PROPERTIES_FILE);
		final String endpoint = quandlProperties.getProperty("endpoint");
		final int numberOfRetries = Integer.parseInt(quandlProperties.getProperty("number_of_retries"));
		final int retryBackOffMs = Integer.parseInt(quandlProperties.getProperty("retry_backoff_ms"));
		final int maximumRetrievalTimeSeconds = Integer
		        .parseInt(quandlProperties.getProperty("maximum_retrieval_time_seconds"));
		final int maximumConcurrentConnections = Integer
		        .parseInt(quandlProperties.getProperty("maximum_concurrent_connections"));
		final int maximumConnectionsPerSecond = Integer
		        .parseInt(quandlProperties.getProperty("maximum_connections_per_second"));
		final int maximumMonthsPerConnection = Integer
		        .parseInt(quandlProperties.getProperty("maximum_months_retrieved_per_connection"));

		//TODO validation of the required properties needed for each API

		return new QuandlConfiguration(endpoint, apiKey, numberOfRetries, retryBackOffMs, maximumRetrievalTimeSeconds,
		        maximumConcurrentConnections, maximumConnectionsPerSecond, maximumMonthsPerConnection);
	}
}