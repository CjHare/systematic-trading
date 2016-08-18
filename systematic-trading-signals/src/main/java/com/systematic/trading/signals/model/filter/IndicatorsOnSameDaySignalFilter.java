/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Signal generated when there is a RSI and MACD filter on the same day.
 * 
 * @author CJ Hare
 */
public class IndicatorsOnSameDaySignalFilter implements SignalFilter {

	/** The signals we will be looking for on each application. */
	private IndicatorSignalType[] indicators;

	/**
	 * @param indicators all the indicators expected in an application, those all required on the
	 *            same date to pass the filtering.
	 */
	public IndicatorsOnSameDaySignalFilter(final IndicatorSignalType... indicators) {

		// There'll be NullPointers unless we have an array of IndicatorSignalType
		if (indicators == null || indicators.length == 0) {
			throw new IllegalArgumentException("Expecting at least one IndicatorSignalType");
		}

		this.indicators = indicators;
	}

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalType, List<IndicatorSignal>> signals,
	        final Comparator<BuySignal> ordering, final LocalDate latestTradingDate ) {
		validateInput(signals);

		final SortedSet<BuySignal> passedSignals = new TreeSet<>(ordering);

		final List<IndicatorSignal> firstIndicatorSignals = signals.get(indicators[0]);

		for (final IndicatorSignal firstIndicatorSignal : firstIndicatorSignals) {
			final LocalDate date = firstIndicatorSignal.getDate();

			// Discover how many of the indicator signals also match on that date
			int matches = 1;
			for (int i = 1; i < indicators.length; i++) {
				if (hasSignalOnSameDay(date, signals.get(indicators[i]))) {
					matches++;
				} else {
					// We need a match across all indicators, missed one :. don't continue
					break;
				}
			}

			// Buy when all have a signal on the same date
			if (matches == indicators.length) {
				passedSignals.add(new BuySignal(date));
			}
		}

		return passedSignals;
	}

	private boolean hasSignalOnSameDay( final LocalDate date, final List<IndicatorSignal> signals ) {

		for (final IndicatorSignal signal : signals) {
			if (date.equals(signal.getDate())) {
				return true;
			}
		}

		return false;
	}

	private void validateInput( final Map<IndicatorSignalType, List<IndicatorSignal>> signals ) {

		for (final IndicatorSignalType indicator : indicators) {
			if (signals.get(indicator) == null) {
				throw new IllegalArgumentException(String.format("Expecting a non-null %s list", indicator));
			}
		}
	}
}