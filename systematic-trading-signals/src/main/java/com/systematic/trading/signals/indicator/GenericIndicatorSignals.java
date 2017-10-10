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
package com.systematic.trading.signals.indicator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Indicator;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Generic calculation of signal functionality.
 * 
 * @author CJ Hare
 */
public class GenericIndicatorSignals<T, U extends Indicator<T>> implements IndicatorSignals {

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	/** Calculators that will be used to generate signals. */
	private final List<SignalCalculator<T>> signalCalculators;

	/** Converts price data into indicator signals. */
	private final U indicator;

	/** Identifier for the configuration of signal calculated. */
	private final IndicatorSignalId id;

	/** Minimum number of trading days required for MACD signal generation. */
	private final int requiredNumberOfTradingDays;

	public GenericIndicatorSignals( final IndicatorSignalId id, final U indicator, final int requiredNumberOfTradingDays,
	        final List<SignalCalculator<T>> signalCalculators, final SignalRangeFilter signalRangeFilter ) {
		this.signalCalculators = signalCalculators;
		this.signalRangeFilter = signalRangeFilter;
		this.requiredNumberOfTradingDays = requiredNumberOfTradingDays;
		this.indicator = indicator;
		this.id = id;

		//TODO validate there's at least one signal calculator 

	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return requiredNumberOfTradingDays;
	}

	@Override
	public IndicatorSignalId getSignalType() {
		return id;
	}

	@Override
	public List<IndicatorSignal> calculate( final TradingDayPrices[] data ) {

		//TODO validate the number of data items meets the minimum

		final Predicate<LocalDate> signalRange = candidate -> dateRangeFilter.isWithinSignalRange(
		        signalRangeFilter.getEarliestSignalDate(data), signalRangeFilter.getLatestSignalDate(data), candidate);

		final T indicatorOutput = indicator.calculate(data);

		final List<IndicatorSignal> indicatorSignals = new ArrayList<>();

		for (final SignalCalculator<T> calculator : signalCalculators) {
			final List<DatedSignal> signals = calculator.calculateSignals(indicatorOutput, signalRange);

			for (final DatedSignal signal : signals) {
				indicatorSignals.add(new IndicatorSignal(signal.getDate(), getSignalType(), calculator.getType()));
			}
		}

		return indicatorSignals;
	}
}