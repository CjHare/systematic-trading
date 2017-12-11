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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.backtest.input.FileBaseOutputDirectory;
import com.systematic.trading.backtest.input.OutputType;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Test for the BacktestLaunchArgumentParser.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class BacktestLaunchArgumentsTest {

	private static final String DIRCTORY_EXCEPTION_MESSAGE = "Directory exception message";
	private static final String OUTPUT_EXCEPTION_MESSAGE = "Ooutput Type exception message";

	@Mock
	private LaunchArgumentsParser argumentParser;

	@Mock
	private LaunchArgument<OutputType> outputTypeArgument;

	@Mock
	private LaunchArgument<FileBaseOutputDirectory> directoryArgument;

	@Mock
	private LaunchArgument<BacktestStartDate> startDateArgument;

	@Mock
	private LaunchArgument<BacktestEndDate> endDateArgument;

	@Mock
	private EquityArguments equityArguments;

	/** Launch argument parser instance being tested. */
	private BacktestLaunchArguments parser;

	@Test
	public void outputType() {
		final String outputType = "elastic_search";
		final Map<ArgumentKey, String> launchArguments = getArgumentMap(outputType);
		setUpArgumentMap(outputType);
		setUpOutputArgument(OutputType.ELASTIC_SEARCH);

		createLaunchArguments(launchArguments);

		verifyOutputType(OutputType.ELASTIC_SEARCH);
		verifyOutputTypeArgument(outputType);
	}

	@Test
	public void fileOutputDirectory() {
		final String outputDirectory = "../../simulations";
		final String outputType = "no_display";
		final Map<ArgumentKey, String> launchArguments = getArgumentMap(outputType, outputDirectory);
		setUpArgumentMap(outputType, outputDirectory);
		setUpDirectoryArgument(outputDirectory);

		createLaunchArguments(launchArguments);

		verifyOutputDirectory(outputDirectory);
		verifyOutputDirectoryArgument(outputType, outputDirectory);
	}

	@Test
	public void outputArgumentException() {
		final String outputType = "unmatched output type";
		final Map<ArgumentKey, String> launchArguments = getArgumentMap(outputType);
		setUpOutputArgumentException();
		setUpArgumentMap(outputType);

		createLaunchArgumentsExpectingException(OUTPUT_EXCEPTION_MESSAGE, launchArguments);

		verifyOutputTypeArgument(outputType);
	}

	@Test
	public void fileOutputDirectoryException() {
		final String outputType = "unmatched output type";
		final Map<ArgumentKey, String> launchArguments = getArgumentMap(outputType);
		setUpDirectoryArgumentException();

		createLaunchArguments(launchArguments);

		outputDirectoryExpectingException(DIRCTORY_EXCEPTION_MESSAGE);
	}

	@Test
	public void startDate() {
		final LocalDate today = LocalDate.now();
		setUpStartDate(today);

		createLaunchArguments();

		verifyStartDate(today);
		verifyStartDateArgument();
	}

	@Test
	public void endDate() {
		final LocalDate today = LocalDate.now();
		setUpEndDate(today);

		createLaunchArguments();

		verifyEndDate(today);
		verifyEndDateArgument();
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

	private void setUpStartDate( final LocalDate startDate ) {
		when(startDateArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenReturn(new BacktestStartDate(startDate));
	}

	private void setUpEndDate( final LocalDate startDate ) {
		when(endDateArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(new BacktestEndDate(startDate));
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

	private void outputDirectoryExpectingException( final String expectedMessage ) {
		try {
			parser.getOutputDirectory("WEEKLY_150");
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
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
		parser = new BacktestLaunchArguments(outputTypeArgument, equityArguments, startDateArgument, endDateArgument,
		        directoryArgument, arguments);
	}

	private void createLaunchArguments() {
		createLaunchArguments(new EnumMap<>(ArgumentKey.class));
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

	private void verifyOutputType( final OutputType expected ) {
		assertEquals(expected, parser.getOutputType());
	}

	private void verifyOutputDirectory( final String baseDirectory ) {
		assertEquals(String.format("%s/WEEKLY_150/", baseDirectory), parser.getOutputDirectory("WEEKLY_150"));
	}

	private void verifyStartDate( final LocalDate expected ) {
		assertNotNull(parser.getStartDate());
		assertEquals(expected, parser.getStartDate().getDate());
	}

	private void verifyEndDate( final LocalDate expected ) {
		assertNotNull(parser.getEndDate());
		assertEquals(expected, parser.getEndDate().getDate());
	}

	private void verifyOutputDirectoryArgument( final String outputValue, final String fileBaseDirectory ) {
		verify(directoryArgument).get(getArgumentMap(outputValue, fileBaseDirectory));
		verifyNoMoreInteractions(directoryArgument);
	}

	private void verifyStartDateArgument() {
		verify(startDateArgument).get(new EnumMap<>(ArgumentKey.class));
		verifyNoMoreInteractions(startDateArgument);
	}

	private void verifyEndDateArgument() {
		verify(endDateArgument).get(new EnumMap<>(ArgumentKey.class));
		verifyNoMoreInteractions(endDateArgument);
	}

	private void verifyOutputTypeArgument( final String outputTypeValue ) {
		verify(outputTypeArgument).get(getArgumentMap(outputTypeValue));
		verifyNoMoreInteractions(outputTypeArgument);
	}

	private void setUpOutputArgument( final OutputType type ) {
		when(outputTypeArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(type);
	}

	private void setUpDirectoryArgument( final String directory ) {
		when(directoryArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenReturn(new FileBaseOutputDirectory(directory));
	}

	private void setUpOutputArgumentException() {
		when(outputTypeArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(OUTPUT_EXCEPTION_MESSAGE));
	}

	private void setUpDirectoryArgumentException() {
		when(directoryArgument.get(anyMapOf(ArgumentKey.class, String.class)))
		        .thenThrow(new IllegalArgumentException(DIRCTORY_EXCEPTION_MESSAGE));
	}

	private void setUpArgumentMap( final String outputValue ) {
		when(argumentParser.parse(any(String[].class))).thenReturn(getArgumentMap(outputValue));
	}

	private Map<ArgumentKey, String> getArgumentMap( final String outputValue ) {
		final Map<ArgumentKey, String> arguments = new EnumMap<>(ArgumentKey.class);
		arguments.put(ArgumentKey.OUTPUT_TYPE, outputValue);
		return arguments;
	}

	private void setUpArgumentMap( final String outputValue, final String fileBaseDirectory ) {
		when(argumentParser.parse(any(String[].class))).thenReturn(getArgumentMap(outputValue, fileBaseDirectory));
	}

	private Map<ArgumentKey, String> getArgumentMap( final String outputValue, final String fileBaseDirectory ) {
		final Map<ArgumentKey, String> arguments = getArgumentMap(outputValue);
		arguments.put(ArgumentKey.FILE_BASE_DIRECTORY, fileBaseDirectory);
		return arguments;
	}
}