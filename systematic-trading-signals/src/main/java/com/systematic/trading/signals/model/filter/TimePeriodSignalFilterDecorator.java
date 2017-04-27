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

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Decorator to apply to filters that excludes signals dated outside a fixed time period.
 * <p/>
 * Restriction on date is useful when applying a warm up to the indicators, where some may fire off
 * during that warm up period.
 * 
 * @author CJ Hare
 */
public class TimePeriodSignalFilterDecorator implements SignalFilter {

	/** Signal filter that has the date restriction applied. */
	private final SignalFilter filter;

	/** Starting book mark for the acceptable signals. */
	private final LocalDate startDateInclusive;

	/** Terminating book end for the acceptable signals. */
	private final LocalDate endDateInclusive;

	public TimePeriodSignalFilterDecorator( final SignalFilter filter, final LocalDate startDateInclusive,
	        final LocalDate endDateInclusive ) {
		this.startDateInclusive = startDateInclusive;
		this.endDateInclusive = endDateInclusive;
		this.filter = filter;
	}

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalType, List<IndicatorSignal>> signals,
	        final Comparator<BuySignal> ordering, final LocalDate latestTradingDate ) {

		final SortedSet<BuySignal> signalSet = filter.apply(signals, ordering, latestTradingDate);

		final Set<BuySignal> toRemove = new HashSet<>();

		for (final BuySignal signal : signalSet) {

			// When the date is outside the desired range remove the signal
			if (startDateInclusive.isAfter(signal.getDate()) || endDateInclusive.isBefore(signal.getDate())) {
				toRemove.add(signal);
			}
		}

		signalSet.removeAll(toRemove);

		return signalSet;
	}
}