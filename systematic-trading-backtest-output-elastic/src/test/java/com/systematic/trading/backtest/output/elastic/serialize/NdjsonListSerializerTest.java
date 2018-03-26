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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Verifying behaviour of the NdjsonListSerializer.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class NdjsonListSerializerTest {

	/** NDJSON is JSON (non-pretty printed) with a new line delimiter after each line. */
	private static final String NEW_LINE_DELIMITER = "\n";

	@Mock
	private JsonGenerator gen;

	@Mock
	private SerializerProvider provider;

	/** Serializer instance being tested. */
	private NdjsonListSerializer serializer;

	@Before
	public void setUp() {

		serializer = new NdjsonListSerializer();
	}

	@Test
	public void serializeNullArray() throws IOException {

		serialize(null);

		verifyNoJson();
	}

	@Test
	public void serializeEmptyArray() throws IOException {

		final List<String> values = setUpValues();

		serialize(values);

		verifyNoJson();
	}

	@Test
	public void serializeSingleEntry() throws IOException {

		final List<String> values = setUpValues("the_only_value");

		serialize(values);

		verifyJson("the_only_value");
	}

	@Test
	public void serializeMultipleEntries() throws IOException {

		final List<String> values = setUpValues("first_value", "second_value", "third_value");

		serialize(values);

		verifyJson("first_value", "second_value", "third_value");
	}

	private List<String> setUpValues( final String... values ) {

		final List<String> setUp = new ArrayList<>();

		for (final String value : values) {
			setUp.add(value);
		}

		return setUp;
	}

	private void serialize( final List<?> values ) throws IOException {

		serializer.serialize(values, gen, provider);
	}

	private void verifyJson( final String... expexted ) throws IOException {

		for (String o : expexted) {
			verify(gen).writeObject(o);
		}

		verify(gen, times(expexted.length)).writeRawValue(NEW_LINE_DELIMITER);
		verifyNoMoreInteractions(gen);
	}

	private void verifyNoJson() {

		verifyZeroInteractions(gen);
	}
}
