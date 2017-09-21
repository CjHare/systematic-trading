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
import java.util.function.Predicate;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexDataPoint;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Given RSI line points calculates when the following bearish events occurred:
 * <ul>
 * <li>Overbrought; RSI moved from being on or above the over brought line to below, 
 * 					signaling a change the direction of momentum.</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexBearishSignalCalculator
        implements SignalCalculator<List<RelativeStrengthIndexDataPoint>> {

	/** Threshold for when the RSI is considered as over brought.*/
	private final BigDecimal overbrought;

	public RelativeStrengthIndexBearishSignalCalculator( final BigDecimal overbrought ) {
		this.overbrought = overbrought;
	}

	@Override
	public SignalType getType() {
		return SignalType.BEARISH;
	}

	@Override
	public List<DatedSignal> calculateSignals( List<RelativeStrengthIndexDataPoint> rsiLine,
	        Predicate<LocalDate> signalRange ) {

		final List<DatedSignal> signals = new ArrayList<>();

		//TODO getting the first value? how about when there aren't any?
		RelativeStrengthIndexDataPoint yesterday = null;

		for (final RelativeStrengthIndexDataPoint today : rsiLine) {
			if (yesterday != null && signalRange.test(today.getDate())
			        && hasMomentumDirectionChanged(yesterday, today)) {
				signals.add(new DatedSignal(today.getDate(), getType()));
			}

			yesterday = today;
		}

		return signals;
	}

	/**
	 * Has the RSI moved from above or on the over sold line to below it?
	 */
	private boolean hasMomentumDirectionChanged( final RelativeStrengthIndexDataPoint yesterday,
	        final RelativeStrengthIndexDataPoint today ) {
		return today.getValue().compareTo(overbrought) < 0 && yesterday.getValue().compareTo(overbrought) >= 0;
	}

}