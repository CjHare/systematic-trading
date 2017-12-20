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
package com.systematic.trading.backtest.output.elastic.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The action and meta data required to accompany all source entries for the Bulk API.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_EMPTY)
public class ElasticBulkApiMetaDataRequestResource {

	/** Always need to have an action. */
	@JsonInclude(Include.ALWAYS)
	private final String action;

	/** Name of the elastic search index (optional). */
	private final String index;

	/** Name of the elastic search type (optional). */
	private final String type;

	/** Id of the document to act on (optional). */
	private final String id;

	public ElasticBulkApiMetaDataRequestResource( final String action, final String index, final String type,
	        final String id ) {
		this.action = action;
		this.index = index;
		this.type = type;
		this.id = id;
	}

	public ElasticBulkApiMetaDataRequestResource( final String action ) {
		this(action, null, null, null);
	}

	public String getAction() {

		return action;
	}

	public String getIndex() {

		return index;
	}

	public String getType() {

		return type;
	}

	public String getId() {

		return id;
	}
}