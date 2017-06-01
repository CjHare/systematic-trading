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
package com.systematic.trading.backtest.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * Unit test for the OutputLaunchArgument.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class OutputLaunchArgumentTest {

	private static final String ERROR_MESSAGE = "%s argument is not in the set of supported OutputTypes: %s";
	private static final String FIRST_ERROR_ARGUMENT = ArgumentKey.OUTPUT_TYPE.getKey();

	@Mock
	private LaunchArgumentValidator validator;

	@Test
	public void unknownOutputType() {
		doThrow(new IllegalArgumentException("error message")).when(validator).validate(any(), anyString(), anyString(),
		        anyString());

		try {
			new OutputLaunchArgument(validator).get(setUpArguments("unknown"));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("error message", e.getMessage());
			verify(validator).validate(isNull(), eq(ERROR_MESSAGE), eq(FIRST_ERROR_ARGUMENT), eq("unknown"));
		}
	}

	@Test
	public void nullOutputType() {
		doThrow(new IllegalArgumentException("error message")).when(validator).validate(any(), anyString(), anyString(),
		        anyString());

		try {
			new OutputLaunchArgument(validator).get(setUpArguments(null));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("error message", e.getMessage());
			verify(validator).validate(isNull(), eq(ERROR_MESSAGE), eq(FIRST_ERROR_ARGUMENT), isNull());
		}
	}

	@Test
	public void noDisplayOutputType() {
		final OutputType output = new OutputLaunchArgument(validator).get(setUpArguments("no_display"));

		assertEquals(OutputType.NO_DISPLAY, output);
		verify(validator).validate(OutputType.NO_DISPLAY, ERROR_MESSAGE, FIRST_ERROR_ARGUMENT, "no_display");
	}

	@Test
	public void fileMinimumOutputType() {
		final OutputType output = new OutputLaunchArgument(validator).get(setUpArguments("file_minimum"));

		assertEquals(OutputType.FILE_MINIMUM, output);
		verify(validator).validate(OutputType.FILE_MINIMUM, ERROR_MESSAGE, FIRST_ERROR_ARGUMENT, "file_minimum");
	}

	@Test
	public void fileCompleteOutputType() {
		final OutputType output = new OutputLaunchArgument(validator).get(setUpArguments("file_complete"));

		assertEquals(OutputType.FILE_COMPLETE, output);
		verify(validator).validate(OutputType.FILE_COMPLETE, ERROR_MESSAGE, FIRST_ERROR_ARGUMENT, "file_complete");
	}

	@Test
	public void elasticSearchOutputType() {
		final OutputType output = new OutputLaunchArgument(validator).get(setUpArguments("elastic_search"));

		assertEquals(OutputType.ELASTIC_SEARCH, output);
		verify(validator).validate(OutputType.ELASTIC_SEARCH, ERROR_MESSAGE, FIRST_ERROR_ARGUMENT, "elastic_search");
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.OUTPUT_TYPE, value);
		return arguments;
	}
}