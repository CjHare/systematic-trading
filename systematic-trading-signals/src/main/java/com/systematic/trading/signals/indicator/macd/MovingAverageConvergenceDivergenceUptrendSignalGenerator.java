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
package com.systematic.trading.signals.indicator.macd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Predicate;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signals.indicator.SignalGenerator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * MACD up trend signal calculator, takes the MACD lines calculated from the price data, ignores the signal line, 
 * with an trend being identified as MACD line being above the origin.
 * 
 * @author CJ Hare
 */
public class MovingAverageConvergenceDivergenceUptrendSignalGenerator
        implements SignalGenerator<MovingAverageConvergenceDivergenceLines> {

	@Override
	public SignalType getType() {
		return SignalType.BULLISH;
	}

	@Override
	public List<DatedSignal> generate( final MovingAverageConvergenceDivergenceLines lines,
	        Predicate<LocalDate> signalRange ) {

		final SortedMap<LocalDate, BigDecimal> macd = lines.getMacd();
		final List<DatedSignal> signals = new ArrayList<>();

		for (final Map.Entry<LocalDate, BigDecimal> entry : macd.entrySet()) {
			final LocalDate today = entry.getKey();

			if (signalRange.test(today) && isAboveOrigin(entry.getValue())) {
				signals.add(new DatedSignal(today, getType()));
			}
		}

		return signals;
	}

	private boolean isAboveOrigin( final BigDecimal today ) {
		return today.compareTo(BigDecimal.ZERO) > 0;
	}
}