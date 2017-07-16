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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.data.impl.TradingDayPricesImpl;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.ColumnResource;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Retrieval of equity data from the Quandl data service.
 * 
 * @author CJ Hare
 */
public class QuandlAPI implements EquityApi {
	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final QuandlDao dao = new QuandlDao();

	@Override
	public TradingDayPrices[] getStockData( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		final QuandlResponseResource response = dao.get(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
		return convertResponse(tickerSymbol, response.getDatatable().getColumns(), response.getDatatable().getData());
	}

	//TODO check if the service is up

	//TODO test call, ensure the data return is working

	//TODO add a connection limit & use that

	//TODO this should not be used, split the calls into may simultaneous calls instead, i.e. years / months - based on connection limit
	@Override
	public Period getMaximumDurationInSingleUpdate() {

		return Period.ofYears(10);
	}

	/**
	 * Verifies the expected data is present and converts into JSON data into the domain model.
	 */
	private TradingDayPrices[] convertResponse( final String tickerSymbol, final List<ColumnResource> columns,
	        final List<List<Object>> data ) throws CannotRetrieveDataException {
		final TreeMap<LocalDate, TradingDayPrices> prices = new TreeMap<LocalDate, TradingDayPrices>();

		for (final List<Object> tuple : data) {
			final LocalDate tradingDate = getTradingDate(columns, tuple);

			prices.put(tradingDate, new TradingDayPricesImpl(tickerSymbol, tradingDate, getOpeningPrice(columns, tuple),
			        getLowestPrice(columns, tuple), getHighestPrice(columns, tuple), getClosingPrice(columns, tuple)));
		}

		return prices.values().toArray(new TradingDayPrices[0]);

	}

	//TODO move this around
	private static final String NAME_DATE = "date";
	private static final String NAME_OPEN_PRICE = "open";
	private static final String NAME_HIGH_PRICE = "high";
	private static final String NAME_LOW_PRICE = "low";
	private static final String NAME_CLOSE_PRICE = "close";

	private BigDecimal getOpeningPrice( final List<ColumnResource> columns, final List<Object> tuple )
	        throws CannotRetrieveDataException {
		return new BigDecimal((Double) tuple.get(getIndexOf(columns, NAME_OPEN_PRICE)));
	}

	private BigDecimal getLowestPrice( final List<ColumnResource> columns, final List<Object> tuple )
	        throws CannotRetrieveDataException {
		return new BigDecimal((Double) tuple.get(getIndexOf(columns, NAME_LOW_PRICE)));
	}

	private BigDecimal getHighestPrice( final List<ColumnResource> columns, final List<Object> tuple )
	        throws CannotRetrieveDataException {
		return new BigDecimal((Double) tuple.get(getIndexOf(columns, NAME_HIGH_PRICE)));
	}

	private BigDecimal getClosingPrice( final List<ColumnResource> columns, final List<Object> tuple )
	        throws CannotRetrieveDataException {
		return new BigDecimal((Double) tuple.get(getIndexOf(columns, NAME_CLOSE_PRICE)));
	}

	private LocalDate getTradingDate( final List<ColumnResource> columns, final List<Object> tuple )
	        throws CannotRetrieveDataException {
		return LocalDate.parse((String) tuple.get(getIndexOf(columns, NAME_DATE)), QUANDL_DATE_FORMAT);
	}

	private int getIndexOf( final List<ColumnResource> columns, final String name ) throws CannotRetrieveDataException {
		for (int i = 0; i < columns.size(); i++) {
			if (StringUtils.equals(name, columns.get(i).getName())) {
				return i;
			}
		}

		throw new CannotRetrieveDataException(String.format("Missing an expected column: %s", name));
	}
}