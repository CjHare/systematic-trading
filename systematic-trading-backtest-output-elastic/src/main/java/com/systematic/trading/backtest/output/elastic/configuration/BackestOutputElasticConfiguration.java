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
package com.systematic.trading.backtest.output.elastic.configuration;

/**
 * General configuration data useful for the output of back test data to Elastic search.
 * 
 * @author CJ Hare
 */
public interface BackestOutputElasticConfiguration {

	/**
	 * The number of primary shards that an index should have, which defaults to 5. 
	 * This setting cannot be changed after index creation.
	 * 
	 * @return number of Elastic search nodes to share the data across.
	 */
	int numberOfShards();

	/**
	 * The number of replica shards (copies) that each primary shard should have, which defaults to 1. 	
	 * 
	 * @return number of Elastic search replications for each index.
	 */
	int numberOfReplicas();

	/**
	 * Retrieve the number of records to send to the Elastic Search bulk API.
	 * 
	 * @return maximum number of records to send to the bulk API.
	 */
	int bulkApiQueueSize();

	/**
	 * Number of concurrent connections to establish to Elastic Search.
	 * 
	 * @return number of threads for outputting data to Elastic Search.
	 */
	int numberOfConnections();
}