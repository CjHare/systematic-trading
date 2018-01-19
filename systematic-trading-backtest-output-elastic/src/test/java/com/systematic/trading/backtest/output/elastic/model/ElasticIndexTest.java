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
package com.systematic.trading.backtest.output.elastic.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies the object representation converts to the correct JSON.
 * 
 * @author CJ Hare
 */
public class ElasticIndexTest {

	/** Mapper instance being tested. */
	private ObjectMapper mapper;

	@Before
	public void setUp() {

		mapper = new ObjectMapper();
	}

	@Test
	public void jsonSingleField() throws JsonProcessingException {

		final int numberOfShards = 4;
		final int numberOfReplicas = 5;
		final ElasticIndex index = new ElasticIndex(numberOfShards, numberOfReplicas);

		final String json = write(index);

		verifyJson(numberOfShards, numberOfReplicas, json);
	}

	@Test
	public void jsonSingleFieldAlternativeValues() throws JsonProcessingException {

		final int numberOfShards = 123;
		final int numberOfReplicas = 543;
		final ElasticIndex index = new ElasticIndex(numberOfShards, numberOfReplicas);

		final String json = write(index);

		verifyJson(numberOfShards, numberOfReplicas, json);
	}

	private String write( final ElasticIndex index ) throws JsonProcessingException {

		return mapper.writeValueAsString(index);
	}

	private void verifyJson( final int numberOfShards, final int numberOfReplicas, final String json ) {

		assertEquals(String.format("{\"settings\":{\"number_of_shards\":%d,\"number_of_replicas\":%d}}", numberOfShards,
		        numberOfReplicas), json);
	}
}