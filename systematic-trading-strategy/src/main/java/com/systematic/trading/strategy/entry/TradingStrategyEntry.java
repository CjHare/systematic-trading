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
package com.systematic.trading.strategy.entry;

import java.util.List;

import com.systematic.trading.collection.LimitedSizeQueue;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.model.DatedSignal;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.strategy.definition.Entry;
import com.systematic.trading.strategy.definition.StrategyEntry;

/**
 * Trading Strategy entry based a single indicator.
 * 
 * @author CJ Hare
 */
public class TradingStrategyEntry implements StrategyEntry {

	/** Entry that performs the signal generation. */
	private final Entry entry;

	/** The trading data as it rolled through the set. */
	private final LimitedSizeQueue<TradingDayPrices> tradingData;

	public TradingStrategyEntry( final Entry entry ) {
		this.entry = entry;

		this.tradingData = new LimitedSizeQueue<>(TradingDayPrices.class,
		        entry.getMaximumNumberOfTradingDaysRequired());
	}

	@Override
	public boolean entryTick( final BrokerageTransactionFee fees, final CashAccount cashAccount,
	        final TradingDayPrices data ) {

		// Add the day's data to the rolling queue
		tradingData.add(data);

		//TODO change to avoid converting to a list
		// Create signals from the available trading data
		return entry.analyse(tradingData.toArray());
	}
}