/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.maths;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.data.price.OpeningPrice;

public class TradingDayPricesImpl implements TradingDayPrices {

	private final LocalDate date;
	private final OpeningPrice openingPrice;
	private final LowestPrice lowestPrice;
	private final HighestPrice highestPrice;
	private final ClosingPrice closingPrice;

	public TradingDayPricesImpl(final LocalDate date, final BigDecimal openingPrice, final BigDecimal lowestPrice,
	        final BigDecimal highestPrice, final BigDecimal closingPrice) {
		this.date = date;
		this.openingPrice = OpeningPrice.valueOf(openingPrice);
		this.lowestPrice = LowestPrice.valueOf(lowestPrice);
		this.highestPrice = HighestPrice.valueOf(highestPrice);
		this.closingPrice = ClosingPrice.valueOf(closingPrice);
	}

	@Override
	public LocalDate getDate() {
		return date;
	}

	@Override
	public ClosingPrice getClosingPrice() {
		return closingPrice;
	}

	@Override
	public LowestPrice getLowestPrice() {
		return lowestPrice;
	}

	@Override
	public HighestPrice getHighestPrice() {
		return highestPrice;
	}

	@Override
	public String getTickerSymbol() {
		return null;
	}

	@Override
	public OpeningPrice getOpeningPrice() {
		return openingPrice;
	}
}
