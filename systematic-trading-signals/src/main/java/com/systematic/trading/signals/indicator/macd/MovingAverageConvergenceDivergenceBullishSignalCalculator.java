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
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * MACD bullish signal calculator, takes the MACD lines calculated from the price data and calculates bullish signals from it.
 * 
 * @author CJ Hare
 */
public class MovingAverageConvergenceDivergenceBullishSignalCalculator
        implements SignalCalculator<MovingAverageConvergenceDivergenceLines> {

	@Override
	public SignalType getType() {
		return SignalType.BULLISH;
	}

	@Override
	public List<DatedSignal> calculateSignals( final MovingAverageConvergenceDivergenceLines lines,
	        Predicate<LocalDate> signalRange ) {

		final SortedMap<LocalDate, BigDecimal> macd = lines.getMacd();
		final SortedMap<LocalDate, BigDecimal> signaLine = lines.getSignalLine();
		final List<DatedSignal> signals = new ArrayList<>();

		// SortedMaps ordered by date, need to skip the first entry, using yesterday as a flag
		LocalDate yesterday = null;

		for (final Map.Entry<LocalDate, BigDecimal> macdEntry : macd.entrySet()) {
			final LocalDate today = macdEntry.getKey();

			if (yesterday != null && signalRange.test(today) && isBullishSignal(macd.get(today), macd.get(yesterday),
			        signaLine.get(today), signaLine.get(yesterday))) {

				signals.add(new DatedSignal(today, getType()));
			}

			yesterday = today;
		}

		return signals;
	}

	/**
	 *  Buy (Bullish) signal is from a cross over of the signal line, or crossing over the origin
	 */
	private boolean isBullishSignal( final BigDecimal todayMacd, final BigDecimal yesterdayMacd,
	        final BigDecimal todaySignalLine, final BigDecimal yesterdaySignalLine ) {
		return crossingSignalLine(yesterdayMacd, todayMacd, todaySignalLine, yesterdaySignalLine)
		        || crossingOrigin(yesterdayMacd, todayMacd);
	}

	/* 
	 * Between yesterday and today: - MACD need to be moving upwards - today's MACD needs to be
	 * above today's signal line - yesterday's MACD needs to be below yesterday's signal line
	 */
	private boolean crossingSignalLine( final BigDecimal yesterdayMacd, final BigDecimal todayMacd,
	        final BigDecimal yesterdaySignalLine, final BigDecimal todaySignalLine ) {
		return isHigher(todayMacd, yesterdayMacd) && isEvenOrHigher(todayMacd, todaySignalLine)
		        && isHigher(yesterdaySignalLine, yesterdayMacd);
	}

	private boolean isHigher( final BigDecimal today, final BigDecimal yesterday ) {
		return today.compareTo(yesterday) > 0;
	}

	private boolean isEvenOrHigher( final BigDecimal today, final BigDecimal yesterday ) {
		return today.compareTo(yesterday) >= 0;
	}

	private boolean isEvenOrLower( final BigDecimal today, final BigDecimal yesterday ) {
		return today.compareTo(yesterday) <= 0;
	}

	private boolean crossingOrigin( final BigDecimal yesterdayMacd, final BigDecimal todayMacd ) {
		return isHigher(todayMacd, yesterdayMacd) && isEvenOrHigher(todayMacd, BigDecimal.ZERO)
		        && isEvenOrLower(yesterdayMacd, BigDecimal.ZERO);
	}
}