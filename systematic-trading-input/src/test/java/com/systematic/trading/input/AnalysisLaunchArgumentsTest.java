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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.TickerDataset;

/**
 * Unit testing for the expected behavior of the AnalysisLaunchArguments.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalysisLaunchArgumentsTest {

	private static final String OPENING_FUNDS_EXCEPTION_MESSAGE = "Opening Funds exception message";

	@Mock
	private EquityArguments equityArguments;

	@Mock
	private LaunchArgument<BigDecimal> openingFundsArgument;

	/** Launch argument parser instance being tested. */
	private AnalysisLaunchArguments parser;

	@Test
	public void openingFunds() {

		final String openingFunds = "101.45";
		final Map<LaunchArgumentKey, String> arguments = argumentMap(openingFunds);
		setUpOpeningFunds(openingFunds);

		createLaunchArguments(arguments);

		verifyOpeningFunds(openingFunds);
		verifyOpeningFundsArgument(arguments);
	}

	@Test
	public void openingFundsException() {

		final Map<LaunchArgumentKey, String> arguments = argumentMap("101.45");
		setUpOpeningFundsException();

		createLaunchArgumentsExpectingException(OPENING_FUNDS_EXCEPTION_MESSAGE, arguments);

		verifyOpeningFundsArgument(arguments);
	}

	@Test
	public void equityDataSet() {

		final String serviceName = "identity of the data set";
		setUpEquityDataSet(serviceName);

		createLaunchArguments();

		verifyEquityDataSet(serviceName);
	}

	@Test
	public void ticketSymbol() {

		final String tickerSymbol = "SYMBOL";
		setUpTickerSymbol(tickerSymbol);

		createLaunchArguments();

		verifyTickerSymbol(tickerSymbol);
	}

	private Map<LaunchArgumentKey, String> argumentMap( final String openingFunds ) {

		final Map<LaunchArgumentKey, String> arguments = new EnumMap<>(LaunchArgumentKey.class);
		arguments.put(LaunchArgumentKey.OPENING_FUNDS, openingFunds);
		return arguments;
	}

	private void createLaunchArgumentsExpectingException(
	        final String expectedMessage,
	        final Map<LaunchArgumentKey, String> arguments ) {

		try {
			createLaunchArguments(arguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void createLaunchArguments( final Map<LaunchArgumentKey, String> arguments ) {

		parser = new AnalysisLaunchArguments(equityArguments, openingFundsArgument, arguments);
	}

	private void createLaunchArguments() {

		createLaunchArguments(new EnumMap<>(LaunchArgumentKey.class));
	}

	private void setUpOpeningFunds( final String funds ) {

		when(openingFundsArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenReturn(new BigDecimal(funds));
	}

	private void setUpTickerSymbol( final String serviceName ) {

		when(equityArguments.tickerSymbol()).thenReturn(new TickerSymbol(serviceName));
	}

	private void setUpEquityDataSet( final String serviceName ) {

		when(equityArguments.tickerDataset()).thenReturn(new TickerDataset(serviceName));
	}

	private void setUpOpeningFundsException() {

		when(openingFundsArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(OPENING_FUNDS_EXCEPTION_MESSAGE));
	}

	private void verifyEquityDataSet( final String expected ) {

		assertNotNull(parser.tickerDataset());
		assertEquals(expected, parser.tickerDataset().dataset());
		verify(equityArguments, atLeastOnce()).tickerDataset();
	}

	private void verifyTickerSymbol( final String expected ) {

		assertNotNull(parser.tickerSymbol());
		assertEquals(expected, parser.tickerSymbol().symbol());
		verify(equityArguments, atLeastOnce()).tickerSymbol();
	}

	private void verifyOpeningFunds( final String expected ) {

		assertNotNull(parser.openingFunds());
		assertEquals(new BigDecimal(expected), parser.openingFunds());
	}

	private void verifyOpeningFundsArgument( final Map<LaunchArgumentKey, String> arguments ) {

		verify(openingFundsArgument).get(arguments);
		verifyNoMoreInteractions(openingFundsArgument);
	}
}
