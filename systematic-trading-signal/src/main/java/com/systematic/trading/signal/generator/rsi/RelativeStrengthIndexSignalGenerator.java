/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.signal.generator.rsi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.signal.generator.SignalGenerator;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Generic RSI signal calculation.
 * 
 * @author CJ Hare
 */
public abstract class RelativeStrengthIndexSignalGenerator implements SignalGenerator<RelativeStrengthIndexLine> {

	public List<DatedSignal> generate(
	        final RelativeStrengthIndexLine rsiLine,
	        final Predicate<LocalDate> signalRange ) {

		final List<DatedSignal> signals = new ArrayList<>();
		Map.Entry<LocalDate, BigDecimal> yesterday = null;

		for (Map.Entry<LocalDate, BigDecimal> today : rsiLine.rsi().entrySet()) {

			if (yesterday != null && signalRange.test(today.getKey())
			        && hasMomentumDirectionChanged(yesterday.getValue(), today.getValue())) {
				signals.add(new DatedSignal(today.getKey(), type()));
			}

			yesterday = today;
		}

		return signals;
	}

	protected abstract boolean hasMomentumDirectionChanged( final BigDecimal yesterday, final BigDecimal today );
}
