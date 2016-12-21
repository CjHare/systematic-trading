/**
 * Copyright (c) 2015-2017-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.signals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.TradingDayPricesDateOrder;
import com.systematic.trading.signals.model.event.IndicatorSignalEvent;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;
import com.systematic.trading.signals.model.filter.SignalFilter;

public class AnalysisLongBuySignals implements AnalysisBuySignals {

	/** When there are no signals generators, no input is needed.*/
	private static final int NO_DAYS_REQUIRED = 0;

	/** Used for converting from base zero to base one counting systems. */
	private static final int CONVERT_BASE_ZERO_TO_BASE_ONE = 1;

	/** Default ordering of signals. */
	private static final BuySignalDateComparator BUY_SIGNAL_ORDER_BY_DATE = new BuySignalDateComparator();

	/** Default ordering of prices. */
	private static final TradingDayPricesDateOrder TRADING_DAY_ORDER_BY_DATE = new TradingDayPricesDateOrder();

	/** Most number of trading day data used by the signal generators. */
	private final int requiredNumberOfTradingDays;

	/** Listeners interested in signal analysis events. */
	private final List<SignalAnalysisListener> listeners = new ArrayList<>();

	private final List<SignalFilter> filters;
	private final List<IndicatorSignalGenerator> generators;

	public AnalysisLongBuySignals(final List<IndicatorSignalGenerator> generators, final List<SignalFilter> filters) {
		validateInput(generators, filters);

		this.generators = generators;
		this.filters = filters;
		this.requiredNumberOfTradingDays = getRequiredNumberOfTradingDays(generators);
	}

	private void validateInput( final List<IndicatorSignalGenerator> generators, final List<SignalFilter> filters ) {
		if (generators == null) {
			throw new IllegalArgumentException("Expecting a non-null list of generators");
		}
		if (filters == null) {
			throw new IllegalArgumentException("Expecting a non-null list of filters");
		}
	}

	private int getRequiredNumberOfTradingDays( final List<IndicatorSignalGenerator> requiredGenerators ) {

		final List<Integer> requiredTradingDays = new ArrayList<>();
		for (final IndicatorSignalGenerator generator : requiredGenerators) {
			requiredTradingDays.add(generator.getRequiredNumberOfTradingDays());
		}

		return requiredTradingDays.isEmpty() ? NO_DAYS_REQUIRED
		        : Collections.max(requiredTradingDays) + CONVERT_BASE_ZERO_TO_BASE_ONE;
	}

	@Override
	public List<BuySignal> analyse( final TradingDayPrices[] data ) {

		// Correct the ordering from earliest to latest
		Arrays.sort(data, TRADING_DAY_ORDER_BY_DATE);

		// Generate the indicator signals
		final Map<IndicatorSignalType, List<IndicatorSignal>> indicatorSignals = getSignals(data);

		final LocalDate latestTradingDate = data[data.length - 1].getDate();
		final List<BuySignal> signals = new ArrayList<>();

		// Apply the rule filters
		for (final SignalFilter filter : filters) {
			signals.addAll(filter.apply(indicatorSignals, BUY_SIGNAL_ORDER_BY_DATE, latestTradingDate));
		}

		return signals;
	}

	private Map<IndicatorSignalType, List<IndicatorSignal>> getSignals( final TradingDayPrices[] data ) {

		final Map<IndicatorSignalType, List<IndicatorSignal>> indicatorSignals = new EnumMap<>(
		        IndicatorSignalType.class);

		for (final IndicatorSignalGenerator generator : generators) {
			final List<IndicatorSignal> signals = calculateSignals(data, generator);
			final IndicatorSignalType type = generator.getSignalType();
			indicatorSignals.put(type, signals);
		}

		return indicatorSignals;
	}

	private List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data,
	        final IndicatorSignalGenerator generator ) {

		final List<IndicatorSignal> signals;

		if (generator.getRequiredNumberOfTradingDays() < data.length) {
			signals = generator.calculateSignals(data);
			notifyIndicatorEvent(signals);
			return signals;
		}

		// Only here on error :. no signals
		return new ArrayList<>();
	}

	@Override
	public int getMaximumNumberOfTradingDaysRequired() {
		return requiredNumberOfTradingDays;
	}

	@Override
	public void addListener( final SignalAnalysisListener listener ) {
		listeners.add(listener);
	}

	private void notifyIndicatorEvent( final List<IndicatorSignal> signals ) {

		// Create the event only when there are listeners
		if (!listeners.isEmpty()) {
			for (final IndicatorSignal signal : signals) {
				final SignalAnalysisEvent event = new IndicatorSignalEvent(signal);
				for (final SignalAnalysisListener listener : listeners) {
					listener.event(event);
				}
			}
		}
	}

	@Override
	public List<SignalFilter> getFilters() {
		return filters;
	}
}