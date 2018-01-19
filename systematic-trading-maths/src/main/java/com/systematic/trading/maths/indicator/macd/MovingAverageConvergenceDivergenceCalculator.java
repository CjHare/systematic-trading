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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageIndicator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageLine;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;

/**
 * Moving Average Convergence Divergence (MACD) line calculation.
 * 
 * @author CJ Hare
 */
public class MovingAverageConvergenceDivergenceCalculator implements MovingAverageConvergenceDivergenceIndicator {

	/** Larger of the exponential moving average values. */
	private final ExponentialMovingAverageIndicator slowEma;

	/** Shorter of the exponential moving average values. */
	private final ExponentialMovingAverageIndicator fastEma;

	/** Exponential moving average of the values from slowEma - fastEma. */
	private final ExponentialMovingAverage signalEma;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	public MovingAverageConvergenceDivergenceCalculator( final ExponentialMovingAverageIndicator fastEma,
	        final ExponentialMovingAverageIndicator slowEma, final ExponentialMovingAverage signalEma,
	        final Validator validator ) {

		this.validator = validator;
		this.signalEma = signalEma;
		this.slowEma = slowEma;
		this.fastEma = fastEma;
	}

	@Override
	public int minimumNumberOfPrices() {

		return slowEma.minimumNumberOfPrices();
	}

	@Override
	public MovingAverageConvergenceDivergenceLines calculate( final TradingDayPrices[] data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, 1);

		final ExponentialMovingAverageLine slowEmaValues = slowEma.calculate(data);
		final ExponentialMovingAverageLine fastEmaValues = fastEma.calculate(data);

		final SortedMap<LocalDate, BigDecimal> macd = new TreeMap<>();
		final SortedMap<LocalDate, BigDecimal> slow = slowEmaValues.ema();
		final SortedMap<LocalDate, BigDecimal> fast = fastEmaValues.ema();
		LocalDate today;

		for (final Map.Entry<LocalDate, BigDecimal> slowEntry : slow.entrySet()) {
			today = slowEntry.getKey();

			if (fast.containsKey(today)) {
				macd.put(today, fast.get(today).subtract(slowEntry.getValue()));
			}
		}

		return new MovingAverageConvergenceDivergenceLines(macd, signalEma.calculate(macd).ema());
	}
}