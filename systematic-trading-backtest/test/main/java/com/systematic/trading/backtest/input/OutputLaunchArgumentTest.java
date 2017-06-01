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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * Unit test for the OutputLaunchArgument.
 * 
 * @author CJ Hare
 */
public class OutputLaunchArgumentTest {

	@Test
	public void unknownOutputType() {
		try {
			new OutputLaunchArgument().get(setUpArguments("unknown"));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("-output argument is not in the set of supported OutputTypes: unknown", e.getMessage());
		}
	}

	@Test
	public void nullOutputType() {
		try {
			new OutputLaunchArgument().get(setUpArguments(null));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("-output argument is not in the set of supported OutputTypes: null", e.getMessage());
		}
	}

	@Test
	public void noDisplayOutputType() {
		final OutputType output = new OutputLaunchArgument().get(setUpArguments("no_display"));

		assertEquals(OutputType.NO_DISPLAY, output);
	}

	@Test
	public void fileMinimumOutputType() {
		final OutputType output = new OutputLaunchArgument().get(setUpArguments("file_minimum"));

		assertEquals(OutputType.FILE_MINIMUM, output);
	}

	@Test
	public void fileCompleteOutputType() {
		final OutputType output = new OutputLaunchArgument().get(setUpArguments("file_complete"));

		assertEquals(OutputType.FILE_COMPLETE, output);
	}

	@Test
	public void elasticSearchOutputType() {
		final OutputType output = new OutputLaunchArgument().get(setUpArguments("elastic_search"));

		assertEquals(OutputType.ELASTIC_SEARCH, output);
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.OUTPUT_TYPE, value);
		return arguments;
	}
}