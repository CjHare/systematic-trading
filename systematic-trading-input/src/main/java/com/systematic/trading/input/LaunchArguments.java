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

import java.util.Map;
import java.util.Optional;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.backtest.input.FileBaseOutputDirectory;
import com.systematic.trading.backtest.input.OutputType;
import com.systematic.trading.data.DataServiceType;

/**
 * An aggregation facade for parsing the arguments given on launch, their validation and type conversion.
 * 
 * @author CJ Hare
 */
public class LaunchArguments {

	enum ArgumentKey {
		DATA_SERVICE_TYPE("-data_service_type"),
		END_DATE("-end_date"),
		EQUITY_DATASET("-equity_dataset"),
		OUTPUT_TYPE("-output"),
		FILE_BASE_DIRECTORY("-output_file_base_directory"),
		START_DATE("-start_date"),
		TICKER_SYMBOL("-ticker_symbol");

		private final String key;

		private ArgumentKey( final String key ) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public static Optional<ArgumentKey> get( final String arg ) {

			for (final ArgumentKey candidate : ArgumentKey.values()) {
				if (candidate.key.equals(arg)) {
					return Optional.of(candidate);
				}
			}

			return Optional.empty();

		}
	}

	/** Data source that will receive the application's output.*/
	private final OutputType outputType;

	/** Optional argument, used with file output types.*/
	private final LaunchArgument<FileBaseOutputDirectory> fileBaseOutputDirectory;

	/** Parsed launch arguments.*/
	private final Map<ArgumentKey, String> arguments;

	/** Mandatory start date for the back test.*/
	private final BacktestStartDate startDate;

	/** Mandatory end date for the back test.*/
	private final BacktestEndDate endDate;

	/** Ticker Symbol to perform the back testing on.*/
	private final TickerSymbol tickerSymbol;

	/**	Optional argument, data set to retrieve the ticker symbol from. */
	private final EquityDataset equityDataset;

	/**	Optional argument, which data source type to use when retrieving data. */
	private final DataServiceType dataService;

	public LaunchArguments( final LaunchArgumentsParser argumentParser, final LaunchArgument<OutputType> outputArgument,
	        final LaunchArgument<DataServiceType> dataServiceArgument,
	        final LaunchArgument<BacktestStartDate> startDateArgument,
	        final LaunchArgument<BacktestEndDate> endDateArgument,
	        final LaunchArgument<EquityDataset> equityDatasetArgument,
	        final LaunchArgument<TickerSymbol> tickerSymbolArgument,
	        final LaunchArgument<FileBaseOutputDirectory> fileBaseOutputDirectoryArgument, final String... args ) {
		this.arguments = argumentParser.parse(args);
		this.outputType = outputArgument.get(arguments);
		this.fileBaseOutputDirectory = fileBaseOutputDirectoryArgument;
		this.startDate = startDateArgument.get(arguments);
		this.endDate = endDateArgument.get(arguments);
		this.equityDataset = equityDatasetArgument.get(arguments);
		this.tickerSymbol = tickerSymbolArgument.get(arguments);
		this.dataService = dataServiceArgument.get(arguments);
	}

	public String getOutputDirectory( final String depositAmount ) {
		return fileBaseOutputDirectory.get(arguments).getDirectory(depositAmount);
	}

	public BacktestStartDate getStartDate() {
		return startDate;
	}

	public BacktestEndDate getEndDate() {
		return endDate;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public TickerSymbol getTickerSymbol() {
		return tickerSymbol;
	}

	public EquityDataset getEquityDataset() {
		return equityDataset;
	}

	public DataServiceType getDataService() {
		return dataService;
	}
}