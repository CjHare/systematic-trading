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
package com.systematic.trading.backtest.output.elastic.serialize;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiMetaDataRequestResource;

/**
 * Serializer for application/x-ndjson (JSON with \n for a element separator)
 * 
 * @author CJ Hare
 */
public class ElasticSearchBulkApiMetaDataSerializer extends StdSerializer<ElasticBulkApiMetaDataRequestResource> {

	/** Classes serial ID. */
	private static final long serialVersionUID = 1L;

	/** Meta identifier for an Elastic Search index. */
	private static final String INDEX = "_index";

	/** Meta identifier for an Elastic Search type. */
	private static final String TYPE = "_type";

	/** Meta identifier for an Elastic Search id. */
	private static final String ID = "_id";

	public ElasticSearchBulkApiMetaDataSerializer() {
		super(ElasticBulkApiMetaDataRequestResource.class, false);
	}

	@Override
	public void serialize( final ElasticBulkApiMetaDataRequestResource value, final JsonGenerator gen,
	        final SerializerProvider provider ) throws IOException {

		if (value == null) {
			return;
		}

		gen.writeStartObject();
		gen.writeFieldName(value.action());
		writeMeta(value, gen);
		gen.writeEndObject();
	}

	private void writeMeta( final ElasticBulkApiMetaDataRequestResource value, final JsonGenerator gen )
	        throws IOException {

		gen.writeStartObject();

		if (StringUtils.isNotBlank(value.index())) {
			gen.writeStringField(INDEX, value.index());
		}

		if (StringUtils.isNotBlank(value.type())) {
			gen.writeStringField(TYPE, value.type());
		}

		if (StringUtils.isNotBlank(value.id())) {
			gen.writeStringField(ID, value.id());
		}

		gen.writeEndObject();
	}
}