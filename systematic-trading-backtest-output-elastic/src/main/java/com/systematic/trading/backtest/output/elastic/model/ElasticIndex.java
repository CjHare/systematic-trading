/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package com.systematic.trading.backtest.output.elastic.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Object structure representation of an ElasticSearch index.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticIndex {

	/**
	 * The number of primary shards that an index should have, which defaults to 5.
	 * This setting cannot be changed after index creation.
	 */
	private static final String NUMBER_OF_SHARDS = "number_of_shards";

	/** The number of replica shards (copies) that each primary shard should have, which defaults to 1. */
	private static final String NUMBER_OF_REPLICAS = "number_of_replicas";

	@JsonProperty("settings")
	private final Map<String, Object> settings;

	public ElasticIndex( final int numberOfShards, final int numberOfReplicas ) {

		final Map<String, Object> message = new HashMap<>();
		message.put(NUMBER_OF_SHARDS, numberOfShards);
		message.put(NUMBER_OF_REPLICAS, numberOfReplicas);

		settings = Collections.unmodifiableMap(message);
	}

	public Map<String, Object> settings() {

		return settings;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder("ElasticIndex [settings=");
		out.append(settings);
		out.append("]");
		return out.toString();
	}
}