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
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.model.SignalType;
import com.systematic.trading.maths.store.IndicatorOutputStore;

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

	/** Provides the array to put the slow-fast ema value for feeding to the signal ema. */
	private final IndicatorOutputStore signalStore;

	/** Responsible for parsing and validating the input. */
	private final IndicatorInputValidator validator;

	public MovingAverageConvergenceDivergenceCalculator( final ExponentialMovingAverage fastEma,
			final ExponentialMovingAverage slowEma, final ExponentialMovingAverage signalEma,
			final IndicatorInputValidator validator, final IndicatorOutputStore signalStore ) {
		this.signalStore = signalStore;
		this.validator = validator;
		this.signalEma = signalEma;
		this.slowEma = slowEma;
		this.fastEma = fastEma;
	}

	@Override
	public List<DatedSignal> macd( final TradingDayPrices[] data ) throws TooFewDataPoints, TooManyDataPoints {

		final BigDecimal[] slowEmaValues = slowEma.ema( data );
		final BigDecimal[] fastEmaValues = fastEma.ema( data );
		final BigDecimal[] macd = signalStore.getStore( data.length );

		// Expecting the same number of input data points as outputs
		if (data.length > slowEmaValues.length) {
			throw new IllegalArgumentException(
					String.format( "The number of data points given: %s exceeds the size of the store: %s", data.length,
							slowEmaValues.length ) );
		}
		if (fastEmaValues.length != slowEmaValues.length) {
			throw new IllegalArgumentException(
					String.format( "The number of fast EMA points: %s must match the number of slow EMA ones: %s",
							fastEmaValues.length, slowEmaValues.length ) );
		}
		if (fastEmaValues.length != macd.length) {
			throw new IllegalArgumentException(
					String.format( "The number of fast EMA points: %s must match the number of Signal store ones: %s",
							fastEmaValues.length, slowEmaValues.length ) );
		}

		// Skip the null entries of the slow EMA
		int startMacdIndex = 0;
		while (startMacdIndex < slowEmaValues.length && slowEmaValues[startMacdIndex] == null) {
			startMacdIndex++;
		}
		while (startMacdIndex < fastEmaValues.length && fastEmaValues[startMacdIndex] == null) {
			startMacdIndex++;
		}

		// The arrays may not be entirely filled i.e. end contains nulls
		int endIndex = slowEmaValues.length;
		do {
			endIndex--;
		} while (endIndex > 0 && slowEmaValues[endIndex] == null);

		while (endIndex > 0 && fastEmaValues[endIndex] == null) {
			endIndex--;
		}

		// MACD is the fast - slow EMAs
		for (int i = startMacdIndex; i < endIndex; i++) {
			macd[i] = fastEmaValues[i].subtract( slowEmaValues[i] );
		}

		final BigDecimal[] signaLine = signalEma.ema( macd );

		return calculateBullishSignals( data, macd, endIndex, signaLine );
	}

	private List<DatedSignal> calculateBullishSignals( final TradingDayPrices[] data, final BigDecimal[] macdValues,
			final int macdValueEndIndex, final BigDecimal[] signaLine ) {

		final List<DatedSignal> signals = new ArrayList<DatedSignal>();

		final int endSignalLineIndex = validator.getLastNonNullIndex( signaLine );
		int index = validator.getFirstNonNullIndex( signaLine );

		// Yesterday need not be null
		index++;

		// Buy signal is from a cross over of the signal line, for crossing over the origin
		BigDecimal todayMacd, yesterdayMacd;

		for (; index < endSignalLineIndex && index < macdValueEndIndex; index++) {

			todayMacd = macdValues[index];
			yesterdayMacd = macdValues[index - 1];

			// The MACD trends up, with crossing the signal line
			// OR trending up and crossing the zero line
			if (crossingSignalLine( yesterdayMacd, todayMacd, signaLine[index - 1], signaLine[index] )
					|| crossingOrigin( yesterdayMacd, todayMacd )) {
				signals.add( new DatedSignal( data[index].getDate(), SignalType.BULLISH ) );
			}
		}

		return signals;

	}

	private boolean crossingSignalLine( final BigDecimal yesterdayMacd, final BigDecimal todayMacd,
			final BigDecimal yesterdaySignalLine, final BigDecimal todaySignalLine ) {
		/* Between yesterday and today: - MACD need to be moving upwards - today's MACD needs to be
		 * above today's signal line - yesterday's MACD needs to be below yesterday's signal line */
		return todayMacd.compareTo( yesterdayMacd ) > 0 && todayMacd.compareTo( todaySignalLine ) >= 0
				&& yesterdaySignalLine.compareTo( yesterdayMacd ) > 0;
	}

	private boolean crossingOrigin( final BigDecimal yesterdayMacd, final BigDecimal todayMacd ) {
		return crossingSignalLine( yesterdayMacd, todayMacd, BigDecimal.ZERO, BigDecimal.ZERO );
	}
}
