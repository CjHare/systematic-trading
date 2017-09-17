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
package com.systematic.trading.maths.indicator.macd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;

/**
 * Moving Average Convergence Divergence (MACD) line calculation.
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

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	public MovingAverageConvergenceDivergenceCalculator( final ExponentialMovingAverage fastEma,
	        final ExponentialMovingAverage slowEma, final ExponentialMovingAverage signalEma,
	        final Validator validator ) {
		this.validator = validator;
		this.signalEma = signalEma;
		this.slowEma = slowEma;
		this.fastEma = fastEma;
	}

	@Override
	public MovingAverageConvergenceDivergenceLines macd( final TradingDayPrices[] data ) {
		//TODO data != null
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, 1);

		final List<BigDecimal> slowEmaValues = slowEma.ema(data);
		final List<BigDecimal> fastEmaValues = fastEma.ema(data);

		// We're only interested in shared indexes, both right most aligned with data[]
		final int slowEmaOffset = Math.max(0, slowEmaValues.size() - fastEmaValues.size());
		final int fastEmaOffset = Math.max(0, fastEmaValues.size() - slowEmaValues.size());
		final int emaEndIndex = Math.min(slowEmaValues.size(), fastEmaValues.size());
		final List<BigDecimal> macdValues = new NonNullableArrayList<>(emaEndIndex);
		final SortedMap<LocalDate, BigDecimal> macd = new TreeMap<>();
		final int macdValuesOffset = data.length - emaEndIndex;

		// MACD is the fast - slow EMAs
		for (int i = 0; i < emaEndIndex; i++) {
			final BigDecimal value = fastEmaValues.get(i + fastEmaOffset)
			        .subtract(slowEmaValues.get(i + slowEmaOffset));
			macdValues.add(value);
			macd.put(data[i + macdValuesOffset].getDate(), value);
		}

		final List<BigDecimal> signaLineEma = signalEma.ema(macdValues);
		final SortedMap<LocalDate, BigDecimal> signalLine = new TreeMap<>();
		final int signalLineOffset = data.length - signaLineEma.size();

		for (int i = signalLineOffset; i < data.length; i++) {
			signalLine.put(data[i].getDate(), signaLineEma.get(i - signalLineOffset));
		}

		return new MovingAverageConvergenceDivergenceLines(macd, signalLine);
	}
}