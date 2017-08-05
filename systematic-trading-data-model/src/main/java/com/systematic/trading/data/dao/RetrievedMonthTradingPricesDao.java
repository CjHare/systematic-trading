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
package com.systematic.trading.data.dao;

import java.util.List;

import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Data Access Object to dealing with whether the trading data for a ticker symbol on any given month, has already been retrieved.
 * 
 * @author CJ Hare
 */
public interface RetrievedMonthTradingPricesDao {

	/**
	 * Creates the entry for the completed retrieval requests.
	 * 
	 * @param requests history retrieval requests that has been completed.
	 */
	void create( List<RetrievedMonthTradingPrices> retrieved );

	/**
	 * Retrieve the months of already obtained price data.
	 * 
	 * @param tickerSymbol symbol of the equity who history to retrieve.
	 * @param startYear beginning year for the range of values to retrieve.
	 * @param endYear last year for the range of values to retrieve.
	 * @return ally full months of already obtained.
	 */
	List<RetrievedMonthTradingPrices> get( String tickerSymbol, int startYear, int endYear );
}