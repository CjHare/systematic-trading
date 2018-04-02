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
import java.util.Map;
import java.util.TreeMap;

import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.model.price.impl.TradingDayPricesImpl;
import com.systematic.trading.signals.data.api.alpha.vantage.resource.TradingDayResource;

/**
 * AlphaVantageResponseConverter converts the standard response from the Alpha Vantage API into the
 * TradingDayPrices structure used by Systematic Trading.
 * 
 * @author CJ Hare
 */
public class AlphaVantageResponseConverter {

	private static final DateTimeFormatter ALPHA_VANTAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final int TWO_DECIMAL_PLACES = 2;

	public TradingDayPrices[] convert( final String tickerSymbol, final Map<String, TradingDayResource> dataset ) {

		final TreeMap<LocalDate, TradingDayPrices> prices = new TreeMap<>();

		for (final Map.Entry<String, TradingDayResource> dayPrices : dataset.entrySet()) {

			final LocalDate tradingDate = tradingDate(dayPrices.getKey());
			final TradingDayResource tradingDay = dayPrices.getValue();

			prices.put(
			        tradingDate,
			        new TradingDayPricesImpl(
			                tickerSymbol,
			                tradingDate,
			                price(tradingDay.open()),
			                price(tradingDay.low()),
			                price(tradingDay.high()),
			                price(tradingDay.close())));
		}

		return prices.values().toArray(new TradingDayPrices[0]);
	}

	private LocalDate tradingDate( final String date ) {

		return LocalDate.parse(date, ALPHA_VANTAGE_DATE_FORMAT);
	}

	private BigDecimal price( final String price ) {

		return new BigDecimal(price).setScale(TWO_DECIMAL_PLACES, RoundingMode.HALF_EVEN);
	}
}
