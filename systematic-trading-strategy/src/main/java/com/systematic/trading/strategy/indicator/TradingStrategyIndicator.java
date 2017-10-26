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
package com.systematic.trading.strategy.indicator;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.SignalCalculator;
import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signal.event.SignalAnalysisEvent;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.generator.SignalGenerator;
import com.systematic.trading.signals.model.DatedSignal;
import com.systematic.trading.signals.model.event.IndicatorSignalEvent;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;
import com.systematic.trading.strategy.definition.Indicator;

/**
 * Trading strategy indicator that generates signals.
 * 
 * @author CJ Hare
 */
public class TradingStrategyIndicator<T, U extends SignalCalculator<T>> implements Indicator {

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	/** Generators that will be used to generate signals. */
	private final SignalGenerator<T> generator;

	/** Converts price data into indicator signals. */
	private final U calculator;

	/** Identifier for the configuration of signal calculated. */
	private final IndicatorId id;

	/** Listners interested in when indicator signals are generated. */
	private final SignalAnalysisListener[] listeners;

	public TradingStrategyIndicator( final IndicatorId id, final U indicator, final SignalGenerator<T> generator,
	        final SignalRangeFilter signalRangeFilter, final SignalAnalysisListener... listeners ) {
		this.signalRangeFilter = signalRangeFilter;
		this.calculator = indicator;
		this.generator = generator;
		this.listeners = listeners;
		this.id = id;
	}

	@Override
	public List<DatedSignal> analyse( TradingDayPrices[] data ) {
		final List<DatedSignal> signals = generator.generate(calculator.calculate(data), signalDateRange(data));
		notifyListners(signals);
		return signals;
	}

	private Predicate<LocalDate> signalDateRange( final TradingDayPrices[] data ) {
		return candidateDate -> dateRangeFilter.isWithinSignalRange(signalRangeFilter.getEarliestSignalDate(data),
		        signalRangeFilter.getLatestSignalDate(data), candidateDate);
	}

	@Override
	public int getNumberOfTradingDaysRequired() {
		return calculator.getMinimumNumberOfPrices();
	}

	private void notifyListners( final List<DatedSignal> signals ) {

		// Create the event only when there are listeners
		if (listeners.length != 0) {
			for (final DatedSignal signal : signals) {
				final SignalAnalysisEvent event = new IndicatorSignalEvent(
				        new IndicatorSignal(signal.getDate(), id, generator.getType()));
				for (final SignalAnalysisListener listener : listeners) {
					listener.event(event);
				}
			}
		}
	}
}