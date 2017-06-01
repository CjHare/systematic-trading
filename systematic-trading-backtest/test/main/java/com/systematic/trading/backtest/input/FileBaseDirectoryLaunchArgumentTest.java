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

import com.systematic.trading.backtest.configuration.FileBaseOutputDirectory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * Unit test for the FileBaseDirectoryLaunchArgument.
 * 
 * @author CJ Hare
 */
public class FileBaseDirectoryLaunchArgumentTest {

	@Test
	public void nullFileBaseOutputDirectory() {
		try {
			new FileBaseDirectoryLaunchArgument().get(setUpArguments(null));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("-output_file_base_directory argument is not present", e.getMessage());
		}
	}

	@Test
	public void relativeFileBaseOutputDirectory() {
		final FileBaseOutputDirectory output = new FileBaseDirectoryLaunchArgument().get(setUpArguments("base"));

		assertEquals("base/WEEKLY_150/", output.getDirectory(DepositConfiguration.WEEKLY_150));
	}

	@Test
	public void multiLevelRelativeFileBaseOutputDirectory() {
		final FileBaseOutputDirectory output = new FileBaseDirectoryLaunchArgument().get(setUpArguments("one/two"));

		assertEquals("one/two/WEEKLY_200/", output.getDirectory(DepositConfiguration.WEEKLY_200));
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.FILE_BASE_DIRECTORY, value);
		return arguments;
	}
}