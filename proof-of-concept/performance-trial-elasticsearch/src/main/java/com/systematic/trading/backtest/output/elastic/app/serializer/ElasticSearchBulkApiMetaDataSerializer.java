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
package com.systematic.trading.backtest.output.elastic.app.serializer;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.systematic.trading.backtest.output.elastic.app.ElasticSearchFields;
import com.systematic.trading.backtest.output.elastic.app.resource.ElasticSearchBulkApiMetaDataResource;

/**
 * Serializer for application/x-ndjson (JSON with \n for a element separator)
 * 
 * @author CJ Hare
 */
public class ElasticSearchBulkApiMetaDataSerializer extends StdSerializer<ElasticSearchBulkApiMetaDataResource> {

	/** Classes serial ID. */
	private static final long serialVersionUID = 1L;

	public ElasticSearchBulkApiMetaDataSerializer() {
		super(ElasticSearchBulkApiMetaDataResource.class, false);
	}

	@Override
	public void serialize( final ElasticSearchBulkApiMetaDataResource value, final JsonGenerator gen,
	        final SerializerProvider provider ) throws IOException {

		gen.writeStartObject();
		gen.writeFieldName(value.getAction());
		writeMeta(value, gen);
		gen.writeEndObject();
	}

	private void writeMeta( final ElasticSearchBulkApiMetaDataResource value, final JsonGenerator gen )
	        throws IOException {
		gen.writeStartObject();

		if (StringUtils.isNotBlank(value.getIndex())) {
			gen.writeStringField(ElasticSearchFields.INDEX, value.getIndex());
		}

		if (StringUtils.isNotBlank(value.getType())) {
			gen.writeStringField(ElasticSearchFields.TYPE, value.getType());
		}

		if (StringUtils.isNotBlank(value.getId())) {
			gen.writeStringField(ElasticSearchFields.ID, value.getType());
		}

		gen.writeEndObject();
	}
}