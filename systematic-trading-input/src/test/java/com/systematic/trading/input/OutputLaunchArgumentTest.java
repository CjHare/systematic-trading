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
package com.systematic.trading.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.input.OutputType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Unit test for the OutputLaunchArgument.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class OutputLaunchArgumentTest {

	private static final String ERROR_MESSAGE = "%s argument is not in the set of supported OutputTypes: %s";
	private static final String FIRST_ERROR_ARGUMENT = ArgumentKey.OUTPUT_TYPE.getKey();
	private static final String VALIDATOR_EXCEPTION_MESSAGE = "Validation exception message";

	@Mock
	private LaunchArgumentValidator validator;

	/** Argument parser instance being tested. */
	private OutputLaunchArgument argument;

	@Before
	public void setUp() {

		argument = new OutputLaunchArgument(validator);
	}

	@Test
	public void noDisplayOutputType() {

		final String outputType = "no_display";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(outputType);

		final OutputType output = getOutput(launchArguments);

		verifyRetrievedType(OutputType.NO_DISPLAY, output);
		veriyValidation(OutputType.NO_DISPLAY, outputType);
	}

	@Test
	public void fileMinimumOutputType() {

		final String outputType = "file_minimum";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(outputType);

		final OutputType output = getOutput(launchArguments);

		verifyRetrievedType(OutputType.FILE_MINIMUM, output);
		veriyValidation(OutputType.FILE_MINIMUM, outputType);
	}

	@Test
	public void fileCompleteOutputType() {

		final String outputType = "file_complete";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(outputType);

		final OutputType output = getOutput(launchArguments);

		verifyRetrievedType(OutputType.FILE_COMPLETE, output);
		veriyValidation(OutputType.FILE_COMPLETE, outputType);
	}

	@Test
	public void elasticSearchOutputType() {

		final String outputType = "elastic_search";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(outputType);

		final OutputType output = getOutput(launchArguments);

		verifyRetrievedType(OutputType.ELASTIC_SEARCH, output);
		veriyValidation(OutputType.ELASTIC_SEARCH, outputType);
	}

	@Test
	public void unknownOutputType() {

		setUpValidatorException();
		final Map<ArgumentKey, String> launchArguments = setUpArguments("unknown");

		outputExpectingException(VALIDATOR_EXCEPTION_MESSAGE, launchArguments);

		veriyValidation(null, "unknown");
	}

	@Test
	public void nullOutputType() {

		setUpValidatorException();
		final Map<ArgumentKey, String> launchArguments = setUpArguments(null);

		outputExpectingException(VALIDATOR_EXCEPTION_MESSAGE, launchArguments);

		veriyValidation(null, null);
	}

	private void outputExpectingException( final String expectedMessage,
	        final Map<ArgumentKey, String> launchArguments ) {

		try {
			getOutput(launchArguments);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private OutputType getOutput( final Map<ArgumentKey, String> launchArguments ) {

		return argument.get(launchArguments);
	}

	private void verifyRetrievedType( final OutputType expected, final OutputType actual ) {

		assertEquals(expected, actual);
	}

	private void veriyValidation( final OutputType outputType, final String launchArgument ) {

		verify(validator).validate(outputType == null ? isNull() : eq(outputType), eq(ERROR_MESSAGE),
		        eq(FIRST_ERROR_ARGUMENT), launchArgument == null ? isNull() : eq(launchArgument));
		verifyNoMoreInteractions(validator);
	}

	private void setUpValidatorException() {

		doThrow(new IllegalArgumentException(VALIDATOR_EXCEPTION_MESSAGE)).when(validator).validate(any(), anyString(),
		        anyString(), anyString());
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {

		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.OUTPUT_TYPE, value);
		return arguments;
	}
}