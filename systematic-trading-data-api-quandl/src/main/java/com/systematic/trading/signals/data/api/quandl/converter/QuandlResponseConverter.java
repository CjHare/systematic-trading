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
package com.systematic.trading.signals.data.api.quandl.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.data.impl.TradingDayPricesImpl;
import com.systematic.trading.signals.data.api.quandl.resource.ColumnResource;
import com.systematic.trading.signals.data.api.quandl.resource.DatatableResource;

/**
 * How to make sense and extra data from the Quandl Resources.
 * 
 * @author CJ Hare
 */
public class QuandlResponseConverter {
	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int TWO_DECIMAL_PLACES = 2;

	private final AllResponseColumns allColumns = new AllResponseColumns();
	private DateValueResponseColumns dateValueColumns = new DateValueResponseColumns();

	/**
	 * Verifies the expected data is present and converts into JSON data into the domain model.
	 */
	public TradingDayPrices[] convert( final String tickerSymbol, final DatatableResource datatable )
	        throws CannotRetrieveDataException {

		final TreeMap<LocalDate, TradingDayPrices> prices = new TreeMap<>();
		final List<ColumnResource> columns = datatable.getColumns();
		final List<List<Object>> data = datatable.getData();
		final ResponseColumns mapping = getColumnMapping(columns);
		final int dateIndex = mapping.dateIndex(columns);
		final int openPriceIndex = mapping.openPriceIndex(columns);
		final int highPriceIndex = mapping.highPriceIndex(columns);
		final int lowPriceIndex = mapping.lowPriceIndex(columns);
		final int closePriceIndex = mapping.closePriceIndex(columns);

		for (final List<Object> tuple : data) {
			final LocalDate tradingDate = getTradingDate(tuple.get(dateIndex));

			prices.put(tradingDate,
			        new TradingDayPricesImpl(tickerSymbol, tradingDate, getgPrice(tuple.get(openPriceIndex)),
			                getgPrice(tuple.get(lowPriceIndex)), getgPrice(tuple.get(highPriceIndex)),
			                getgPrice(tuple.get(closePriceIndex))));
		}

		return prices.values().toArray(new TradingDayPrices[0]);
	}

	private ResponseColumns getColumnMapping( final List<ColumnResource> columns ) {

		if (allColumns.canParse(columns)) {
			return allColumns;
		}

		return dateValueColumns;
	}

	private BigDecimal getgPrice( final Object price ) {
		return new BigDecimal((Double) price).setScale(TWO_DECIMAL_PLACES, RoundingMode.HALF_EVEN);
	}

	private LocalDate getTradingDate( final Object date ) {
		return LocalDate.parse((String) date, QUANDL_DATE_FORMAT);
	}
}