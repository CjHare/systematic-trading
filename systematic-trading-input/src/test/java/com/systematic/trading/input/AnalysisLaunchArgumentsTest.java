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
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

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
		final Map<ArgumentKey, String> arguments = argumentMap(openingFunds);
		setUpOpeningFunds(openingFunds);

		createLaunchArguments(arguments);

		verifyOpeningFunds(openingFunds);
		verifyOpeningFundsArgument(arguments);
	}

	@Test
	public void openingFundsException() {
		final Map<ArgumentKey, String> arguments = argumentMap("101.45");
		setUpOpeningFundsException();

		createLaunchArgumentsExpectingException(OPENING_FUNDS_EXCEPTION_MESSAGE, arguments);

		verifyOpeningFundsArgument(arguments);
	}

	@Test
	public void dataService() {
		final String serviceName = "identity of the data service";
		setUpDataService(serviceName);

		createLaunchArguments();

		verifyDataService(serviceName);
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

	private Map<ArgumentKey, String> argumentMap( final String openingFunds ) {
		final Map<ArgumentKey, String> arguments = new EnumMap<>(ArgumentKey.class);
		arguments.put(ArgumentKey.OPENING_FUNDS, openingFunds);
		return arguments;
	}

	private void createLaunchArgumentsExpectingException( final String expectedMessage,
	        final Map<ArgumentKey, String> arguments ) {
		try {
			createLaunchArguments(arguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void createLaunchArguments( final Map<ArgumentKey, String> arguments ) {
		parser = new AnalysisLaunchArguments(equityArguments, openingFundsArgument, arguments);
	}

	private void createLaunchArguments() {
		createLaunchArguments(new EnumMap<>(ArgumentKey.class));
	}

	private void setUpOpeningFunds( final String funds ) {
		when(openingFundsArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(new BigDecimal(funds));
	}

	private void setUpTickerSymbol( final String serviceName ) {
		when(equityArguments.getTickerSymbol()).thenReturn(new TickerSymbol(serviceName));
	}

	private void setUpDataService( final String serviceName ) {
		when(equityArguments.getDataService()).thenReturn(new DataServiceType(serviceName));
	}

	private void setUpEquityDataSet( final String serviceName ) {
		when(equityArguments.getEquityDataset()).thenReturn(new EquityDataset(serviceName));
	}

	private void setUpOpeningFundsException() {
		when(openingFundsArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(OPENING_FUNDS_EXCEPTION_MESSAGE));
	}

	private void verifyDataService( final String expected ) {
		assertNotNull(parser.getDataService());
		assertEquals(expected, parser.getDataService().getType());
		verify(equityArguments, atLeastOnce()).getDataService();
	}

	private void verifyEquityDataSet( final String expected ) {
		assertNotNull(parser.getEquityDataset());
		assertEquals(expected, parser.getEquityDataset().getDataset());
		verify(equityArguments, atLeastOnce()).getEquityDataset();
	}

	private void verifyTickerSymbol( final String expected ) {
		assertNotNull(parser.getTickerSymbol());
		assertEquals(expected, parser.getTickerSymbol().getSymbol());
		verify(equityArguments, atLeastOnce()).getTickerSymbol();
	}

	private void verifyOpeningFunds( final String expected ) {
		assertNotNull(parser.getOpeningFunds());
		assertEquals(new BigDecimal(expected), parser.getOpeningFunds());
	}

	private void verifyOpeningFundsArgument( final Map<ArgumentKey, String> arguments ) {
		verify(openingFundsArgument).get(arguments);
		verifyNoMoreInteractions(openingFundsArgument);
	}
}