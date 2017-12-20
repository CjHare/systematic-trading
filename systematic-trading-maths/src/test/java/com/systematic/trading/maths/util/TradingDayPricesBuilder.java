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
package com.systematic.trading.maths.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;

/**
 * Convenience utility for creating concrete TradingDayPrices.
 * 
 * @author CJ Hare
 */
public class TradingDayPricesBuilder {

	private static final String NO_TICKER_SYMBOL = null;

	private LocalDate tradingDate;
	private double openingPrice;
	private double lowestPrice;
	private double highestPrice;
	private double closingPrice;

	public TradingDayPricesBuilder withOpeningPrice( final double openingPrice ) {

		this.openingPrice = openingPrice;
		return this;
	}

	public TradingDayPricesBuilder withLowestPrice( final double lowestPrice ) {

		this.lowestPrice = lowestPrice;
		return this;
	}

	public TradingDayPricesBuilder withHighestPrice( final double highestPrice ) {

		this.highestPrice = highestPrice;
		return this;
	}

	public TradingDayPricesBuilder withClosingPrice( final double closingPrice ) {

		this.closingPrice = closingPrice;
		return this;
	}

	public TradingDayPricesBuilder withTradingDate( final LocalDate tradingDate ) {

		this.tradingDate = tradingDate;
		return this;
	}

	public TradingDayPrices build() {

		return new TradingDayPricesImpl(NO_TICKER_SYMBOL, tradingDate(), BigDecimal.valueOf(openingPrice),
		        BigDecimal.valueOf(lowestPrice), BigDecimal.valueOf(highestPrice), BigDecimal.valueOf(closingPrice));
	}

	private LocalDate tradingDate() {

		return tradingDate == null ? LocalDate.now() : tradingDate;
	}
}