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
package com.systematic.trading.maths;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestEquityPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.data.price.OpeningPrice;

/**
 * Data object pairing a date and a value.
 * 
 * @author CJ Hare
 */
public class DatedValue implements TradingDayPrices {

	private final LocalDate date;
	private final BigDecimal value;

	public DatedValue( final LocalDate date, final BigDecimal value ) {

		if (date == null || value == null) {
			throw new IllegalArgumentException(
			        String.format("Expecting non null date and value, given Date: %s and Value: %s", date, value));
		}

		this.date = date;
		this.value = value;
	}

	@Override
	public LocalDate date() {

		return date;
	}

	public BigDecimal value() {

		return value;
	}

	@Override
	public ClosingPrice closingPrice() {

		// Immutable value is verified as non-null in constructor
		return ClosingPrice.valueOf(value);
	}

	@Override
	public LowestPrice lowestPrice() {

		throw new UnsupportedOperationException();
	}

	@Override
	public HighestEquityPrice highestPrice() {

		throw new UnsupportedOperationException();
	}

	@Override
	public OpeningPrice openingPrice() {

		throw new UnsupportedOperationException();
	}

	@Override
	public String tickerSymbol() {

		throw new UnsupportedOperationException();
	}
}
