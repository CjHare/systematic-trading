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
package com.systematic.trading.signals.data.api.quandl.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.data.impl.TradingDayPricesImpl;

/**
 * How to make sense and extra data from the Quandl Resources.
 * 
 * @author CJ Hare
 */
public class QuandlResponseFormat {
	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String NAME_DATE = "date";
	private static final String NAME_OPEN_PRICE = "open";
	private static final String NAME_HIGH_PRICE = "high";
	private static final String NAME_LOW_PRICE = "low";
	private static final String NAME_CLOSE_PRICE = "close";
	private static final int TWO_DECIMAL_PLACES = 2;

	/**
	 * Verifies the expected data is present and converts into JSON data into the domain model.
	 */
	public TradingDayPrices[] convert( final String tickerSymbol, final DatatableResource datatable )
	        throws CannotRetrieveDataException {
		final TreeMap<LocalDate, TradingDayPrices> prices = new TreeMap<LocalDate, TradingDayPrices>();
		final List<ColumnResource> columns = datatable.getColumns();
		final List<List<Object>> data = datatable.getData();

		final int dateIndex = getIndexOf(columns, NAME_DATE);
		final int openPriceIndex = getIndexOf(columns, NAME_OPEN_PRICE);
		final int highPriceIndex = getIndexOf(columns, NAME_HIGH_PRICE);
		final int lowPriceIndex = getIndexOf(columns, NAME_LOW_PRICE);
		final int closePriceIndex = getIndexOf(columns, NAME_CLOSE_PRICE);

		for (final List<Object> tuple : data) {
			final LocalDate tradingDate = getTradingDate(tuple.get(dateIndex));

			prices.put(tradingDate,
			        new TradingDayPricesImpl(tickerSymbol, tradingDate, getgPrice(tuple.get(openPriceIndex)),
			                getgPrice(tuple.get(lowPriceIndex)), getgPrice(tuple.get(highPriceIndex)),
			                getgPrice(tuple.get(closePriceIndex))));
		}

		return prices.values().toArray(new TradingDayPrices[0]);

	}

	private BigDecimal getgPrice( final Object price ) throws CannotRetrieveDataException {
		return new BigDecimal((Double) price).setScale(TWO_DECIMAL_PLACES, RoundingMode.HALF_EVEN);
	}

	private LocalDate getTradingDate( final Object date ) throws CannotRetrieveDataException {
		return LocalDate.parse((String) date, QUANDL_DATE_FORMAT);
	}

	private int getIndexOf( final List<ColumnResource> columns, final String name ) throws CannotRetrieveDataException {
		for (int i = 0; i < columns.size(); i++) {
			if (StringUtils.equals(name, columns.get(i).getName())) {
				return i;
			}
		}

		throw new CannotRetrieveDataException(String.format("Missing expected column: %s", name));
	}

}