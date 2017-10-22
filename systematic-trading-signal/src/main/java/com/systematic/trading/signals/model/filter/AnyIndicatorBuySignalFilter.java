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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class AnyIndicatorBuySignalFilter implements SignalFilter {

	/** Ordering of the generated signals. */
	private final Comparator<BuySignal> signalOrdering;

	/** When there is at least one of these signals on a date, it's a buy. */
	private final IndicatorSignalId[] indicators;

	public AnyIndicatorBuySignalFilter( final Comparator<BuySignal> signalOrdering,
	        final IndicatorSignalId... indicators ) {
		
		//TODO use validator
		
		this.signalOrdering = signalOrdering;
		this.indicators = indicators;
	}

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalId, List<IndicatorSignal>> signals,
	        final LocalDate latestTradingDate ) {
		final Set<LocalDate> signalDates = getSignalDates(signals);

		final SortedSet<BuySignal> passedSignals = new TreeSet<>(signalOrdering);
		for (final LocalDate signalDate : signalDates) {
			passedSignals.add(new BuySignal(signalDate));
		}

		return passedSignals;
	}

	private Set<LocalDate> getSignalDates( final Map<IndicatorSignalId, List<IndicatorSignal>> signals ) {
		final Set<LocalDate> signalDates = new HashSet<>();

		for (final IndicatorSignalId indicator : indicators) {
			for (final IndicatorSignal signal : signals.get(indicator)) {
				signalDates.add(signal.getDate());
			}
		}

		return signalDates;
	}
}