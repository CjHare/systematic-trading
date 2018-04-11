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
package com.systematic.trading.signals.data.api.alpha.vantage.dao;

import java.time.LocalDate;

import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * Data Access Object for retrieving data from the AlphaVantage API.
 * 
 * DAO's responsibility is ensure the AlphaVantage reply contains the expected JSON format, not the
 * data
 * integrity.
 * 
 * @author CJ Hare
 */
public interface AlphaVantageApiDao {

	/**
	 * Retrieve historical equity price data from AlphaVantage.
	 * 
	 * @param equityDataset
	 *            identifier for the dataset to retrieve the ticker symbol from e.g. AX for the ASX.
	 * @param tickerSymbol
	 *            identifier of the equity to retrieve.
	 * @param inclusiveStartDate
	 *            the first day of the historical data to retrieve.
	 * @param inclusiveEndDate
	 *            the last day of the historical data to retrieve.
	 * @param throttler
	 *            synchronization object to limit the connections to the AlphaVantage API.
	 * @return retrieved AlphaVantage data.
	 * @throws CannotRetrieveDataException
	 *             problem encountered during connecting to the AlphaVantage API.
	 */
	TradingDayPrices[] get(
	        String equityDataset,
	        String tickerSymbol,
	        LocalDate inclusiveStartDate,
	        LocalDate inclusiveEndDate,
	        BlockingEventCount throttler ) throws CannotRetrieveDataException;
}
