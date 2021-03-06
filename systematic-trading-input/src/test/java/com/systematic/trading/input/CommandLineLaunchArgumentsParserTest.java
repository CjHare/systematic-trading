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
package com.systematic.trading.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

/**
 * CommandLineLaunchArgumentsParser test.
 * 
 * @author CJ Hare
 */
public class CommandLineLaunchArgumentsParserTest {

	@Test
	public void outputType() {

		final String[] launchArguments = { "-output", "elastic_search" };

		final Map<LaunchArgumentKey, String> results = parse(launchArguments);

		verifyOutputType("elastic_search", results);
	}

	@Test
	public void tooManyArguments() {

		final String[] launchArguments = { "-output", "no_display", "another_argument" };

		final Map<LaunchArgumentKey, String> results = parse(launchArguments);

		verifyOutputType("no_display", results);
	}

	@Test
	public void tooFewArguments() {

		final String[] launchArguments = { "-output" };

		try {
			new CommandLineInputLaunchArgumentParser().parse(launchArguments);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Missing value for argument key -output", e.getMessage());
		}
	}

	private Map<LaunchArgumentKey, String> parse( final String[] launchArguments ) {

		return new CommandLineInputLaunchArgumentParser().parse(launchArguments);
	}

	private void verifyOutputType( final String expectedOutputType, final Map<LaunchArgumentKey, String> results ) {

		assertNotNull(results);
		assertEquals(expectedOutputType, results.get(LaunchArgumentKey.OUTPUT_TYPE));
	}
}
