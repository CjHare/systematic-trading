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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
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

import com.systematic.trading.backtest.configuration.BacktestEndDate;
import com.systematic.trading.backtest.configuration.BacktestStartDate;
import com.systematic.trading.backtest.configuration.FileBaseOutputDirectory;
import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.TickerSymbol;
import com.systematic.trading.backtest.input.LaunchArguments.ArgumentKey;

/**
 * Test for the BacktestLaunchArgumentParser.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class LaunchArgumentsTest {

	private static final String ARGUMENT_PARSER_EXCEPTION_MESSAGE = "Argument parser exception message";
	private static final String DIRCTORY_EXCEPTION_MESSAGE = "Directory exception message";
	private static final String OUTPUT_EXCEPTION_MESSAGE = "Ooutput Type exception message";
	private static final String START_DATE_ARGUMENT_EXCEPTION_MESSAGE = "Start Date argument exception message";
	private static final String END_DATE_ARGUMENT_EXCEPTION_MESSAGE = "End Date argument exception message";
	private static final String TICKER_SYMBOL_ARGUMENT_EXCEPTION_MESSAGE = "Ticker Symbol argument exception message";

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
	private LaunchArgument<TickerSymbol> tickerSymbolArgument;

	/** Launch argument parser instance being tested. */
	private LaunchArguments parser;

	@Test
	public void outputType() {
		final String outputType = "elastic_search";
		final String[] launchArguments = { "-output", outputType };
		setUpArgumentMap(outputType);
		setUpOutputArgument(OutputType.ELASTIC_SEARCH);

		createLaunchArguments(launchArguments);

		verifyOutputType(OutputType.ELASTIC_SEARCH);
		verifyArgumentsParsed(launchArguments);
		verifyOutputTypeArgument(outputType);
	}

	@Test
	public void getFileOutputDirectory() {
		final String outputDirectory = "../../simulations";
		final String outputType = "no_display";
		final String[] launchArguments = { "-output", outputType, "-output_file_base_directory", outputDirectory };
		setUpArgumentMap(outputType, outputDirectory);
		setUpDirectoryArgument(outputDirectory);

		createLaunchArguments(launchArguments);

		verifyOutputDirectory(outputDirectory);
		verifyArgumentsParsed(launchArguments);
		verifyOutputDirectoryArgument(outputType, outputDirectory);
	}

	@Test
	public void outputArgumentException() {
		final String outputType = "unmatched output type";
		final String[] launchArguments = { "-output", outputType };
		setUpOutputArgumentException();
		setUpArgumentMap(outputType);

		createLaunchArgumentsExpectingException(OUTPUT_EXCEPTION_MESSAGE, launchArguments);

		verifyOutputTypeArgument(outputType);
	}

	@Test
	public void argumentParserException() {
		setUpArgumentParserException();

		createLaunchArgumentsExpectingException(ARGUMENT_PARSER_EXCEPTION_MESSAGE);
	}

	@Test
	public void startDateArgumentException() {
		setUpStartDateArgumentxception();

		createLaunchArgumentsExpectingException(START_DATE_ARGUMENT_EXCEPTION_MESSAGE);
	}

	@Test
	public void endDateArgumentException() {
		setUpEndDateArgumentxception();

		createLaunchArgumentsExpectingException(END_DATE_ARGUMENT_EXCEPTION_MESSAGE);
	}

	@Test
	public void tickerSymbolArgumentException() {
		setUpTickerSymbolArgumentxception();

		createLaunchArgumentsExpectingException(TICKER_SYMBOL_ARGUMENT_EXCEPTION_MESSAGE);
	}

	@Test
	public void getFileOutputDirectoryException() {
		final String[] launchArguments = { "-output", "unmatched output type" };
		setUpDirectoryArgumentException();
		createLaunchArguments(launchArguments);

		getOutputDirectoryExpectingException(DIRCTORY_EXCEPTION_MESSAGE);
	}

	private void getOutputDirectoryExpectingException( final String expectedMessage ) {
		try {
			parser.getOutputDirectory(DepositConfiguration.WEEKLY_150);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void createLaunchArgumentsExpectingException( final String expectedMessage, final String... args ) {
		try {
			createLaunchArguments(args);
			fail("expecting an exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	private void createLaunchArguments( final String... args ) {
		parser = new LaunchArguments(argumentParser, outputTypeArgument, startDateArgument, endDateArgument,
		        tickerSymbolArgument, directoryArgument, args);
	}

	private void verifyOutputType( final OutputType expected ) {
		assertEquals(expected, parser.getOutputType());
	}

	private void verifyOutputDirectory( final String baseDirectory ) {
		assertEquals(String.format("%s/WEEKLY_150/", baseDirectory),
		        parser.getOutputDirectory(DepositConfiguration.WEEKLY_150));
	}

	private void verifyOutputDirectoryArgument( final String outputValue, final String fileBaseDirectory ) {
		verify(directoryArgument).get(getArgumentMap(outputValue, fileBaseDirectory));
		verifyNoMoreInteractions(directoryArgument);
	}

	private void verifyOutputTypeArgument( final String outputTypeValue ) {
		verify(outputTypeArgument).get(getArgumentMap(outputTypeValue));
		verifyNoMoreInteractions(outputTypeArgument);
	}

	private void verifyArgumentsParsed( final String[] launchArguments ) {
		verify(argumentParser).parse(launchArguments);
		verifyNoMoreInteractions(argumentParser);
	}

	private void setUpOutputArgument( final OutputType type ) {
		when(outputTypeArgument.get(anyMapOf(ArgumentKey.class, String.class))).thenReturn(type);
	}

	private void setUpArgumentParserException() {
		when(argumentParser.parse(any(String[].class)))
		        .thenThrow(new IllegalArgumentException(ARGUMENT_PARSER_EXCEPTION_MESSAGE));
	}

	private void setUpStartDateArgumentxception() {
		when(argumentParser.parse(any(String[].class)))
		        .thenThrow(new IllegalArgumentException(START_DATE_ARGUMENT_EXCEPTION_MESSAGE));
	}

	private void setUpEndDateArgumentxception() {
		when(argumentParser.parse(any(String[].class)))
		        .thenThrow(new IllegalArgumentException(END_DATE_ARGUMENT_EXCEPTION_MESSAGE));
	}

	private void setUpTickerSymbolArgumentxception() {
		when(argumentParser.parse(any(String[].class)))
		        .thenThrow(new IllegalArgumentException(TICKER_SYMBOL_ARGUMENT_EXCEPTION_MESSAGE));
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