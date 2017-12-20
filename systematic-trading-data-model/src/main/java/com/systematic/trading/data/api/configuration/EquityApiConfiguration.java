/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.data.api.configuration;

/**
 * General configuration data useful for an EquityApi.
 * 
 * @author CJ Hare
 */
public interface EquityApiConfiguration {

	/**
	 * The root location of the EquityApi endpoint.
	 */
	public String endpoint();

	/**
	 * The key required to access the service.
	 * 
	 * @return the API key or <code>null</code> when no key is required,
	 */
	public String apiKey();

	/**
	 * The number of times to retry a single call before failing.
	 * 
	 * @return maximum number of attempts for a single request, performed serially.
	 */
	public int numberOfRetries();

	/**
	 * Sleeping time between retry attempts, where every retry attempt the sleep time increases by
	 * back off amount.
	 * 
	 * e.g back off: 100 (ms) 1st retry sleep: 100 ms 2nd retry sleep: 200 ms 3rd retry sleep: 300
	 * ms
	 * 
	 * @return staggering sleep time between each retry attempt, in milliseconds.
	 */
	public int retryBackOffMs();

	/**
	 * The long amount of time to allow for a making a call to the API.
	 * 
	 * @return maximum amount of time until the retrieval attempt is failed, should include
	 *         timeouts, latency, retry attempts and backoffs.
	 */
	public int maximumRetrievalTimeSeconds();

	/**
	 * Maximum number of concurrent connections allowed to the API.
	 * 
	 * @return the number of simultaneous connections that will be used to retrieve data.
	 */
	public int maximumConcurrentConnections();

	/**
	 * Maximum number of connections to the API service allowed per a second.
	 * 
	 * @return the limit on the number of connections every rolling second.
	 */
	public int maximumConnectionsPerSecond();

	/**
	 * Limit the pay load that will be retrieve per a connection.
	 * 
	 * @return maximum number of months of data requested per a connection to the API.
	 */
	public int maximumMonthsPerConnection();
}