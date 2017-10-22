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
package com.systematic.trading.signals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signal.event.SignalAnalysisEvent;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signals.indicator.IndicatorSignals;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.event.IndicatorSignalEvent;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

public class AnalysisLongBuySignals implements AnalysisBuySignals {

	/** Used for converting from base zero to base one counting systems. */
	private static final int CONVERT_BASE_ZERO_TO_BASE_ONE = 1;

	//TODO the order for a sorted set or map of TradingDayPrices
	/** Default ordering of prices. */
	//private static final TradingDayPricesDateOrder TRADING_DAY_ORDER_BY_DATE = new TradingDayPricesDateOrder();

	/** Most number of trading day data used by the signal generators. */
	private final int requiredNumberOfTradingDays;

	/** Listeners interested in signal analysis events. */
	private final List<SignalAnalysisListener> listeners = new ArrayList<>();

	private final SignalFilter[] filters;
	private final IndicatorSignals[] generators;

	public AnalysisLongBuySignals( final IndicatorSignals[] generators, final SignalFilter[] filters ) {
		validateInput(generators, filters);
		this.generators = generators;
		this.filters = filters;
		this.requiredNumberOfTradingDays = getRequiredNumberOfTradingDays(generators);

		if (requiredNumberOfTradingDays < 1) {
			throw new IllegalArgumentException(String.format(
			        "Expecting at least one trading day required from generators %s", Arrays.toString(generators)));
		}
	}

	//TODO validator
	private void validateInput( final IndicatorSignals[] generators, final SignalFilter[] filters ) {
		if (generators == null) {
			throw new IllegalArgumentException("Expecting a non-null list of generators");
		}
		if (generators.length == 0) {
			throw new IllegalArgumentException("Expecting a non-empty list of generators");
		}
		if (filters == null) {
			throw new IllegalArgumentException("Expecting a non-null list of filters");
		}
		if (filters.length == 0) {
			throw new IllegalArgumentException("Expecting a non-empty list of filters");
		}
	}

	private int getRequiredNumberOfTradingDays( final IndicatorSignals[] requiredGenerators ) {

		final List<Integer> requiredTradingDays = new ArrayList<>();
		for (final IndicatorSignals generator : requiredGenerators) {
			requiredTradingDays.add(generator.getRequiredNumberOfTradingDays());
		}

		return Collections.max(requiredTradingDays) + CONVERT_BASE_ZERO_TO_BASE_ONE;
	}

	//TODO the given data should already be sorted, change to a sorted map or set, keyed by LocalDate
	//TODO return type should be a sorted set, earliest to latest
	@Override
	public List<BuySignal> analyse( final TradingDayPrices[] data ) {

		// Generate the indicator signals
		final Map<IndicatorSignalId, List<IndicatorSignal>> indicatorSignals = calculateSignals(data);
		final LocalDate latestTradingDate = getLatestTradingDate(data);

		return filterSignals(indicatorSignals, latestTradingDate);
	}

	private List<BuySignal> filterSignals( final Map<IndicatorSignalId, List<IndicatorSignal>> indicatorSignals,
	        final LocalDate latestTradingDate ) {
		final List<BuySignal> signals = new ArrayList<>();

		// Apply the rule filters
		for (final SignalFilter filter : filters) {
			signals.addAll(filter.apply(indicatorSignals, latestTradingDate));
		}

		return signals;
	}

	private LocalDate getLatestTradingDate( final TradingDayPrices[] data ) {
		return data[data.length - 1].getDate();
	}

	private Map<IndicatorSignalId, List<IndicatorSignal>> calculateSignals( final TradingDayPrices[] data ) {
		final Map<IndicatorSignalId, List<IndicatorSignal>> indicatorSignals = new HashMap<>();

		for (final IndicatorSignals generator : generators) {
			final List<IndicatorSignal> signals = calculateSignals(data, generator);
			final IndicatorSignalId type = generator.getSignalId();
			indicatorSignals.put(type, signals);
		}

		return indicatorSignals;
	}

	private List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data, final IndicatorSignals generator ) {

		final List<IndicatorSignal> signals;

		if (generator.getRequiredNumberOfTradingDays() < data.length) {
			signals = generator.calculate(data);
			notifyIndicatorEvent(signals);
			return signals;
		}

		//TODO reqwrite to use optional, need to update the filters to accept no entry
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
	public SignalFilter[] getFilters() {
		return filters;
	}
}