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
package com.systematic.trading.maths.indicator.macd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.model.SignalType;

/**
 * Moving Average Convergence Divergence (MACD) only using the crossover for signals.
 * 
 * @author CJ Hare
 */
public class MovingAverageConvergenceDivergenceCalculator implements MovingAverageConvergenceDivergence {

	/** Larger of the exponential moving average values. */
	private final ExponentialMovingAverage slowEma;

	/** Shorter of the exponential moving average values. */
	private final ExponentialMovingAverage fastEma;

	/** Exponential moving average of the values from slowEma - fastEma. */
	private final ExponentialMovingAverage signalEma;

	/** Provides a store for the slow-fast ema value for feeding to the signal ema. */
	private final List<BigDecimal> macdValues;

	/** Provides a store for the dates that match the signal line entries. */
	private final List<LocalDate> signalLineDates;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** The date of the last processed price data. */
	private LocalDate lastDateProcessed = LocalDate.MIN;

	public MovingAverageConvergenceDivergenceCalculator(final ExponentialMovingAverage fastEma,
	        final ExponentialMovingAverage slowEma, final ExponentialMovingAverage signalEma,
	        final Validator validator) {
		this.macdValues = new NonNullableArrayList<BigDecimal>();
		this.signalLineDates = new NonNullableArrayList<LocalDate>();
		this.validator = validator;
		this.signalEma = signalEma;
		this.slowEma = slowEma;
		this.fastEma = fastEma;
	}

	@Override
	public List<DatedSignal> macd( final TradingDayPrices[] data ) {

		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, 1);

		final List<BigDecimal> slowEmaValues = slowEma.ema(data);
		final List<BigDecimal> fastEmaValues = fastEma.ema(data);

		// Clear the stores, readying them for use
		macdValues.clear();
		signalLineDates.clear();

		// We're only interested in shared indexes, both right most aligned with data[]
		final int slowEmaOffset = Math.max(0, slowEmaValues.size() - fastEmaValues.size());
		final int fastEmaOffset = Math.max(0, fastEmaValues.size() - slowEmaValues.size());
		final int emaEndIndex = Math.min(slowEmaValues.size(), fastEmaValues.size());

		// MACD is the fast - slow EMAs
		for (int i = 0; i < emaEndIndex; i++) {
			macdValues.add(fastEmaValues.get(i + fastEmaOffset).subtract(slowEmaValues.get(i + slowEmaOffset)));
		}

		final List<BigDecimal> signaLine = signalEma.ema(macdValues);

		// The signal line matches with the right most values of the data array
		for (int i = data.length - signaLine.size(); i < data.length; i++) {
			signalLineDates.add(data[i].getDate());
		}

		final List<DatedSignal> bullishSignals = calculateBullishSignals(macdValues, signaLine, signalLineDates);

		lastDateProcessed = data[data.length - 1].getDate();

		return bullishSignals;
	}

	private List<DatedSignal> calculateBullishSignals( final List<BigDecimal> macdValues,
	        final List<BigDecimal> signaLine, final List<LocalDate> signalLineDates ) {

		final List<DatedSignal> signals = new ArrayList<DatedSignal>();

		// We're only interested in shared indexes, both right most aligned with data[]
		final int macdValuesOffset = Math.max(0, macdValues.size() - signaLine.size());
		final int signalLineOffset = Math.max(0, signaLine.size() - macdValues.size());
		final int endIndex = Math.min(signaLine.size(), macdValues.size());

		// Buy signal is from a cross over of the signal line, for crossing over the origin
		BigDecimal todayMacd, todaySignalLine, yesterdayMacd, yesterdaySignalLine;
		LocalDate todaySignalLineDate;

		for (int index = 1; index < endIndex; index++) {

			todayMacd = macdValues.get(index + macdValuesOffset);
			yesterdayMacd = macdValues.get(index + macdValuesOffset - 1);
			todaySignalLine = signaLine.get(index + signalLineOffset);
			yesterdaySignalLine = signaLine.get(index + signalLineOffset - 1);
			todaySignalLineDate = signalLineDates.get(index + signalLineOffset);

			if (isAfterLastDateProcessed(todaySignalLineDate)) {
				// The MACD trends up, with crossing the signal line
				// OR trending up and crossing the zero line
				if (crossingSignalLine(yesterdayMacd, todayMacd, todaySignalLine, yesterdaySignalLine)
				        || crossingOrigin(yesterdayMacd, todayMacd)) {
					signals.add(new DatedSignal(todaySignalLineDate, SignalType.BULLISH));
				}
			}
		}

		return signals;

	}

	private boolean isAfterLastDateProcessed( final LocalDate date ) {
		return date.isAfter(lastDateProcessed);
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
