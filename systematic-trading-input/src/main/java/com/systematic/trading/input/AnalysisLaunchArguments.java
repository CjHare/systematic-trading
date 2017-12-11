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

import java.math.BigDecimal;
import java.util.Map;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * An aggregation facade for parsing the arguments given on launch, their validation and type conversion.
 * 
 * @author CJ Hare
 */
public class AnalysisLaunchArguments {

	/** Ticker Symbol to perform the back testing on.*/
	private final TickerSymbol tickerSymbol;

	/** Funds contained the cash account to use when opening positions. */
	private final BigDecimal openingFunds;

	/**	Optional argument, data set to retrieve the ticker symbol from. */
	private final EquityDataset equityDataset;

	/**	Optional argument, which data source type to use when retrieving data. */
	private final DataServiceType dataService;

	public AnalysisLaunchArguments( final LaunchArgumentsParser argumentParser,
	        final LaunchArgument<DataServiceType> dataServiceArgument,
	        final LaunchArgument<EquityDataset> equityDatasetArgument,
	        final LaunchArgument<TickerSymbol> tickerSymbolArgument,
	        final LaunchArgument<BigDecimal> openingFundsArgument, final String... args ) {

		final Map<ArgumentKey, String> arguments = argumentParser.parse(args);
		this.equityDataset = equityDatasetArgument.get(arguments);
		this.tickerSymbol = tickerSymbolArgument.get(arguments);
		this.dataService = dataServiceArgument.get(arguments);
		this.openingFunds = openingFundsArgument.get(arguments);
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

	public BigDecimal getOpeningFunds() {
		return openingFunds;
	}
}