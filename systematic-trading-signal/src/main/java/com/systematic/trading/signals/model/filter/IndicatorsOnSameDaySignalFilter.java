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

import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * Signal generated when there is a RSI and MACD filter on the same day.
 * 
 * @author CJ Hare
 */
public class IndicatorsOnSameDaySignalFilter implements SignalFilter {

	/** Ordering of the generated signals. */
	private final Comparator<BuySignal> signalOrdering;

	/** The signals we will be looking for on each application. */
	private IndicatorId[] indicators;

	/**
	 * @param indicators all the indicators expected in an application, those all required on the
	 *            same date to pass the filtering.
	 */
	public IndicatorsOnSameDaySignalFilter( final Comparator<BuySignal> signalOrdering,
	        final IndicatorId... indicators ) {
		
		//TODO use validator
		validate(indicators, "Expecting at least one IndicatorSignalType");
		validate(signalOrdering, "Expecting a non-null singal comparator");

		// There'll be NullPointers unless we have an array of IndicatorSignalType
		if (indicators.length == 0) {
			throw new IllegalArgumentException("Expecting at least one IndicatorSignalType");
		}

		this.indicators = indicators;
		this.signalOrdering = signalOrdering;
	}

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorId, List<IndicatorSignal>> signals,
	        final LocalDate latestTradingDate ) {
		validateInput(signals);

		final SortedSet<BuySignal> passedSignals = new TreeSet<>(signalOrdering);

		// Loop through the set of signals from the first indicator
		final List<IndicatorSignal> firstIndicatorSignals = signals.get(indicators[0]);
		for (final IndicatorSignal firstIndicatorSignal : firstIndicatorSignals) {
			final LocalDate date = firstIndicatorSignal.getDate();

			if (hasEverySignalsOnSameDay(date, signals)) {
				passedSignals.add(new BuySignal(date));
			}
		}

		return passedSignals;
	}

	private boolean hasEverySignalsOnSameDay( final LocalDate date,
	        final Map<IndicatorId, List<IndicatorSignal>> signals ) {

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

		return matches == indicators.length;
	}

	private boolean hasSignalOnSameDay( final LocalDate date, final List<IndicatorSignal> signals ) {

		for (final IndicatorSignal signal : signals) {
			if (date.equals(signal.getDate())) {
				return true;
			}
		}

		return false;
	}

	//TODO move into a validator
	private void validateInput( final Map<IndicatorId, List<IndicatorSignal>> signals ) {

		for (final IndicatorId indicator : indicators) {
			if (signals.get(indicator) == null) {
				throw new IllegalArgumentException(String.format("Expecting a non-null %s list", indicator));
			}
		}
	}

	private void validate( final Object toValidate, final String message ) {
		if (toValidate == null) {
			throw new IllegalArgumentException(message);
		}
	}
}