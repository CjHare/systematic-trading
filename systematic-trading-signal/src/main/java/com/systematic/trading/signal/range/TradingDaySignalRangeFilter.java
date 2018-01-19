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
package com.systematic.trading.signal.range;

import java.time.LocalDate;

import com.systematic.trading.model.price.TradingDayPrices;

/**
 * A filter that is used to exclude the range of trading days when a signal can be generated.
 * 
 * @author CJ Hare
 */
public class TradingDaySignalRangeFilter implements SignalRangeFilter {

	/** Offset applied to the size of array, when converting to index to reference entries. */
	private static final int ZERO_BASED_INDEX_OFFSET = 1;

	/** Number of trading days before the latest (current) trading date to generate signals on. */
	private final int previousTradingDaySignalRange;

	public TradingDaySignalRangeFilter( final int previousTradingDaySignalRange ) {

		this.previousTradingDaySignalRange = previousTradingDaySignalRange;
	}

	@Override
	public LocalDate earliestSignalDate( final TradingDayPrices[] data ) {

		return data[earliestAboveZeroIndex(data)].date();
	}

	@Override
	public LocalDate latestSignalDate( final TradingDayPrices[] data ) {

		return data[latestAboveZeroIndex(data)].date();
	}

	private int latestAboveZeroIndex( final TradingDayPrices[] data ) {

		return aboveZeroIndex(data.length);
	}

	private int earliestAboveZeroIndex( final TradingDayPrices[] data ) {

		return aboveZeroIndex(data.length - previousTradingDaySignalRange);
	}

	private int aboveZeroIndex( final int value ) {

		return Math.max(0, value - ZERO_BASED_INDEX_OFFSET);
	}
}