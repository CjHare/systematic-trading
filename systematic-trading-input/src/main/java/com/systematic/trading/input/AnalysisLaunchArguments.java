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

import java.math.BigDecimal;
import java.util.Map;

import com.systematic.trading.backtest.equity.TickerSymbol;
import com.systematic.trading.backtest.input.EquityDataset;
import com.systematic.trading.data.DataServiceStructure;
import com.systematic.trading.data.DataServiceType;

/**
 * An aggregation facade for parsing the arguments given on launch, their validation and type
 * conversion.
 * 
 * @author CJ Hare
 */
public class AnalysisLaunchArguments {

	/** Equity and it's data source. */
	private final EquityArguments equityArguments;

	/** Funds contained the cash account to use when opening positions. */
	private final BigDecimal openingFunds;

	public AnalysisLaunchArguments(
	        final EquityArguments equityArguments,
	        final LaunchArgument<BigDecimal> openingFundsArgument,
	        final Map<LaunchArgumentKey, String> arguments ) {

		this.openingFunds = openingFundsArgument.get(arguments);
		this.equityArguments = equityArguments;
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

	public DataServiceStructure dataServiceStructure() {

		return equityArguments.dataServiceStructure();
	}

	public BigDecimal openingFunds() {

		return openingFunds;
	}
}
