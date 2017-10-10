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
package com.systematic.trading.signals.model.filter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * Simply passes through every indicator signal as a buy signal.
 * 
 * @author CJ Hare
 */
public class AnyIndicatorIsBuySignalFilter implements SignalFilter {

	@Override
	/**
	 * Just pass through the indicators given as signals
	 */
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalId, List<IndicatorSignal>> signals,
	        final Comparator<BuySignal> ordering, final LocalDate latestTradingDate ) {

		final SortedSet<BuySignal> passedSignals = new TreeSet<>(ordering);

		for (final List<IndicatorSignal> values : signals.values()) {
			for (final IndicatorSignal signal : values) {
				passedSignals.add(new BuySignal(signal.getDate()));
			}
		}

		return passedSignals;
	}
}