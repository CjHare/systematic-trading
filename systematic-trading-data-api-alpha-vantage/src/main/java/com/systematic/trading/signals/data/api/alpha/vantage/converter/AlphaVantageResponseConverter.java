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
package com.systematic.trading.signals.data.api.alpha.vantage.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.model.price.impl.TradingDayPricesImpl;
import com.systematic.trading.signals.data.api.alpha.vantage.model.AlphaVantageColumnName;
import com.systematic.trading.signals.data.api.alpha.vantage.model.AlphaVantageResultSet;

/**
 * How to make sense and extra data from the AlphaVantage Resources.
 * 
 * @author CJ Hare
 */
public class AlphaVantageResponseConverter {

	private static final DateTimeFormatter QUANDL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int TWO_DECIMAL_PLACES = 2;

	private final AllResponseColumns allColumns = new AllResponseColumns();

	/**
	 * Verifies the expected data is present and converts into JSON data into the domain model.
	 */
	public TradingDayPrices[] convert( final String tickerSymbol, final AlphaVantageResultSet resultSet )
	        throws CannotRetrieveDataException {

		final TreeMap<LocalDate, TradingDayPrices> prices = new TreeMap<>();
		final List<AlphaVantageColumnName> columns = resultSet.columns();
		final List<List<Object>> data = resultSet.data();
		final int dateIndex = allColumns.dateIndex(columns);
		final int openPriceIndex = allColumns.openPriceIndex(columns);
		final int highPriceIndex = allColumns.highPriceIndex(columns);
		final int lowPriceIndex = allColumns.lowPriceIndex(columns);
		final int closePriceIndex = allColumns.closePriceIndex(columns);

		for (final List<Object> tuple : data) {
			final LocalDate tradingDate = tradingDate(tuple.get(dateIndex));

			prices.put(tradingDate,
			        new TradingDayPricesImpl(tickerSymbol, tradingDate, price(tuple.get(openPriceIndex)),
			                price(tuple.get(lowPriceIndex)), price(tuple.get(highPriceIndex)),
			                price(tuple.get(closePriceIndex))));
		}

		return prices.values().toArray(new TradingDayPrices[0]);
	}

	private BigDecimal price( final Object price ) {

		return new BigDecimal((Double) price).setScale(TWO_DECIMAL_PLACES, RoundingMode.HALF_EVEN);
	}

	private LocalDate tradingDate( final Object date ) {

		return LocalDate.parse((String) date, QUANDL_DATE_FORMAT);
	}
}