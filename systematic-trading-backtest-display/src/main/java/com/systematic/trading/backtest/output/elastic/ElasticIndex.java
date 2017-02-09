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
package com.systematic.trading.backtest.output.elastic;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

/**
 * An Object structure representation of an ElasticSearch index.
 * 
 * @author CJ Hare
 */
public class ElasticIndex {

	private static final String PROPERTIES = "properties";
	private static final String TYPE = "type";

	/** Top level 'mappings' key for the index. */
	private final Map<String, Object> mappings;

	//TODO convert the type into an enum
	//TODO multiple fields? 
	public ElasticIndex(final String mappingName, final String fieldName, final String fieldType) {

		mappings = new HashMap<>();
		final Map<String, Object> properties = new HashMap<>();

		final Map<String, Object> message = new HashMap<>();
		final SimpleEntry<String, String> messageType = new SimpleEntry<String, String>(TYPE, fieldType);
		message.put(fieldName, messageType);

		properties.put(PROPERTIES, message);
		mappings.put(mappingName, properties);
	}

	/**
	 * Object structure representation of an ElasticSearch index.
	 * 
	 * @return ready to convert to JSON and PUT into ElasticSearch IndexAPI.
	 */
	public Map<String, Object> getMappings() {
		return mappings;
	}
}