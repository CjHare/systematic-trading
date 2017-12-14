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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Testing for the optional DataServiceTypeLaunchArgument
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class EquityDatasetLaunchArgumentTest {

	private static final String ERROR_MESSAGE = "%s argument is not present";
	private static final String FIRST_ERROR_ARGUMENT = ArgumentKey.EQUITY_DATASET.getKey();
	private static final String VALIDATOR_EXCEPTION_MESSAGE = "Validation exception message";

	@Mock
	private LaunchArgumentValidator validator;

	/** Launch argument parser instance being tested. */
	private EquityDatasetLaunchArgument argument;

	@Before
	public void setUp() {
		argument = new EquityDatasetLaunchArgument(validator);
	}

	@Test
	public void present() {
		final String expectedSymbol = "ServiceType";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(expectedSymbol);

		final EquityDataset symbol = getEquityDataset(launchArguments);

		verifEquityDataset(expectedSymbol, symbol);
	}

	@Test
	public void missingValue() {
		setUpValidatorException();

		equityDatasetExpectingException(VALIDATOR_EXCEPTION_MESSAGE, setUpArguments(""));

		veriyValidationExceptionOnValidate("");
	}

	@Test
	public void missingKey() {
		setUpValidatorException();

		equityDatasetExpectingException(VALIDATOR_EXCEPTION_MESSAGE, new HashMap<ArgumentKey, String>());

		veriyValidationExceptionOnValidate(null);
	}

	private void setUpValidatorException() {
		doThrow(new IllegalArgumentException(VALIDATOR_EXCEPTION_MESSAGE)).when(validator).validate(any(), anyString(),
		        anyString());
	}

	private void veriyValidationExceptionOnValidate( final String launchArgument ) {
		verify(validator).validate(launchArgument == null ? isNull() : eq(launchArgument), eq(ERROR_MESSAGE),
		        eq(FIRST_ERROR_ARGUMENT));
		verifyNoMoreInteractions(validator);
	}

	private EquityDataset getEquityDataset( final Map<ArgumentKey, String> launchArguments ) {
		return argument.get(launchArguments);
	}

	private void verifEquityDataset( final String expected, final EquityDataset actual ) {
		assertNotNull(actual);
		assertNotNull(actual.getDataset());
		assertTrue(StringUtils.equals(expected, actual.getDataset()));
	}

	private void equityDatasetExpectingException( final String expectedMessage,
	        final Map<ArgumentKey, String> launchArguments ) {
		try {
			getEquityDataset(launchArguments);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.EQUITY_DATASET, value);
		return arguments;
	}
}