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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.configuration.FileBaseOutputDirectory;
import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * Test for the BacktestLaunchArgumentParser.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class LaunchArgumentsTest {

	@Mock
	private LaunchArgumentsParser argumentParser;

	@Mock
	private LaunchArgument<OutputType> outputArgument;

	@Mock
	private LaunchArgument<FileBaseOutputDirectory> fileDirectoryArgument;

	@Test
	public void outputType() {
		final String[] launchArguments = { "-output", "elastic_search" };
		final Map<ArgumentKey, String> arguments = setUpArgumentMap("elastic_search");
		when(outputArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(OutputType.ELASTIC_SEARCH);

		final LaunchArguments parser = new LaunchArguments(argumentParser, outputArgument, fileDirectoryArgument,
		        launchArguments);

		assertEquals(OutputType.ELASTIC_SEARCH, parser.getOutputType());
		verify(argumentParser).parse(launchArguments);
		verify(outputArgument).get(arguments);
	}

	@Test
	public void argumentParserException() {
		when(argumentParser.parse(any(String[].class)))
		        .thenThrow(new IllegalArgumentException("Expected exception message"));

		try {
			new LaunchArguments(argumentParser, outputArgument, fileDirectoryArgument);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected exception message", e.getMessage());
		}
	}

	@Test
	public void outputArgumentException() {
		final String[] launchArguments = { "-output", "unmatched output type" };
		final Map<ArgumentKey, String> arguments = setUpArgumentMap("unmatched output type");

		when(outputArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException("Expected exception message"));

		try {
			new LaunchArguments(argumentParser, outputArgument, fileDirectoryArgument, launchArguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected exception message", e.getMessage());
			verify(outputArgument).get(arguments);
		}
	}

	@Test
	public void getFileOutputDirectory() {
		final String[] launchArguments = { "-output", "no_display", "-output_file_base_directory",
		        "../../simulations" };
		final Map<ArgumentKey, String> arguments = setUpArgumentMap("no_display", "../../simulations");
		when(fileDirectoryArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenReturn(new FileBaseOutputDirectory("directory"));

		final LaunchArguments parser = new LaunchArguments(argumentParser, outputArgument, fileDirectoryArgument,
		        launchArguments);
		final String directory = parser.getOutputDirectory(DepositConfiguration.WEEKLY_150);

		assertEquals("directory/WEEKLY_150/", directory);
		verify(argumentParser).parse(launchArguments);
		verify(fileDirectoryArgument).get(arguments);
	}

	@Test
	public void getFileOutputDirectoryException() {
		final String[] launchArguments = { "-output", "unmatched output type" };
		when(fileDirectoryArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException("Expected exception message"));

		try {
			new LaunchArguments(argumentParser, outputArgument, fileDirectoryArgument, launchArguments)
			        .getOutputDirectory(DepositConfiguration.WEEKLY_150);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected exception message", e.getMessage());
		}
	}

	private Map<ArgumentKey, String> setUpArgumentMap( final String outputValue ) {
		final Map<ArgumentKey, String> arguments = new EnumMap<>(ArgumentKey.class);

		arguments.put(ArgumentKey.OUTPUT_TYPE, outputValue);

		when(argumentParser.parse(any(String[].class))).thenReturn(arguments);

		return arguments;
	}

	private Map<ArgumentKey, String> setUpArgumentMap( final String outputValue, final String fileBaseDirectory ) {
		final Map<ArgumentKey, String> arguments = new EnumMap<>(ArgumentKey.class);

		arguments.put(ArgumentKey.OUTPUT_TYPE, outputValue);
		arguments.put(ArgumentKey.FILE_BASE_DIRECTORY, fileBaseDirectory);

		when(argumentParser.parse(any(String[].class))).thenReturn(arguments);

		return arguments;
	}

}