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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.configuration.BacktestStartDate;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class StartDateLaunchArgumentTest {

	@Mock
	private LaunchArgumentValidator validator;

	private StartDateLaunchArgument argument;

	@Before
	public void setUp() {
		argument = new StartDateLaunchArgument(validator);
	}

	@Test
	public void validStartDate() {
		final String expectedStartDate = "2017-06-06";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(expectedStartDate);

		final BacktestStartDate startDate = argument.get(launchArguments);

		verifStartyDate(expectedStartDate, startDate);
	}

	@Test
	public void invalidFormat() {
		final String expectedStartDate = "06-06-2017";
		final Map<ArgumentKey, String> launchArguments = setUpArguments(expectedStartDate);

		final BacktestStartDate startDate = argument.get(launchArguments);

		verifStartyDate(expectedStartDate, startDate);
	}

	//TODO invalid & formatting issues

	private void verifStartyDate( final String expected, final BacktestStartDate axtual ) {
		assertNotNull(axtual);
		assertNotNull(axtual.getStartDate());
		assertTrue(LocalDate.parse(expected).equals(axtual.getStartDate()));
	}

	private Map<ArgumentKey, String> setUpArguments( final String value ) {
		final Map<ArgumentKey, String> arguments = new HashMap<>();
		arguments.put(ArgumentKey.START_DATE, value);
		return arguments;
	}
}