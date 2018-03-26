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
package com.systematic.trading.signals.data.api.quandl.configuration;

import com.systematic.trading.data.api.configuration.EquityApiConfiguration;

/**
 * Configuration data for the Quandl service.
 * 
 * @author CJ Hare
 */
public class QuandlConfiguration implements EquityApiConfiguration {

	private final int maximumConcurrentConnections;
	private final int maximumConnectionsPerSecond;
	private final int maximumMonthsPerConnection;

	private final QuandlConnectionConfiguration connectionConfiguration;

	public QuandlConfiguration(
	        final QuandlConnectionConfiguration connectionConfiguration,
	        final int maximumConcurrentConnections,
	        final int maximumConnectionsPerSecond,
	        final int maximumMonthsPerConnection ) {

		this.connectionConfiguration = connectionConfiguration;
		this.maximumConcurrentConnections = maximumConcurrentConnections;
		this.maximumConnectionsPerSecond = maximumConnectionsPerSecond;
		this.maximumMonthsPerConnection = maximumMonthsPerConnection;
	}

	@Override
	public String endpoint() {

		return connectionConfiguration.endpoint();
	}

	@Override
	public String apiKey() {

		return connectionConfiguration.apiKey();
	}

	@Override
	public int numberOfRetries() {

		return connectionConfiguration.numberOfRetries();
	}

	@Override
	public int retryBackOffMs() {

		return connectionConfiguration.retryBackOffMs();
	}

	@Override
	public int maximumRetrievalTimeSeconds() {

		return connectionConfiguration.maximumRetrievalTimeSeconds();
	}

	@Override
	public int maximumConcurrentConnections() {

		return maximumConcurrentConnections;
	}

	@Override
	public int maximumConnectionsPerSecond() {

		return maximumConnectionsPerSecond;
	}

	@Override
	public int maximumMonthsPerConnection() {

		return maximumMonthsPerConnection;
	}
}
