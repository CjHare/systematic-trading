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
import java.util.stream.Collectors;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.model.SignalType;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class MovingAveragingConvergeDivergenceSignals implements IndicatorSignalGenerator {

	private final MovingAverageConvergenceDivergence macd;
	private final int requiredNumberOfTradingDays;

	/** Number of trading days before the latest (current) trading date to generate signals on. */
	private final int previousTradingDaySignalRange;

	public MovingAveragingConvergeDivergenceSignals( final int fastTimePeriods, final int slowTimePeriods,
	        final int signalTimePeriods, final int previousTradingDaySignalRange, final MathContext mathContext ) {

		final ExponentialMovingAverage fastEma = new ExponentialMovingAverageCalculator(fastTimePeriods,
		        new IllegalArgumentThrowingValidator(), mathContext);
		final ExponentialMovingAverage slowEma = new ExponentialMovingAverageCalculator(slowTimePeriods,
		        new IllegalArgumentThrowingValidator(), mathContext);
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator(signalTimePeriods,
		        new IllegalArgumentThrowingValidator(), mathContext);

		this.macd = new MovingAverageConvergenceDivergenceCalculator(fastEma, slowEma, signalEma,
		        new IllegalArgumentThrowingValidator());

		this.requiredNumberOfTradingDays = fastEma.getMinimumNumberOfPrices() + slowEma.getMinimumNumberOfPrices()
		        + signalEma.getMinimumNumberOfPrices();
		this.previousTradingDaySignalRange = previousTradingDaySignalRange;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO validate the number of data items meets the minimum

		final LocalDate earliestSignalDate = data[data.length - 1 - previousTradingDaySignalRange].getDate();
		final LocalDate latestSignalDate = data[data.length - 1].getDate();

		//TODO generate the down signals too
		final List<DatedSignal> signals = calculateBullishSignals(macd.macd(data), earliestSignalDate,
		        latestSignalDate);

		return signals.stream().map(signal -> new IndicatorSignal(signal.getDate(), IndicatorSignalType.MACD,
		        IndicatorDirectionType.BULLISH)).collect(Collectors.toList());
	}

	//TODO refactor into a bullish signal behaviour module
	private List<DatedSignal> calculateBullishSignals( final MovingAverageConvergenceDivergenceLines lines,
	        final LocalDate earliestSignalDate, final LocalDate latestSignalDate ) {

		final List<BigDecimal> macdValues = lines.getMacdValues();
		final List<BigDecimal> signaLine = lines.getSignaLine();
		final List<LocalDate> signalLineDates = lines.getSignalLineDates();

		final List<DatedSignal> signals = new ArrayList<>();

		// We're only interested in shared indexes, both right most aligned with data[]
		final int macdValuesOffset = Math.max(0, macdValues.size() - signaLine.size());
		final int signalLineOffset = Math.max(0, signaLine.size() - macdValues.size());
		final int endIndex = Math.min(signaLine.size(), macdValues.size());

		// Buy signal is from a cross over of the signal line, for crossing over the origin
		BigDecimal todayMacd;
		BigDecimal todaySignalLine;
		BigDecimal yesterdayMacd;
		BigDecimal yesterdaySignalLine;
		LocalDate todaySignalLineDate;

		for (int index = 1; index < endIndex; index++) {

			todaySignalLineDate = signalLineDates.get(index + signalLineOffset);

			if (isWithinSignalRange(earliestSignalDate, latestSignalDate, todaySignalLineDate)) {

				todayMacd = macdValues.get(index + macdValuesOffset);
				yesterdayMacd = macdValues.get(index + macdValuesOffset - 1);
				todaySignalLine = signaLine.get(index + signalLineOffset);
				yesterdaySignalLine = signaLine.get(index + signalLineOffset - 1);

				// The MACD trends up, with crossing the signal line OR trending up and crossing the zero line
				if (crossingSignalLine(yesterdayMacd, todayMacd, todaySignalLine, yesterdaySignalLine)
				        || crossingOrigin(yesterdayMacd, todayMacd)) {
					signals.add(new DatedSignal(todaySignalLineDate, SignalType.BULLISH));
				}
			}
		}

		return signals;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return requiredNumberOfTradingDays;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.MACD;
	}

	private boolean isWithinSignalRange( final LocalDate earliestSignalDate, final LocalDate latestSignalDate,
	        final LocalDate candidate ) {
		return isWithinEarliestSignalRange(earliestSignalDate, candidate)
		        && isWithinLatestSignalRange(latestSignalDate, candidate);
	}

	private boolean isWithinEarliestSignalRange( final LocalDate earliestSignalDate, final LocalDate candidate ) {
		return !candidate.isBefore(earliestSignalDate);
	}

	private boolean isWithinLatestSignalRange( final LocalDate latestSignalDate, final LocalDate candidate ) {
		return !candidate.isAfter(latestSignalDate);
	}

	private boolean crossingSignalLine( final BigDecimal yesterdayMacd, final BigDecimal todayMacd,
	        final BigDecimal yesterdaySignalLine, final BigDecimal todaySignalLine ) {
		/* Between yesterday and today: - MACD need to be moving upwards - today's MACD needs to be
		 * above today's signal line - yesterday's MACD needs to be below yesterday's signal line */
		return todayMacd.compareTo(yesterdayMacd) > 0 && todayMacd.compareTo(todaySignalLine) >= 0
		        && yesterdaySignalLine.compareTo(yesterdayMacd) > 0;
	}

	private boolean crossingOrigin( final BigDecimal yesterdayMacd, final BigDecimal todayMacd ) {
		return crossingSignalLine(yesterdayMacd, todayMacd, BigDecimal.ZERO, BigDecimal.ZERO);
	}
}