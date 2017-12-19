/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.model;

import java.time.LocalDate;
import java.util.Map;

import com.systematic.trading.data.TradingDayPrices;

/**
 * Details for the trading range, per a specific ticker symbol.
 * 
 * @author CJ Hare
 */
public interface TickerSymbolTradingData {

	/**
	 * Identity of the equity across the universe of equities.
	 * 
	 * @return never <code>null</code>.
	 */
	EquityIdentity equityIdentity();

	/**
	 * Marks the beginning of the trading data range.
	 * 
	 * @return Inclusive date for the beginning of the data set.
	 */
	LocalDate earliestDate();

	/**
	 * Marks the end of the trading data range.
	 * 
	 * @return Inclusive date for the end of the data set.
	 */
	LocalDate latestDate();

	/**
	 * Retrieve the number of trading prices data points.
	 * 
	 * @return the number of trading data points within the defined start and end dates.
	 */
	int requiredTradingPrices();

	/**
	 * Retrieves the map of trading data points to their prices.
	 * 
	 * @return trading prices keyed by the date they correspond with.
	 */
	Map<LocalDate, TradingDayPrices> tradingPrices();
}
