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

import java.util.Optional;

import javax.ws.rs.client.Entity;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonEntityMatcher extends ArgumentMatcher<Entity<?>> {

	private final ObjectMapper mapper = new ObjectMapper();
	private final String expectedJson;

	public JsonEntityMatcher(final String expectedJson) {
		this.expectedJson = expectedJson;
	}

	public boolean matches( Object argument ) {

		if (argument instanceof Entity<?>) {

			final Entity<?> entity = (Entity<?>) argument;
			final Optional<String> jsonEntity = parseEntity(entity);

			System.out.println(expectedJson);
			System.out.println(jsonEntity.get());

			return jsonEntity.isPresent() && StringUtils.contains(jsonEntity.get(), expectedJson);
		}

		return false;
	}

	@Override
	public void describeTo( Description description ) {
		description.appendText(expectedJson);
	}

	private Optional<String> parseEntity( final Entity<?> entity ) {
		try {
			return Optional.of(mapper.writeValueAsString(entity.getEntity()));
		} catch (JsonProcessingException e) {
			return Optional.empty();
		}
	}
}