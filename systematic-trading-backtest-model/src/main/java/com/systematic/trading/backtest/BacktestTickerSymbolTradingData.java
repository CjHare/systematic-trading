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
package com.systematic.trading.backtest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;

/**
 * Summary details for the trading data used for a single ticker symbol.
 * 
 * @author CJ Hare
 */
public class BacktestTickerSymbolTradingData implements TickerSymbolTradingData {

	/** Equity being subjected to back testing. */
	private final EquityIdentity equity;

	/** Inclusive beginning date for the set of trading prices. */
	private final LocalDate earliestDate;

	/** Inclusive last date for the set of trading prices. */
	private final LocalDate latestDate;

	/** The trading data to feed into the simulation. */
	private final Map<LocalDate, TradingDayPrices> tradingData;

	/**
	 * Restrictions of no duplicate trading dates, applies date ordering on the given data.
	 */
	public BacktestTickerSymbolTradingData( final EquityIdentity equity, final TradingDayPrices[] data ) {
		this.equity = equity;

		final Map<LocalDate, TradingDayPrices> modifiableTradingData = new HashMap<>();

		for (final TradingDayPrices tradingDay : data) {
			modifiableTradingData.put(tradingDay.date(), tradingDay);
		}

		if (modifiableTradingData.isEmpty()) {
			throw new IllegalArgumentException("Requires at least one trading day of data");
		}

		if (modifiableTradingData.size() != data.length) {
			throw new IllegalArgumentException("Duplicate trading dates provided");
		}

		this.tradingData = Collections.unmodifiableMap(modifiableTradingData);
		this.earliestDate = earliestDate(tradingData);
		this.latestDate = latestDate(tradingData);
	}

	@Override
	public LocalDate earliestDate() {

		return earliestDate;
	}

	@Override
	public LocalDate latestDate() {

		return latestDate;
	}

	@Override
	public int requiredTradingPrices() {

		return tradingData.size();
	}

	@Override
	public Map<LocalDate, TradingDayPrices> tradingPrices() {

		return tradingData;
	}

	@Override
	public EquityIdentity equityIdentity() {

		return equity;
	}

	private LocalDate earliestDate( final Map<LocalDate, TradingDayPrices> tradingData ) {

		LocalDate earliest = tradingData.values().iterator().next().date();

		for (final TradingDayPrices contender : tradingData.values()) {
			if (contender.date().isBefore(earliest)) {
				earliest = contender.date();
			}
		}

		return earliest;
	}

	private LocalDate latestDate( final Map<LocalDate, TradingDayPrices> tradingData ) {

		LocalDate latest = tradingData.values().iterator().next().date();

		for (final TradingDayPrices contender : tradingData.values()) {
			if (contender.date().isAfter(latest)) {
				latest = contender.date();
			}
		}

		return latest;
	}
}