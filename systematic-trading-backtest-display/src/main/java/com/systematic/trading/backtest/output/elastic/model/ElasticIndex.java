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
package com.systematic.trading.backtest.output.elastic.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * An Object structure representation of an ElasticSearch index.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticIndex {

	/*
	"settings" : {
	    "index" : {
	        "number_of_shards" : 3,
	        "number_of_replicas" : 2
	    }
	}	  
	 */

	//TODO ? strucutre with a @JsonCreator to accept format of
	//TODO used when reading the index to verify structure is correct
	//{"signal-analysis":{"aliases":{},"mappings":{"signal-analysis":{"properties":{"event":{"type":"text"}}}},"settings":{"index":{"creation_date":"1487497092304","number_of_shards":"5","number_of_replicas":"1","uuid":"dUnk_vL9QbOR8J5hCmIgkg","version":{"created":"5010199"},"provided_name":"signal-analysis"}}}}

	/** Elastic key for the index properties. */
	private static final String PROPERTIES = "properties";

	/** Elastic key for the index type */
	private static final String TYPE = "type";

	/** Top level 'mappings' key for the index. */
	@JsonProperty("mappings")
	private final Map<String, Object> indexMapping;

	public ElasticIndex(final String indexTypeName, final Pair<ElasticFieldName, ElasticFieldType> field) {
		this(indexTypeName, Arrays.asList(field));
	}

	public ElasticIndex(final String indexTypeName, final List<Pair<ElasticFieldName, ElasticFieldType>> fields) {

		final Map<String, Object> message = new HashMap<>();

		for (final Pair<ElasticFieldName, ElasticFieldType> field : fields) {
			message.put(getName(field), getType(field));
		}

		final Map<String, Object> properties = new HashMap<>();
		properties.put(PROPERTIES, Collections.unmodifiableMap(message));

		final Map<String, Object> mappings = new HashMap<>();
		mappings.put(indexTypeName, Collections.unmodifiableMap(properties));
		indexMapping = Collections.unmodifiableMap(mappings);

	}

	/**
	 * Includes the type mapping with the additional level for the indexTypeName;
	 */
	public Map<String, Object> getIndexMapping() {
		return indexMapping;
	}

	private String getName( final Pair<ElasticFieldName, ElasticFieldType> field ) {
		return field.getLeft().getName();
	}

	private Map.Entry<String, String> getType( final Pair<ElasticFieldName, ElasticFieldType> field ) {
		return new SimpleEntry<String, String>(TYPE, field.getRight().getName());
	}
}