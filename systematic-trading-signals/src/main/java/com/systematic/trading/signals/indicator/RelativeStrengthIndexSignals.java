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
package com.systematic.trading.signals.indicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Takes the output from RSI and identifies any buy signals using a double cross over of RSI
 * signals.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexSignals implements IndicatorSignalGenerator {

	/** To provide accuracy with the smoothing more then the minimum data points are needed. */
	private static final int MINIMUM_PRICES_FOR_ACCURACY = 50;

	/** Minimum days of RSI signals that it makes sense to analyse. */
	private static final int DAYS_OF_SIGNALS = 2;

	private final RelativeStrengthIndex rsi;

	private final int requiredNumberOfTradingDays;

	private final BigDecimal oversold;

	private final BigDecimal overbought;

	// TODO enums for configuration

	// TODO only one constructor
	public RelativeStrengthIndexSignals(final int lookback, final int oversold, final int overbought,
	        final MathContext mathContext) {
		this.oversold = BigDecimal.valueOf(oversold);
		this.overbought = BigDecimal.valueOf(overbought);
		this.requiredNumberOfTradingDays = lookback + DAYS_OF_SIGNALS + MINIMUM_PRICES_FOR_ACCURACY;

		this.rsi = new RelativeStrengthIndexCalculator(lookback, DAYS_OF_SIGNALS,
		        new IllegalArgumentThrowingValidator(), mathContext);
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		// Calculate the RSI signals
		// TODO convert return type to value with date
		final List<BigDecimal> tenDayRsi = rsi.rsi(data);

		/* RSI triggers a buy signal when crossing the over brought level (e.g. 30) */
		return buySignals(tenDayRsi, data);
	}

	protected List<IndicatorSignal> buySignals( final List<BigDecimal> rsi, final TradingDayPrices[] data ) {

		// TODO pass the store array in
		final List<IndicatorSignal> buySignals = new ArrayList<IndicatorSignal>();

		final int offset = data.length - rsi.size();

		for (int index = 0; index < rsi.size(); index++) {
			if (isOversold(rsi.get(index))) {
				// rsi list maps to the right most data entries
				buySignals.add(new IndicatorSignal(data[offset + index].getDate(), IndicatorSignalType.RSI));
			}
		}

		return buySignals;
	}

	/**
	 * Security is considered over sold when the RSI meet or falls below the threshold F
	 */
	private boolean isOversold( final BigDecimal rsi ) {
		return oversold.compareTo(rsi) >= 0;
	}

	protected List<IndicatorSignal> intersection( final List<IndicatorSignal> a, final List<IndicatorSignal> b ) {
		final List<IndicatorSignal> intersection = new ArrayList<IndicatorSignal>();
		final List<IndicatorSignal> shorter = a.size() < b.size() ? a : b;
		final List<IndicatorSignal> larger = a.size() >= b.size() ? a : b;

		for (final IndicatorSignal signal : shorter) {
			// Match on the dates
			final LocalDate aDate = signal.getDate();
			if (contains(aDate, larger)) {
				intersection.add(signal);
			}
		}

		return intersection;
	}

	private boolean contains( final LocalDate date, final List<IndicatorSignal> signals ) {
		for (final IndicatorSignal signal : signals) {
			if (date.equals(signal.getDate())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return requiredNumberOfTradingDays;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.RSI;
	}
}
