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
package com.systematic.trading.strategy.periodic;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Periodic signal creation at frequency intervals.
 * 
 * @author CJ Hare
 */
public class TradingStrategyPeriodic implements Periodic {

	/** How often to create signals. */
	private final Period frequency;

	/** The type of signal the periodic is generating. */
	private final SignalType type;

	/** The last date purchase order was created. */
	private LocalDate lastOrder;

	public TradingStrategyPeriodic( final LocalDate firstOrder, final Period frequency, final SignalType type ) {

		this.frequency = frequency;
		this.type = type;

		// The first order needs to be on that date, not interval after
		this.lastOrder = LocalDate.from(firstOrder).minus(frequency);
	}

	@Override
	public List<DatedSignal> analyse( final TradingDayPrices[] data ) {

		List<DatedSignal> signals = new ArrayList<>(1);

		if (hasPrices(data)) {
			final LocalDate tradingDate = data[data.length - 1].date();

			if (isOrderTime(tradingDate)) {
				updateLastOrder(tradingDate);
				signals.add(new DatedSignal(tradingDate, type));
			}
		}

		return signals;
	}

	/**
	 * Full intervals to bring the date as close to today as possible, without going beyond.
	 */
	private void updateLastOrder( final LocalDate today ) {

		while (lastOrder.isBefore(today)) {
			lastOrder = lastOrder.plus(frequency);
		}

		lastOrder = lastOrder.minus(frequency);
	}

	private boolean hasPrices( final TradingDayPrices[] data ) {

		return data.length > 0;
	}

	private boolean isOrderTime( final LocalDate tradingDate ) {

		return tradingDate.isAfter(lastOrder.plus(frequency));
	}
}
