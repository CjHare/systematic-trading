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
package com.systematic.trading.signals.indicator.rsi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLines;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Given RSI line points calculates when the following bullish events occurred:
 * <ul>
 * <li>Oversold; RSI moved from being below or on the over sold line to above
 * 					signaling a change the direction of momentum.</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexBullishSignalCalculator implements SignalCalculator<RelativeStrengthIndexLines> {

	/** Threshold for when the RSI is considered as over sold.*/
	private final BigDecimal oversold;

	public RelativeStrengthIndexBullishSignalCalculator( final BigDecimal oversold ) {
		this.oversold = oversold;
	}

	@Override
	public SignalType getType() {
		return SignalType.BULLISH;
	}

	@Override
	public List<DatedSignal> calculateSignals( final RelativeStrengthIndexLines rsiLine,
	        final Predicate<LocalDate> signalRange ) {

		final List<DatedSignal> signals = new ArrayList<>();
		Map.Entry<LocalDate, BigDecimal> yesterday = null;

		for (Map.Entry<LocalDate, BigDecimal> today : rsiLine.getRsi().entrySet()) {

			if (yesterday != null && signalRange.test(today.getKey())
			        && hasMomentumDirectionChanged(yesterday.getValue(), today.getValue())) {
				signals.add(new DatedSignal(today.getKey(), getType()));
			}

			yesterday = today;
		}

		return signals;
	}

	/**
	 * Has the RSI moved from below or on the over sold line to above it?
	 */
	private boolean hasMomentumDirectionChanged( final BigDecimal yesterday, final BigDecimal today ) {
		return today.compareTo(oversold) > 0 && yesterday.compareTo(oversold) <= 0;
	}
}