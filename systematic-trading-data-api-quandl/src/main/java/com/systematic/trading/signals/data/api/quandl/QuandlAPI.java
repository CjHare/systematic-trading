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
package com.systematic.trading.signals.data.api.quandl;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.signals.data.api.quandl.converter.QuandlResponseConverter;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlApiDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResultSet;

/**
 * Retrieval of equity data from the Quandl data service.
 * 
 * @author CJ Hare
 */
public class QuandlAPI implements EquityApi {

	private final QuandlApiDao dao;

	private final QuandlResponseConverter dataFormat;

	private final Period maximumDurationPerConnection;
	private final int maximumConcurrentConnections;
	private final int maximumRetrievalTimeSeconds;
	private final int maximumConnectionsPerSecond;

	public QuandlAPI( final QuandlApiDao dao, final EquityApiConfiguration configuration,
	        final QuandlResponseConverter dataFormat ) {

		this.dao = dao;
		this.dataFormat = dataFormat;
		this.maximumDurationPerConnection = Period.ofMonths(configuration.maximumMonthsPerConnection());
		this.maximumConcurrentConnections = configuration.maximumConcurrentConnections();
		this.maximumRetrievalTimeSeconds = configuration.maximumRetrievalTimeSeconds();
		this.maximumConnectionsPerSecond = configuration.maximumConcurrentConnections();
	}

	@Override
	public TradingDayPrices[] stockData( final String equityDataset, final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate, final BlockingEventCount throttler )
	        throws CannotRetrieveDataException {

		final QuandlResultSet response = dao.get(equityDataset, tickerSymbol, inclusiveStartDate, exclusiveEndDate,
		        throttler);

		return dataFormat.convert(tickerSymbol, response);
	}

	@Override
	public Period maximumDurationPerConnection() {

		return maximumDurationPerConnection;
	}

	@Override
	public int maximumConcurrentConnections() {

		return maximumConcurrentConnections;
	}

	@Override
	public int maximumRetrievalTimeSeconds() {

		return maximumRetrievalTimeSeconds;
	}

	@Override
	public int maximumConnectionsPerSecond() {

		return maximumConnectionsPerSecond;
	}
}