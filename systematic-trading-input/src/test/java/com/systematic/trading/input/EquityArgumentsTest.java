/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Verifying the behavior of the EquityArguments.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class EquityArgumentsTest {

	private static final String DATA_SERVICE_EXCEPTION_MESSAGE = "Opening Funds exception message";
	private static final String EQUITY_DATASET_EXCEPTION_MESSAGE = "Opening Funds exception message";
	private static final String TICKER_SYMBOL_EXCEPTION_MESSAGE = "Opening Funds exception message";

	@Mock
	private LaunchArgument<TickerSymbol> tickerSymbolArgument;

	@Mock
	private LaunchArgument<EquityDataset> equityDatasetArgument;

	@Mock
	private LaunchArgument<DataServiceType> dataServiceArgument;

	/** Equity argument parser instance being tested. */
	private EquityArguments parser;

	@Test
	public void dataService() {

		final String service = "aaaAAAaaa";
		final Map<ArgumentKey, String> arguments = argumentMap(service, "aaaZZZaaa", "eeeeTTTTeeee");
		setUpDataService(service);

		createEquityArguments(arguments);

		verifyDataService(service);
		verifDataServiceArgument(arguments);
	}

	@Test
	public void dataServiceException() {

		final Map<ArgumentKey, String> arguments = argumentMap("aaaAAAaaa", "aaaZZZaaa", "eeeeTTTTeeee");
		setUpDataServiceException();

		createEquityArgumentsExpectingException(DATA_SERVICE_EXCEPTION_MESSAGE, arguments);

		verifDataServiceArgument(arguments);
	}

	@Test
	public void equityDataSet() {

		final String dataSet = "BBBbbBBBbbBB";
		final Map<ArgumentKey, String> arguments = argumentMap("abcdefg", dataSet, "stuwxyz");
		setUpEquityDataSet(dataSet);

		createEquityArguments(arguments);

		verifyEquityDataSet(dataSet);
		verifyEquityDataSetArgument(arguments);
	}

	@Test
	public void equityDataSetException() {

		final Map<ArgumentKey, String> arguments = argumentMap("abcdefg", "BBBbbBBBbbBB", "stuwxyz");
		setUpEquityDataSetException();

		createEquityArgumentsExpectingException(EQUITY_DATASET_EXCEPTION_MESSAGE, arguments);

		verifyEquityDataSetArgument(arguments);
	}

	@Test
	public void tickerSymbol() {

		final String ticker = "Ticker";
		final Map<ArgumentKey, String> arguments = argumentMap("vvv", "yyy", ticker);
		setUpTickerSymbol(ticker);

		createEquityArguments(arguments);

		verifyTickerSymbol(ticker);
		verifyTickerSymbolArgument(arguments);
	}

	@Test
	public void tickerSymbolException() {

		final Map<ArgumentKey, String> arguments = argumentMap("vvv", "yyy", "Ticker");
		setUpTickerException();

		createEquityArgumentsExpectingException(TICKER_SYMBOL_EXCEPTION_MESSAGE, arguments);

		verifyTickerSymbolArgument(arguments);
	}

	private void createEquityArguments( final Map<ArgumentKey, String> arguments ) {

		parser = new EquityArguments(dataServiceArgument, equityDatasetArgument, tickerSymbolArgument, arguments);
	}

	private Map<ArgumentKey, String> argumentMap( final String dataService, final String equityDataSet,
	        final String tickerSymbol ) {

		final Map<ArgumentKey, String> arguments = new EnumMap<>(ArgumentKey.class);
		arguments.put(ArgumentKey.DATA_SERVICE_TYPE, dataService);
		arguments.put(ArgumentKey.EQUITY_DATASET, equityDataSet);
		arguments.put(ArgumentKey.TICKER_SYMBOL, tickerSymbol);
		return arguments;
	}

	private void createEquityArgumentsExpectingException( final String expectedMessage,
	        final Map<ArgumentKey, String> arguments ) {

		try {
			createEquityArguments(arguments);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void setUpDataService( final String service ) {

		when(dataServiceArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenReturn(new DataServiceType(service));
	}

	private void setUpDataServiceException() {

		when(dataServiceArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(DATA_SERVICE_EXCEPTION_MESSAGE));
	}

	private void setUpEquityDataSet( final String dataSet ) {

		when(equityDatasetArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenReturn(new EquityDataset(dataSet));
	}

	private void setUpEquityDataSetException() {

		when(dataServiceArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(DATA_SERVICE_EXCEPTION_MESSAGE));
	}

	private void setUpTickerSymbol( final String dataSet ) {

		when(tickerSymbolArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(new TickerSymbol(dataSet));
	}

	private void setUpTickerException() {

		when(tickerSymbolArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(TICKER_SYMBOL_EXCEPTION_MESSAGE));
	}

	private void verifyDataService( final String expected ) {

		assertNotNull(parser.dataService());
		assertEquals(expected, parser.dataService().type());
	}

	private void verifyEquityDataSet( final String expected ) {

		assertNotNull(parser.equityDataset());
		assertEquals(expected, parser.equityDataset().dataset());
	}

	private void verifyTickerSymbol( final String expected ) {

		assertNotNull(parser.tickerSymbol());
		assertEquals(expected, parser.tickerSymbol().symbol());
	}

	private void verifDataServiceArgument( final Map<ArgumentKey, String> arguments ) {

		verify(equityDatasetArgument).get(arguments);
		verifyNoMoreInteractions(equityDatasetArgument);
	}

	private void verifyEquityDataSetArgument( final Map<ArgumentKey, String> arguments ) {

		verify(equityDatasetArgument).get(arguments);
		verifyNoMoreInteractions(equityDatasetArgument);
	}

	private void verifyTickerSymbolArgument( final Map<ArgumentKey, String> arguments ) {

		verify(tickerSymbolArgument).get(arguments);
		verifyNoMoreInteractions(tickerSymbolArgument);
	}
}