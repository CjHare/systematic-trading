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
import com.systematic.trading.maths.formula.rs.RelativeStrengthCalculator;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexDataPoint;
import com.systematic.trading.signal.IndicatorSignalType;
import com.systematic.trading.signals.filter.InclusiveDatelRangeFilter;
import com.systematic.trading.signals.filter.SignalRangeFilter;

public class RelativeStrengthIndexSignals implements IndicatorSignalGenerator {

	/** The least number of data points that enables RSI signal generation. */
	private static final int MINIMUM_DAYS_OF_RSI_VALUES = 2;

	/** Provides date range filtering. */
	private final InclusiveDatelRangeFilter dateRangeFilter = new InclusiveDatelRangeFilter();

	private final RelativeStrengthIndex rsi;

	/** Required number of data points required for RSI calculation. */
	private final int minimumNumberOfPrices;

	/** Threshold for when the RSI is considered as over sold.*/
	private final BigDecimal oversold;

	/** Threshold for when the RSI is considered as over brought.*/
	private final BigDecimal overbrought;

	/** Range of signal dates of interest. */
	private final SignalRangeFilter signalRangeFilter;

	/**
	 * @param lookback the number of data points to use in calculations.
	 * @param daysOfRsiValues the number of RSI values desired.
	 */
	public RelativeStrengthIndexSignals( final int lookback, final BigDecimal oversold, final BigDecimal overbrought,
	        final SignalRangeFilter filter, final MathContext mathContext ) {
		this.minimumNumberOfPrices = lookback + MINIMUM_DAYS_OF_RSI_VALUES;
		this.overbrought = overbrought;
		this.oversold = oversold;
		this.signalRangeFilter = filter;
		this.rsi = new RelativeStrengthIndexCalculator(
		        new RelativeStrengthCalculator(lookback, new IllegalArgumentThrowingValidator(), mathContext),
		        new IllegalArgumentThrowingValidator(), mathContext);
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return minimumNumberOfPrices;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO validate minimum nummber of points given

		final Predicate<LocalDate> signalRange = ( candidate ) -> dateRangeFilter.isWithinSignalRange(
		        signalRangeFilter.getEarliestSignalDate(data), signalRangeFilter.getLatestSignalDate(data), candidate);

		final List<RelativeStrengthIndexDataPoint> rsiData = rsi.rsi(data);

		return addSellSignals(rsiData, addBuySignals(rsiData, new ArrayList<>(), signalRange), signalRange);
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.RSI;
	}

	private List<IndicatorSignal> addBuySignals( final List<RelativeStrengthIndexDataPoint> rsiData,
	        final List<IndicatorSignal> signals, final Predicate<LocalDate> signalRange ) {

		//TODO apply a date filter on signals

		RelativeStrengthIndexDataPoint yesterday = rsiData.get(0);

		for (final RelativeStrengthIndexDataPoint today : rsiData) {
			if (signalRange.test(today.getDate()) && isOversold(yesterday, today)) {
				signals.add(new IndicatorSignal(today.getDate(), IndicatorSignalType.RSI, SignalType.BULLISH));
			}

			yesterday = today;
		}

		return signals;
	}

	private List<IndicatorSignal> addSellSignals( final List<RelativeStrengthIndexDataPoint> rsiData,
	        final List<IndicatorSignal> signals, final Predicate<LocalDate> signalRange ) {

		RelativeStrengthIndexDataPoint yesterday = rsiData.get(0);

		for (final RelativeStrengthIndexDataPoint today : rsiData) {
			if (signalRange.test(today.getDate()) && isOverbrought(yesterday, today)) {
				signals.add(new IndicatorSignal(today.getDate(), IndicatorSignalType.RSI, SignalType.BEARISH));
			}

			yesterday = today;
		}

		return signals;
	}

	private boolean isOversold( final RelativeStrengthIndexDataPoint yesterday,
	        final RelativeStrengthIndexDataPoint today ) {
		return yesterday.getValue().compareTo(oversold) <= 0 && today.getValue().compareTo(oversold) >= 0;
	}

	private boolean isOverbrought( final RelativeStrengthIndexDataPoint yesterday,
	        final RelativeStrengthIndexDataPoint today ) {
		return yesterday.getValue().compareTo(overbrought) >= 0 && today.getValue().compareTo(overbrought) <= 0;
	}
}