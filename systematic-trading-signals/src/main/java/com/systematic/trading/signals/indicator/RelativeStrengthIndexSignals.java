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
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.signals.model.IndicatorDirectionType;
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

	private List<IndicatorSignal> buySignals( final List<BigDecimal> rsi, final TradingDayPrices[] data ) {

		// TODO pass the store array in
		final List<IndicatorSignal> buySignals = new ArrayList<>();

		final int offset = data.length - rsi.size();

		// Need at least two values to test transition between barriers
		for (int index = 1; index < rsi.size(); index++) {
			if (hasTransitionedFromOversold(rsi, index)) {
				buySignals.add(new IndicatorSignal(data[offset + index].getDate(), IndicatorSignalType.RSI,
				        IndicatorDirectionType.UP));
			}
		}

		return buySignals;
	}

	private boolean hasTransitionedFromOversold( final List<BigDecimal> rsi, final int index ) {
		final BigDecimal rsiYesterday = rsi.get(index - 1);
		final BigDecimal rsiToday = rsi.get(index);

		return isOversold(rsiYesterday) && isNotOversold(rsiToday);
	}

	/**
	 * Security is considered over sold when the RSI meet or falls below the threshold.
	 */
	private boolean isOversold( final BigDecimal rsi ) {
		return oversold.compareTo(rsi) >= 0;
	}

	/**
	 * Security is not considered over sold when the RSI meet or falls below the threshold.
	 */
	private boolean isNotOversold( final BigDecimal rsi ) {
		return oversold.compareTo(rsi) < 0;
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
