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
package com.systematic.trading.signals.indicator.sma;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * The Simple Moving Average (SMA) gradient, whether it is negative (downward),flat
 * (no change) or positive (upward).
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignals implements IndicatorSignalGenerator {

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	/** Number of days to average the value on. */
	private final int lookback;

	/** The number of days the SMA gradient covers. */
	private final int daysOfGradient;

	/** Responsible for calculating the simple moving average. */
	private final SimpleMovingAverage sma;

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	/** Calculators that will be used to generate signals. */
	private final List<SignalCalculator<SimpleMovingAverageLine>> signalCalculators;

	public SimpleMovingAverageGradientSignals( final int lookback, final int daysOfGradient,
	        final SimpleMovingAverage sma, final List<SignalCalculator<SimpleMovingAverageLine>> signalCalculators,
	        final SignalRangeFilter filter ) {
		this.signalCalculators = signalCalculators;
		this.daysOfGradient = daysOfGradient;
		this.sma = sma;
		this.lookback = lookback;
		this.signalRangeFilter = filter;

		//TODO validate there's at least one signal calculator 
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO validate the number of data items meets the minimum

		final Predicate<LocalDate> signalRange = candidate -> dateRangeFilter.isWithinSignalRange(
		        signalRangeFilter.getEarliestSignalDate(data), signalRangeFilter.getLatestSignalDate(data), candidate);

		final SimpleMovingAverageLine smaLine = sma.sma(data);

		final List<IndicatorSignal> indicatorSignals = new ArrayList<>();

		for (final SignalCalculator<SimpleMovingAverageLine> calculator : signalCalculators) {
			final List<DatedSignal> signals = calculator.calculateSignals(smaLine, signalRange);

			for (final DatedSignal signal : signals) {
				indicatorSignals.add(new IndicatorSignal(signal.getDate(), getSignalType(), calculator.getType()));
			}
		}

		return indicatorSignals;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return lookback + daysOfGradient;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.SMA;
	}
}