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
package com.systematic.trading.signals.indicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageCalculator;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;

/**
 * Interested in the Simple Moving Average (SMA) gradient, whether it is negative (downward),flat
 * (no change) or positive (upward).
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignals implements IndicatorSignalGenerator {

	public enum GradientType {
		NEGATIVE,
		FLAT,
		POSITIVE
	}

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	/** On which type of gradient does a signal get generated. */
	private final GradientType signalGenerated;

	/** Number of days to average the value on. */
	private final int lookback;

	/** The number of days the SMA gradient covers. */
	private final int daysOfGradient;

	/** Responsible for calculating the simple moving average. */
	private final SimpleMovingAverage movingAverage;

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	public SimpleMovingAverageGradientSignals( final int lookback, final int daysOfGradient,
	        final GradientType signalGenerated, final SignalRangeFilter filter, final MathContext mathContext ) {

		this(lookback, daysOfGradient, signalGenerated, filter, mathContext, new SimpleMovingAverageCalculator(lookback,
		        daysOfGradient, new IllegalArgumentThrowingValidator(), mathContext));
	}

	private SimpleMovingAverageGradientSignals( final int lookback, final int daysOfGradient,
	        final GradientType signalGenerated, final SignalRangeFilter filter, final MathContext mathContext,
	        final SimpleMovingAverageCalculator movingAverage ) {
		this.signalGenerated = signalGenerated;
		this.daysOfGradient = daysOfGradient;
		this.movingAverage = movingAverage;
		this.mathContext = mathContext;
		this.lookback = lookback;
		this.signalRangeFilter = filter;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO validate the number of data items meets the minimum

		final Predicate<LocalDate> signalRange = candidate -> dateRangeFilter.isWithinSignalRange(
		        signalRangeFilter.getEarliestSignalDate(data), signalRangeFilter.getLatestSignalDate(data), candidate);

		final List<BigDecimal> sma = movingAverage.sma(data);

		// Only look at the gradient if there's more than one sma result
		if (!sma.isEmpty()) {
			return analysisGradient(data, sma, signalRange);
		}

		return new ArrayList<>();
	}

	private List<IndicatorSignal> analysisGradient( final TradingDayPrices[] data, final List<BigDecimal> sma,
	        final Predicate<LocalDate> signalRange ) {
		final List<IndicatorSignal> signals = new ArrayList<>();

		// We're only using the right most values of the data
		final int offset = data.length - sma.size();

		// Start with the first value, bump the index
		BigDecimal previous = sma.get(0);
		for (int index = 1; index < sma.size(); index++) {

			final LocalDate today = data[index + offset].getDate();

			if (signalRange.test(today)) {

				//TODO generate the down signals too
				switch (signalGenerated) {
					case POSITIVE:
						if (isPositiveGardient(previous, sma.get(index))) {
							signals.add(new IndicatorSignal(today, IndicatorSignalType.SMA, SignalType.BULLISH));
						}
					break;
					case FLAT:
						if (isFlatGardient(previous, sma.get(index))) {
							signals.add(new IndicatorSignal(today, IndicatorSignalType.SMA, SignalType.BULLISH));
						}
					break;
					case NEGATIVE:
						if (isNegativeGardient(previous, sma.get(index))) {
							signals.add(new IndicatorSignal(today, IndicatorSignalType.SMA, SignalType.BULLISH));
						}
					break;
					default:
						throw new IllegalArgumentException(String.format("%s enum is unexpected", signalGenerated));
				}
			}

			previous = sma.get(index);
		}

		return signals;
	}

	private boolean isPositiveGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract(previous, mathContext).compareTo(BigDecimal.ZERO) > 0;
	}

	private boolean isNegativeGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract(previous, mathContext).compareTo(BigDecimal.ZERO) < 0;
	}

	private boolean isFlatGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract(previous, mathContext).compareTo(BigDecimal.ZERO) == 0;
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