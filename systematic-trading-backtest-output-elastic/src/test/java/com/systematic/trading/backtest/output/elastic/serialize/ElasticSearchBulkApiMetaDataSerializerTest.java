/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiMetaDataRequestResource;

/**
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchBulkApiMetaDataSerializerTest {

	/** Meta identifier for an Elastic Search index. */
	private static final String INDEX = "_index";

	/** Meta identifier for an Elastic Search type. */
	private static final String TYPE = "_type";

	/** Meta identifier for an Elastic Search id. */
	private static final String ID = "_id";

	@Mock
	private JsonGenerator gen;

	@Mock
	private SerializerProvider provider;

	/** Serializer instance being tested. */
	private ElasticSearchBulkApiMetaDataSerializer serializer;

	@Before
	public void setUp() {

		serializer = new ElasticSearchBulkApiMetaDataSerializer();
	}

	@Test
	public void serializeNullArray() throws IOException {

		serialize(null);

		verifyNoJson();
	}

	@Test
	public void serializeAction() throws IOException {

		final ElasticBulkApiMetaDataRequestResource value = new ElasticBulkApiMetaDataRequestResource("action");

		serialize(value);

		verifyJson("action");
	}

	@Test
	public void serializeActionIndex() throws IOException {

		final ElasticBulkApiMetaDataRequestResource value = new ElasticBulkApiMetaDataRequestResource("action", "index",
		        null, null);

		serialize(value);

		verifyJson("action", "index");
	}

	@Test
	public void serializeActionIndexType() throws IOException {

		final ElasticBulkApiMetaDataRequestResource value = new ElasticBulkApiMetaDataRequestResource("action", "index",
		        "type", null);

		serialize(value);

		verifyJson("action", "index", "type");
	}

	@Test
	public void serializeActionIndexTypeId() throws IOException {

		final ElasticBulkApiMetaDataRequestResource value = new ElasticBulkApiMetaDataRequestResource("action", "index",
		        "type", "Id");

		serialize(value);

		verifyJson("action", "index", "type", "Id");
	}

	private void serialize( final ElasticBulkApiMetaDataRequestResource value ) throws IOException {

		serializer.serialize(value, gen, provider);
	}

	private void verifyNoJson() {

		verifyZeroInteractions(gen);
	}

	private void verifyJson( final String action ) throws IOException {

		verifyJson(action, null, null, null);
	}

	private void verifyJson( final String action, final String index ) throws IOException {

		verifyJson(action, index, null, null);
	}

	private void verifyJson( final String action, final String index, final String type ) throws IOException {

		verifyJson(action, index, type, null);
	}

	private void verifyJson( final String action, final String index, final String type, final String id )
	        throws IOException {

		InOrder order = inOrder(gen);
		order.verify(gen).writeStartObject();
		order.verify(gen).writeFieldName(action);
		order.verify(gen).writeStartObject();

		if (index != null) {
			order.verify(gen).writeStringField(INDEX, index);
		}

		if (type != null) {
			order.verify(gen).writeStringField(TYPE, type);
		}

		if (id != null) {
			order.verify(gen).writeStringField(ID, id);
		}

		order.verify(gen, times(2)).writeEndObject();
		verifyNoMoreInteractions(gen);
	}
}