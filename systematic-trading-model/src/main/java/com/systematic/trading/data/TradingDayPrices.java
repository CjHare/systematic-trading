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
package com.systematic.trading.data;

import java.time.LocalDate;

import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.data.price.HighestEquityPrice;
import com.systematic.trading.data.price.LowestPrice;
import com.systematic.trading.data.price.OpeningPrice;

/**
 * Data points for a single day of trading.
 * 
 * @author CJ Hare
 */
public interface TradingDayPrices {

	/**
	 * The calendar date for the day's trading.
	 * 
	 * @return date of the trading data.
	 */
	LocalDate date();

	/**
	 * Price at the market close.
	 * 
	 * @return price of the equity at the close of trading.
	 */
	ClosingPrice closingPrice();

	/**
	 * Lowest price of the equity during the day.
	 * 
	 * @return lowest price of the equity during the day's trading.
	 */
	LowestPrice lowestPrice();

	/**
	 * Highest price of the equity during the day.
	 * 
	 * @return highest price of the equity during the day's trading.
	 */
	HighestEquityPrice highestPrice();

	/**
	 * Price of the equity at the start of trading.
	 * 
	 * @return opening price of the equity during the day's trading.
	 */
	OpeningPrice openingPrice();

	/**
	 * The abbreviation used in markets for the equity.
	 * 
	 * @return market abbreviation.
	 */
	String tickerSymbol();
}
