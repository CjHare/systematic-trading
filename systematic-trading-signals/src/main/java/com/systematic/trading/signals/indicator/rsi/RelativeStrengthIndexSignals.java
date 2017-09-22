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
package com.systematic.trading.signals.indicator.rsi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Given time series price date, creates RSI values and any appropriate signals.
 * <p/>
 * The RSI is most reliable in a ranging market and may give misleading signals in a trending market.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexSignals implements IndicatorSignalGenerator {

	/** The least number of data points that enables RSI signal generation. */
	private static final int MINIMUM_DAYS_OF_RSI_VALUES = 2;

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	/** Calculates the RSI values from time series data. */
	private final RelativeStrengthIndex rsi;

	/** Required number of data points required for RSI calculation. */
	private final int minimumNumberOfPrices;

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	/** Calculators that each parse the signal output to potentially create signals.  */
	private final List<SignalCalculator<RelativeStrengthIndexLine>> signalCalculators;

	/**
	 * @param lookback the number of data points to use in calculations.
	 * @param daysOfRsiValues the number of RSI values desired.
	 */
	public RelativeStrengthIndexSignals( final int lookback, final RelativeStrengthIndexCalculator rsi,
	        final List<SignalCalculator<RelativeStrengthIndexLine>> signalCalculators,
	        final SignalRangeFilter filter ) {
		this.minimumNumberOfPrices = lookback + MINIMUM_DAYS_OF_RSI_VALUES;
		this.signalRangeFilter = filter;
		this.signalCalculators = signalCalculators;
		this.rsi = rsi;

		//TODO validate there's at least one signal calculator 
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return minimumNumberOfPrices;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO validate minimum nummber of points given

		final Predicate<LocalDate> signalRange = candidate -> dateRangeFilter.isWithinSignalRange(
		        signalRangeFilter.getEarliestSignalDate(data), signalRangeFilter.getLatestSignalDate(data), candidate);

		final RelativeStrengthIndexLine rsiLine = rsi.rsi(data);

		final List<IndicatorSignal> indicatorSignals = new ArrayList<>();

		for (final SignalCalculator<RelativeStrengthIndexLine> calculator : signalCalculators) {
			final List<DatedSignal> signals = calculator.calculateSignals(rsiLine, signalRange);

			for (final DatedSignal signal : signals) {
				indicatorSignals.add(new IndicatorSignal(signal.getDate(), getSignalType(), calculator.getType()));
			}
		}

		return indicatorSignals;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.RSI;
	}
}