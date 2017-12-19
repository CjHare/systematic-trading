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
package com.systematic.trading.data.dao;

import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;

/**
 * Trading Day Prices DAO.
 * 
 * @author CJ Hare
 */
public interface TradingDayPricesDao {

	/**
	 * Inserts a new DataPoint into it's appropriate ticker symbol table.
	 * 
	 * @param data to create.
	 * @return The number of entities updated or deleted.
	 */
	void create( TradingDayPrices data );

	/**
	 * Inserts DataPoint into the appropriate ticker symbol table.
	 * 
	 * @param data to create.
	 * @param session transaction to perform he insert query within.
	 * @return The number of entities updated or deleted.
	 */
	void create( TradingDayPrices[] data );

	/**
	 * If not already present creates the appropriate data point table.
	 * 
	 * @param tickerSymbol to create.
	 * @return The number of entities updated or deleted.
	 */
	void createTableIfAbsent( String tickerSymbol );

	/**
	 * Retrieves the set of data points within the given dates.
	 * 
	 * @param tickerSymbol symbol of the equity to retrieve.
	 * @param startDate inclusive beginning date for the data range.
	 * @param endDate inclusive end date for the data range.
	 * @return the set of trading data points within the given dates.
	 */
	TradingDayPrices[] prices( String tickerSymbol, LocalDate startDate, LocalDate endDate );

	/**
	 * Counts the number of data points within the given range
	 * 
	 * @param tickerSymbol symbol of the equity to retrieve.
	 * @param startDate inclusive beginning date for the data range.
	 * @param endDate inclusive end date for the data range.
	 * @return the number of trading data points within the given dates.
	 */
	long count( String tickerSymbol, LocalDate startDate, LocalDate endDate );

	/**
	 * Retrieves the most recent data point since the given date.
	 * 
	 * @param tickerSymbol symbol of the equity to retrieve.
	 * @return the most trading data point since the given date, or <code>null</code> if there are
	 *         none or the table does not exist.
	 */
	TradingDayPrices mostRecent( String tickerSymbol );
}
