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
package com.systematic.trading.backtest.output.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object from Elastic-search post of an entry to an index.
 * 
 * @author CJ Hare
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticPostEventResponse {

	private final String created;
	private final String id;
	private final String index;
	private final String result;
	private final String type;

	@JsonCreator
	public ElasticPostEventResponse( @JsonProperty("_index") final String index,
	        @JsonProperty("_type") final String type, @JsonProperty("_id") final String id,
	        @JsonProperty("result") final String result, @JsonProperty("created") final String created ) {
		this.created = created;
		this.id = id;
		this.index = index;
		this.result = result;
		this.type = type;
	}

	public String getCreated() {
		return created;
	}

	public String getId() {
		return id;
	}

	public String getIndex() {
		return index;
	}

	public String getResult() {
		return result;
	}

	public String getType() {
		return type;
	}

	public boolean isCreated() {
		return "true".equals(created);
	}

	public boolean isResultCreated() {
		return "created".equals(result);
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder("ElasticPostEventResponse [");
		out.append("created=");
		out.append(created);
		out.append(", id=");
		out.append(id);
		out.append(", index=");
		out.append(index);
		out.append(", result=");
		out.append(result);
		out.append(", type=");
		out.append(type);
		out.append("]");
		return out.toString();
	}
}