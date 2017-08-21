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
package com.systematic.trading.backtest.output.elastic.app.configuration;

/**
 * Configuration builder for Elastic Search.
 * 
 * @author CJ Hare
 */
public class ElasticSearchConfigurationBuilder {

	/** Location of the elastic search end point. */
	private static final String ELASTIC_ENDPOINT_URL = "http://localhost:9200";

	/** 
	 * The number of primary shards that an index should have, which defaults to 5. 
	 * This setting cannot be changed after index creation.
	 */
	private static final int DEFAULT_NUMBER_OF_SHARDS = 5;

	/** The number of replica shards (copies) that each primary shard should have, which defaults to 1. */
	private static final int DEFAULT_NUMBER_OF_REPLICAS = 1;

	private String endpoint;
	private Integer numberOfShards;
	private Integer numberOfReplicas;

	public ElasticSearchConfigurationBuilder withEndpoint( final String endpoint ) {
		this.endpoint = endpoint;
		return this;
	}

	public ElasticSearchConfigurationBuilder withShards( final int numberOfShards ) {
		this.numberOfShards = numberOfShards;
		return this;
	}

	public ElasticSearchConfigurationBuilder withReplicas( final int numberOfReplicas ) {
		this.numberOfReplicas = numberOfReplicas;
		return this;
	}

	public ElasticSearchConfiguration build() {
		return new ElasticSearchConfiguration(getEndpoint(), getNumberOfShards(), numberOfReplicas());
	}

	private String getEndpoint() {
		return endpoint == null ? ELASTIC_ENDPOINT_URL : endpoint;
	}

	private int getNumberOfShards() {
		return numberOfShards == null ? DEFAULT_NUMBER_OF_SHARDS : numberOfShards;
	}

	private int numberOfReplicas() {
		return numberOfReplicas == null ? DEFAULT_NUMBER_OF_REPLICAS : numberOfReplicas;
	}
}