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

import java.math.BigDecimal;
import java.util.Map;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.backtest.input.FileBaseOutputDirectory;
import com.systematic.trading.backtest.input.OutputType;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * An aggregation facade for parsing the arguments given on launch, their validation and type conversion.
 * 
 * @author CJ Hare
 */
public class BacktestLaunchArguments {

	/** Equity and it's data source. */
	private final EquityArguments equityArguments;

	/** Data source that will receive the application's output. */
	private final OutputType outputType;

	/** Optional argument, used with file output types. */
	private final LaunchArgument<FileBaseOutputDirectory> fileBaseOutputDirectory;

	/** Parsed launch arguments. */
	private final Map<ArgumentKey, String> arguments;

	/** Mandatory start date for the back test. */
	private final BacktestStartDate startDate;

	/** Mandatory end date for the back test. */
	private final BacktestEndDate endDate;

	/** Funds contained the cash account to use when opening positions. */
	private final BigDecimal openingFunds;

	public BacktestLaunchArguments( final LaunchArgument<OutputType> outputArgument,
	        final EquityArguments equityArguments, final LaunchArgument<BigDecimal> openingFundsArgument,
	        final LaunchArgument<BacktestStartDate> startDateArgument,
	        final LaunchArgument<BacktestEndDate> endDateArgument,
	        final LaunchArgument<FileBaseOutputDirectory> fileBaseOutputDirectoryArgument,
	        final Map<ArgumentKey, String> arguments ) {
		this.arguments = arguments;
		this.openingFunds = openingFundsArgument.get(arguments);
		this.outputType = outputArgument.get(arguments);
		this.fileBaseOutputDirectory = fileBaseOutputDirectoryArgument;
		this.startDate = startDateArgument.get(arguments);
		this.endDate = endDateArgument.get(arguments);
		this.equityArguments = equityArguments;
	}

	public String outputDirectory( final String depositAmount ) {

		return fileBaseOutputDirectory.get(arguments).directory(depositAmount);
	}

	public BacktestStartDate startDate() {

		return startDate;
	}

	public BacktestEndDate endDate() {

		return endDate;
	}

	public OutputType outputType() {

		return outputType;
	}

	public TickerSymbol tickerSymbol() {

		return equityArguments.tickerSymbol();
	}

	public EquityDataset equityDataset() {

		return equityArguments.equityDataset();
	}

	public DataServiceType dataService() {

		return equityArguments.dataService();
	}

	public BigDecimal openingFunds() {

		return openingFunds;
	}
}