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
package com.systematic.trading.data.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestEquityPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.data.price.OpeningPrice;

public class TradingDayPricesUtil {

	private static final TradingDayPricesUtil INSTANCE = new TradingDayPricesUtil();

	private TradingDayPricesUtil() {
	}

	public static final TradingDayPricesUtil getInstance() {
		return INSTANCE;
	}

	public TradingDayPrices parseDataPoint( final String tickerSymbol, final Object uncast ) {
		final Object[] data = (Object[]) uncast;
		final LocalDate date = parseDate(data[0]);

		final OpeningPrice openingPrice = OpeningPrice.valueOf(parseBigDecimal(data[3]));
		final LowestPrice lowestPrice = LowestPrice.valueOf(parseBigDecimal(data[1]));
		final HighestEquityPrice highestPrice = HighestEquityPrice.valueOf(parseBigDecimal(data[2]));
		final ClosingPrice closingPrice = ClosingPrice.valueOf(parseBigDecimal(data[4]));

		return new TradingDayPricesImpl(tickerSymbol, date, openingPrice, lowestPrice, highestPrice, closingPrice);
	}

	private LocalDate parseDate( final Object o ) {
		return Date.valueOf(o.toString()).toLocalDate();
	}

	private BigDecimal parseBigDecimal( final Object o ) {
		return BigDecimal.valueOf(Double.valueOf(o.toString()));
	}
}