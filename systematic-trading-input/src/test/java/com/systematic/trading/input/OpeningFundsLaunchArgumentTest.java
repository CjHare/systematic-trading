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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class OpeningFundsLaunchArgumentTest {

	private static final LaunchArgumentKey KEY = LaunchArgumentKey.OPENING_FUNDS;
	private static final String ERROR_MESSAGE = "%s argument is not present";
	private static final String VALIDATOR_EXCEPTION_MESSAGE = "Validation exception message";

	@Mock
	private LaunchArgumentValidator validator;

	/** Launch argument parser instance being tested. */
	private OpeningFundsLaunchArgument argument;

	@Before
	public void setUp() {

		argument = new OpeningFundsLaunchArgument(validator);
	}

	@Test
	public void validOpeningFunds() {

		final String expected = "1.2";
		final Map<LaunchArgumentKey, String> launchArguments = setUpArguments(expected);

		final BigDecimal symbol = value(launchArguments);

		verifyOpeningFunds(expected, symbol);
	}

	@Test
	public void missingValue() {

		setUpValidatorException();

		valueExpectingException(VALIDATOR_EXCEPTION_MESSAGE, setUpArguments(""));

		verifyValidationExceptionOnValidate("");
	}

	@Test
	public void missingKey() {

		setUpValidatorException();

		valueExpectingException(VALIDATOR_EXCEPTION_MESSAGE, new HashMap<LaunchArgumentKey, String>());

		verifyValidationExceptionOnValidate(null);
	}

	private void valueExpectingException(
	        final String expectedMessage,
	        final Map<LaunchArgumentKey, String> launchArguments ) {

		try {
			value(launchArguments);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private BigDecimal value( final Map<LaunchArgumentKey, String> launchArguments ) {

		return argument.get(launchArguments);
	}

	private void setUpValidatorException() {

		doThrow(new IllegalArgumentException(VALIDATOR_EXCEPTION_MESSAGE)).when(validator)
		        .validate(any(), anyString(), anyString());
	}

	private void verifyValidationExceptionOnValidate( final String launchArgument ) {

		verify(validator).validate(launchArgument == null ? isNull() : eq(launchArgument), eq(ERROR_MESSAGE), eq(KEY));
		verifyNoMoreInteractions(validator);
	}

	private void verifyOpeningFunds( final String expected, final BigDecimal actual ) {

		assertNotNull(actual);
		assertNotNull(actual);
		assertEquals(new BigDecimal(expected), actual);
	}

	private Map<LaunchArgumentKey, String> setUpArguments( final String value ) {

		final Map<LaunchArgumentKey, String> arguments = new HashMap<>();
		arguments.put(KEY, value);
		return arguments;
	}
}
