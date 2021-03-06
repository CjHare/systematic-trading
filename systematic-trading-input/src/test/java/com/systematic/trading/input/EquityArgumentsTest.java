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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.TickerDataset;

/**
 * Verifying the behavior of the EquityArguments.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class EquityArgumentsTest {

	private static final String EQUITY_DATASET_EXCEPTION_MESSAGE = "Equity Dataser exception message";
	private static final String TICKER_SYMBOL_EXCEPTION_MESSAGE = "Opening Funds exception message";

	@Mock
	private LaunchArgument<TickerSymbol> tickerSymbolArgument;

	@Mock
	private LaunchArgument<TickerDataset> equityDatasetArgument;

	/** Equity argument parser instance being tested. */
	private EquityArguments parser;

	@Test
	public void equityDataSet() {

		final String dataSet = "BBBbbBBBbbBB";
		final Map<LaunchArgumentKey, String> arguments = argumentMap("zzzXXXXzzzz", "abcdefg", dataSet, "stuwxyz");
		setUpEquityDataSet(dataSet);

		createEquityArguments(arguments);

		verifyEquityDataSet(dataSet);
		verifyEquityDataSetArgument(arguments);
	}

	@Test
	public void equityDataSetException() {

		final Map<LaunchArgumentKey,
		        String> arguments = argumentMap("zzzXXXXzzzz", "abcdefg", "BBBbbBBBbbBB", "stuwxyz");
		setUpEquityDataSetException();

		createEquityArgumentsExpectingException(EQUITY_DATASET_EXCEPTION_MESSAGE, arguments);

		verifyEquityDataSetArgument(arguments);
	}

	@Test
	public void tickerSymbol() {

		final String ticker = "Ticker";
		final Map<LaunchArgumentKey, String> arguments = argumentMap("zzzXXXXzzzz", "vvv", "yyy", ticker);
		setUpTickerSymbol(ticker);

		createEquityArguments(arguments);

		verifyTickerSymbol(ticker);
		verifyTickerSymbolArgument(arguments);
	}

	@Test
	public void tickerSymbolException() {

		final Map<LaunchArgumentKey, String> arguments = argumentMap("zzzXXXXzzzz", "vvv", "yyy", "Ticker");
		setUpTickerException();

		createEquityArgumentsExpectingException(TICKER_SYMBOL_EXCEPTION_MESSAGE, arguments);

		verifyTickerSymbolArgument(arguments);
	}

	private void createEquityArguments( final Map<LaunchArgumentKey, String> arguments ) {

		parser = new EquityArguments(equityDatasetArgument, tickerSymbolArgument, arguments);
	}

	private Map<LaunchArgumentKey, String> argumentMap(
	        final String dataService,
	        final String dataServiceStructure,
	        final String equityDataSet,
	        final String tickerSymbol ) {

		final Map<LaunchArgumentKey, String> arguments = new EnumMap<>(LaunchArgumentKey.class);
		arguments.put(LaunchArgumentKey.DATA_SERVICE, dataService);
		arguments.put(LaunchArgumentKey.DATA_SERVICE_STRUCTURE, dataServiceStructure);
		arguments.put(LaunchArgumentKey.TICKER_DATASET, equityDataSet);
		arguments.put(LaunchArgumentKey.TICKER_SYMBOL, tickerSymbol);
		return arguments;
	}

	private void createEquityArgumentsExpectingException(
	        final String expectedMessage,
	        final Map<LaunchArgumentKey, String> arguments ) {

		try {
			createEquityArguments(arguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void setUpEquityDataSet( final String dataSet ) {

		when(equityDatasetArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenReturn(new TickerDataset(dataSet));
	}

	private void setUpEquityDataSetException() {

		when(equityDatasetArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(EQUITY_DATASET_EXCEPTION_MESSAGE));
	}

	private void setUpTickerSymbol( final String dataSet ) {

		when(tickerSymbolArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenReturn(new TickerSymbol(dataSet));
	}

	private void setUpTickerException() {

		when(tickerSymbolArgument.get(anyMapOf(LaunchArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(TICKER_SYMBOL_EXCEPTION_MESSAGE));
	}

	private void verifyEquityDataSet( final String expected ) {

		assertNotNull(parser.tickerDataset());
		assertEquals(expected, parser.tickerDataset().dataset());
	}

	private void verifyTickerSymbol( final String expected ) {

		assertNotNull(parser.tickerSymbol());
		assertEquals(expected, parser.tickerSymbol().symbol());
	}

	private void verifyEquityDataSetArgument( final Map<LaunchArgumentKey, String> arguments ) {

		verify(equityDatasetArgument).get(arguments);
		verifyNoMoreInteractions(equityDatasetArgument);
	}

	private void verifyTickerSymbolArgument( final Map<LaunchArgumentKey, String> arguments ) {

		verify(tickerSymbolArgument).get(arguments);
		verifyNoMoreInteractions(tickerSymbolArgument);
	}
}
