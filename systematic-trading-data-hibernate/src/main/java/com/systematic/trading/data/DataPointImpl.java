/**
 * Copyright (c) 2015, CJ Hare
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
package com.systematic.trading.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DataPointImpl implements DataPoint {

	private final String tickerSymbol;
	private final LocalDate date;
	private final BigDecimal closingPrice;
	private final BigDecimal lowestPrice;
	private final BigDecimal highestPrice;

	public DataPointImpl( final String tickerSymbol, final LocalDate date, final BigDecimal lowestPrice,
			final BigDecimal highestPrice, final BigDecimal closingPrice ) {
		this.tickerSymbol = tickerSymbol;
		this.date = date;
		this.closingPrice = closingPrice;
		this.lowestPrice = lowestPrice;
		this.highestPrice = highestPrice;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getClosingPrice() {
		return closingPrice;
	}

	public BigDecimal getLowestPrice() {
		return lowestPrice;
	}

	public BigDecimal getHighestPrice() {
		return highestPrice;
	}

	public String getTickerSymbol() {
		return tickerSymbol;
	}
}
