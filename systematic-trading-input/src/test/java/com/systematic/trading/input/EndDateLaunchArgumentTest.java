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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.input.BacktestEndDate;

/**
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class EndDateLaunchArgumentTest {

	private static final String FORMAT_ERROR_MESSAGE = "%s argument date format is invalid";
	private static final String ERROR_MESSAGE = "%s argument is not present";
	private static final String FIRST_ERROR_ARGUMENT = ArgumentKey.END_DATE.getKey();
	private static final String VALIDATOR_EXCEPTION_MESSAGE = "Validation exception message";
	private static final String VALIDATOR_FORMAT_EXCEPTION_MESSAGE = "Validation format exception message";

	@Mock
	private LaunchArgumentValidator validator;

	/** Argument parser instance being tested. */
	private EndDateLaunchArgument argument;

	@Before
	public void setUp() {

		argument = new EndDateLaunchArgument(validator);
	}

	@Test
	public void validEndDate() {

		final String expectedStartDate = "2017-06-06";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(expectedStartDate);

		final BacktestEndDate startDate = endDate(launchArguments);

		verifyStartDate(expectedStartDate, startDate);
	}

	@Test
	public void invaliEndDatedFormat() {

		setUpValidatorFormatException();
		final String expectedStartDate = "06-06-2017";

		try {
			endDate(setUpArguments(expectedStartDate));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(VALIDATOR_FORMAT_EXCEPTION_MESSAGE, e.getMessage());
			verifyValidation(expectedStartDate);
		}
	}

	@Test
	public void missingValue() {

		setUpValidatorException();

		try {
			endDate(setUpArguments(""));
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(VALIDATOR_EXCEPTION_MESSAGE, e.getMessage());
			verifyValidationExceptionOnValidate("");
		}
	}

	@Test
	public void missingKey() {

		setUpValidatorException();

		try {
			endDate(new HashMap<ArgumentKey, String>());
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(VALIDATOR_EXCEPTION_MESSAGE, e.getMessage());
			verifyValidationExceptionOnValidate(null);
		}
	}

	private BacktestEndDate endDate( final Map<ArgumentKey, String> arguments ) {

		return argument.get(arguments);
	}

	private void setUpValidatorException() {

		doThrow(new IllegalArgumentException(VALIDATOR_EXCEPTION_MESSAGE)).when(validator)
		        .validate(any(), anyString(), anyString());
	}

	private void setUpValidatorFormatException() {

		doThrow(new IllegalArgumentException(VALIDATOR_FORMAT_EXCEPTION_MESSAGE)).when(validator)
		        .validateDateFormat(anyString(), anyString(), any());
	}

	private void verifyValidation( final String launchArgument ) {

		verify(validator).validate(
		        launchArgument == null ? isNull() : eq(launchArgument),
		        eq(ERROR_MESSAGE),
		        eq(FIRST_ERROR_ARGUMENT));
		verify(validator).validateDateFormat(launchArgument, FORMAT_ERROR_MESSAGE, ArgumentKey.END_DATE.getKey());
		verifyNoMoreInteractions(validator);
	}

	private void verifyValidationExceptionOnValidate( final String launchArgument ) {

		verify(validator).validate(
		        launchArgument == null ? isNull() : eq(launchArgument),
		        eq(ERROR_MESSAGE),
		        eq(FIRST_ERROR_ARGUMENT));
		verifyNoMoreInteractions(validator);
	}

	private void verifyStartDate( final String expected, final BacktestEndDate axtual ) {

		assertNotNull(axtual);
		assertNotNull(axtual.date());
		assertTrue(LocalDate.parse(expected).equals(axtual.date()));
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {

		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.END_DATE, value);
		return arguments;
	}
}
