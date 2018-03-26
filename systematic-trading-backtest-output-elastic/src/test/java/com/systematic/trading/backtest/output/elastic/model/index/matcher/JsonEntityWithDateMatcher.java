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
package com.systematic.trading.backtest.output.elastic.model.index.matcher;

import java.time.LocalDate;
import java.util.Optional;

import javax.ws.rs.client.Entity;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * As the LocalDate JSON conversion mixes up the ordering of elements, a more generous matching is
 * required.
 * 
 * @author CJ Hare
 */
public class JsonEntityWithDateMatcher extends ArgumentMatcher<Entity<?>> {

	private final ObjectMapper mapper = new ObjectMapper();
	private final String expectedJson;
	private final LocalDate date;

	public JsonEntityWithDateMatcher( final String expectedJson, final LocalDate date ) {

		this.expectedJson = expectedJson;
		this.date = date;
	}

	public boolean matches( Object argument ) {

		if (argument instanceof Entity<?>) {

			final Entity<?> entity = (Entity<?>) argument;
			final Optional<String> jsonEntity = parseEntity(entity);
			return jsonEntity.isPresent() && containsJson(jsonEntity.get()) && containsDate(jsonEntity.get());
		}

		return false;
	}

	private boolean containsJson( final String json ) {

		return StringUtils.contains(json, expectedJson);
	}

	private boolean containsDate( final String json ) {

		return StringUtils.contains(json, String.format("\"dayOfMonth\":%s", date.getDayOfMonth()))
		        && StringUtils.contains(json, String.format("\"monthValue\":%s", date.getMonthValue()))
		        && StringUtils.contains(json, String.format("\"year\":%s", date.getYear()));
	}

	@Override
	public void describeTo( Description description ) {

		description.appendText(expectedJson);
		description.appendText(date.toString());
	}

	private Optional<String> parseEntity( final Entity<?> entity ) {

		try {
			return Optional.of(mapper.writeValueAsString(entity.getEntity()));
		} catch (JsonProcessingException e) {
			return Optional.empty();
		}
	}
}
