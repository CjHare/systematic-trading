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

import org.junit.Test;

import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;

/**
 * Test for the BacktestLaunchArgumentParser.
 * 
 * @author CJ Hare
 */
public class LaunchArgumentsTest {

	@Test
	public void outputTypeElasticSearch() {
		final String[] launchArguments = { "-output", "elastic_search" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals(OutputType.ELASTIC_SEARCH, parser.getOutputType());
	}

	@Test
	public void outputTypeFileComplete() {
		final String[] launchArguments = { "-output", "file_complete" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals(OutputType.FILE_COMPLETE, parser.getOutputType());
	}

	@Test
	public void outputTypeFileMinimum() {
		final String[] launchArguments = { "-output", "file_minimum" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals(OutputType.FILE_MINIMUM, parser.getOutputType());
	}

	@Test
	public void outputTypeNoDisplay() {
		final String[] launchArguments = { "-output", "no_display" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals(OutputType.NO_DISPLAY, parser.getOutputType());
	}

	@Test
	public void tooFewArguments() {

		try {
			new LaunchArguments(new CommandLineLaunchArgumentsParser(),
			        new OutputLaunchArgument());
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Output argument is not in the set of supported OutputTypes: null", e.getMessage());
		}
	}

	@Test
	public void tooManyArguments() {
		final String[] launchArguments = { "-output", "no_display", "another_argument" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals(OutputType.NO_DISPLAY, parser.getOutputType());
	}

	@Test
	public void withoutMatchingOutputType() {
		final String[] launchArguments = { "-output", "unmatched output type" };

		try {
			new LaunchArguments(new CommandLineLaunchArgumentsParser(),
			        new OutputLaunchArgument(), launchArguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Output argument is not in the set of supported OutputTypes: unmatched output type",
			        e.getMessage());
		}
	}

	@Test
	public void getBaseOutputDirectory() {
		final String[] launchArguments = { "-output", "no_display" };

		final LaunchArguments parser = new LaunchArguments(
		        new CommandLineLaunchArgumentsParser(), new OutputLaunchArgument(), launchArguments);

		assertEquals("../../simulations/WEEKLY_150/", parser.getBaseOutputDirectory(DepositConfiguration.WEEKLY_150));
	}
}