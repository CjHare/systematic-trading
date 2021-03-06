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
package com.systematic.trading.data.api;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * External data source for equity price information.
 * 
 * @author CJ Hare
 */
public interface EquityApi {

	/**
	 * 
	 * @param tickerDataset
	 *            identifier for the source to retrieve the ticker symbol from.
	 * @param symbol
	 *            ticker symbol for the stock to retrieve data on.
	 * @param startDateInclusive
	 *            inclusive start date for the data points.
	 * @param endDateExclusive
	 *            exclusive end date for the data points.
	 * @param throttler
	 *            used to throttle the number of connection to abide by API constraints.
	 * @return the given data parsed into domain objects.
	 * @throws CannotRetrieveDataException
	 *             problem encountered in retrieving the stock data.
	 */
	TradingDayPrices[] stockData(
	        String tickerDataset,
	        String symbol,
	        LocalDate startDateInclusive,
	        LocalDate endDateExclusive,
	        BlockingEventCount throttler ) throws CannotRetrieveDataException;

	/**
	 * Maximum number of time that may be retrieved in one attempt.
	 * 
	 * @return number of days that can be retrieved each attempt.
	 */
	Period maximumDurationPerConnection();

	/**
	 * Number of concurrent calls accepted by the API.
	 * 
	 * @return number of threads that may simultaneously call the API.
	 */
	int maximumConcurrentConnections();

	/**
	 * Maximum number of seconds that a retrieval will take.
	 * 
	 * @return maximum time allowed for a call, worst scenario (i.e. full retries).
	 */
	int maximumRetrievalTimeSeconds();

	/**
	 * Allowed limit on the number of connections to the API.
	 * 
	 * @return number of connections allowed per a rolling second.
	 */
	int maximumConnectionsPerSecond();
}
