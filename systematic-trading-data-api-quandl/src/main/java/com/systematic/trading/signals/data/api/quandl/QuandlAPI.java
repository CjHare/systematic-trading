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
package com.systematic.trading.signals.data.api.quandl;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseFormat;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Retrieval of equity data from the Quandl data service.
 * 
 * @author CJ Hare
 */
public class QuandlAPI implements EquityApi {
	private static final Period MAXIMUM_RETRIEVAL_PER_CALL = Period.ofYears(1);

	private final QuandlDao dao;

	private final QuandlResponseFormat dataFormat;

	public QuandlAPI( final QuandlDao dao, final QuandlResponseFormat dataFormat ) {
		this.dao = dao;
		this.dataFormat = dataFormat;
	}

	@Override
	public TradingDayPrices[] getStockData( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		final QuandlResponseResource response = dao.get(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
		return dataFormat.convert(tickerSymbol, response.getDatatable());
	}

	//TODO retry behaviour, on certain HTTP problems

	//TODO add a connection limit & use that

	//TODO this should not be used, split the calls into may simultaneous calls instead, i.e. years / months - based on connection limit
	@Override
	public Period getMaximumDurationInSingleUpdate() {

		//TODO when too much / too big a payload is returned, threads hang waiting for responses (i.e. 1yr not 10yrs)
		return MAXIMUM_RETRIEVAL_PER_CALL;
	}
}