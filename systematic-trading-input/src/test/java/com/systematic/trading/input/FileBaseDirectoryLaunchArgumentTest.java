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
package com.systematic.trading.input;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.input.FileBaseOutputDirectory;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Unit test for the FileBaseDirectoryLaunchArgument.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class FileBaseDirectoryLaunchArgumentTest {

	private static final String ERROR_MESSAGE = "%s argument is not present";
	private static final String FIRST_ERROR_ARGUMENT = ArgumentKey.FILE_BASE_DIRECTORY.getKey();
	private static final String VALIDATOR_EXCEPTION_MESSAGE = "Validation exception message";

	@Mock
	private LaunchArgumentValidator validator;

	/** Argument parser instance being tested. */
	private FileBaseDirectoryLaunchArgument launchArgument;

	@Before
	public void setUp() {
		launchArgument = new FileBaseDirectoryLaunchArgument(validator);
	}

	@Test
	public void nullFileBaseOutputDirectory() {
		setUpValidatorException();

		try {
			getOutputDirectory(null);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(VALIDATOR_EXCEPTION_MESSAGE, e.getMessage());
			verifyValidation(null);
		}
	}

	@Test
	public void relativeFileBaseOutputDirectory() {
		final String baseDirectory = "base";

		final FileBaseOutputDirectory output = getOutputDirectory(baseDirectory);

		verifyDirectory(baseDirectory, output);
		verifyValidation(baseDirectory);
	}

	@Test
	public void multiLevelRelativeFileBaseOutputDirectory() {
		final String baseDirectory = "one/two";

		final FileBaseOutputDirectory output = getOutputDirectory(baseDirectory);

		verifyDirectory(baseDirectory, output);
		verifyValidation(baseDirectory);
	}

	private FileBaseOutputDirectory getOutputDirectory( final String baseDirectory ) {
		return launchArgument.get(setUpArguments(baseDirectory));
	}

	private void verifyDirectory( final String expected, final FileBaseOutputDirectory output ) {
		assertEquals(String.format("%s/WEEKLY_200/", expected), output.directory("WEEKLY_200"));
	}

	private void setUpValidatorException() {
		doThrow(new IllegalArgumentException(VALIDATOR_EXCEPTION_MESSAGE)).when(validator).validate(any(), anyString(),
		        anyString());
	}

	private void verifyValidation( final Object value ) {
		verify(validator).validate(value == null ? isNull() : eq(value), eq(ERROR_MESSAGE), eq(FIRST_ERROR_ARGUMENT));

	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.FILE_BASE_DIRECTORY, value);
		return arguments;
	}
}