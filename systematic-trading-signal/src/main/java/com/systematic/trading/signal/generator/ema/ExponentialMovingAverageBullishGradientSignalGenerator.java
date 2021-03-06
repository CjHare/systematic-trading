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
package com.systematic.trading.signal.generator.ema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Predicate;

import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageLine;
import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.signal.generator.SignalGenerator;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Bullish signal calculation based on the gradient of a SMA.
 * <p/>
 * Gradient of the SMA is evaluated, when it's positive it's considered bullish, otherwise it's not.
 * 
 * @author CJ Hare
 */
public class ExponentialMovingAverageBullishGradientSignalGenerator
        implements SignalGenerator<ExponentialMovingAverageLine> {

	@Override
	public SignalType type() {

		return SignalType.BULLISH;
	}

	@Override
	public List<DatedSignal> generate(
	        final ExponentialMovingAverageLine indicatorOutput,
	        final Predicate<LocalDate> signalRange ) {

		final SortedMap<LocalDate, BigDecimal> ema = indicatorOutput.ema();
		final List<DatedSignal> signals = new ArrayList<>();
		Map.Entry<LocalDate, BigDecimal> previousEntry = null;

		for (final Map.Entry<LocalDate, BigDecimal> entry : ema.entrySet()) {
			final LocalDate today = entry.getKey();

			if (previousEntry != null && signalRange.test(today) && isPositiveGradient(entry, previousEntry)) {
				signals.add(new DatedSignal(today, type()));
			}

			previousEntry = entry;
		}

		return signals;
	}

	private boolean isPositiveGradient(
	        final Map.Entry<LocalDate, BigDecimal> entry,
	        final Map.Entry<LocalDate, BigDecimal> previousEtnry ) {

		return entry.getValue().subtract(previousEtnry.getValue()).compareTo(BigDecimal.ZERO) > 0;
	}
}
