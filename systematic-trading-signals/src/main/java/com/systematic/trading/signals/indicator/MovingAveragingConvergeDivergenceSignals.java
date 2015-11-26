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
package com.systematic.trading.signals.indicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.ValueWithDate;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.indicator.ExponentialMovingAverage;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class MovingAveragingConvergeDivergenceSignals implements IndicatorSignalGenerator {

	private static final int DAYS_OF_MACD = 3;

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	private final int slowTimePeriods;
	private final int fastTimePeriods;
	private final int signalTimePeriods;

	public MovingAveragingConvergeDivergenceSignals( final int fastTimePeriods, final int slowTimePeriods,
			final int signalTimePeriods, final MathContext mathContext ) {
		this.slowTimePeriods = slowTimePeriods;
		this.fastTimePeriods = fastTimePeriods;
		this.signalTimePeriods = signalTimePeriods;
		this.mathContext = mathContext;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) throws TooFewDataPoints {

		final ExponentialMovingAverage slowEma = new ExponentialMovingAverage( slowTimePeriods, mathContext );
		final ExponentialMovingAverage fastEma = new ExponentialMovingAverage( fastTimePeriods, mathContext );

		final ValueWithDate[] vd = convertToClosingPriceAndDate( data );
		final BigDecimal[] slowEmaValues = slowEma.ema( vd );
		final BigDecimal[] fastEmaValues = fastEma.ema( vd );
		final BigDecimal[] macd = new BigDecimal[data.length];

		// MACD is the fast - slow EMAs
		for (int i = slowTimePeriods; i < macd.length; i++) {
			macd[i] = fastEmaValues[i].subtract( slowEmaValues[i] );
		}

		// Signal line
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverage( signalTimePeriods, mathContext );
		final ValueWithDate[] macdDataPoint = new ValueWithDate[macd.length];
		for (int i = 0; i < macd.length; i++) {
			macdDataPoint[i] = new ValueWithDate( data[i].getDate(), macd[i] );
		}

		final BigDecimal[] signaline = signalEma.ema( macdDataPoint );
		return buySignals( macdDataPoint, signaline );
	}

	protected List<IndicatorSignal> buySignals( final ValueWithDate[] macdDataPoint, final BigDecimal[] signaline ) {
		final List<IndicatorSignal> buySignals = new ArrayList<IndicatorSignal>();

		// Skip the initial null entries from the MACD array
		int index = 1;
		for (; index < signaline.length - 1; index++) {
			if (signaline[index] != null) {
				// Increment the index to avoid comparison against null
				index++;
				break;
			}
		}

		// Buy signal is from a cross over of the signal line, for crossing over the origin
		BigDecimal todayMacd, yesterdayMacd;

		for (; index < signaline.length - 1; index++) {
			todayMacd = macdDataPoint[index].geValue();
			yesterdayMacd = macdDataPoint[index - 1].geValue();

			// The MACD trends up, with crossing the signal line
			// OR trending up and crossing the zero line
			if (crossingSignalLine( yesterdayMacd, todayMacd, signaline[index - 1], signaline[index] )
					|| crossingOrigin( yesterdayMacd, todayMacd )) {
				buySignals.add( new IndicatorSignal( macdDataPoint[index].getDate(), IndicatorSignalType.MACD ) );
			}

		}

		return buySignals;
	}

	private boolean crossingSignalLine( final BigDecimal yesterdayMacd, final BigDecimal todayMacd,
			final BigDecimal yesterdaySignalLine, final BigDecimal todaySignalLine ) {
		/* Between yesterday and today: - MACD need to be moving upwards - today's MACD needs to be
		 * above today's signal line - yesterday's MACD needs to be below yesterday's signal line */
		return todayMacd.compareTo( yesterdayMacd ) > 0 && todayMacd.compareTo( todaySignalLine ) > 0
				&& yesterdaySignalLine.compareTo( yesterdayMacd ) > 0;
	}

	private boolean crossingOrigin( final BigDecimal yesterdayMacd, final BigDecimal todayMacd ) {
		return crossingSignalLine( yesterdayMacd, todayMacd, BigDecimal.ZERO, BigDecimal.ZERO );
	}

	private ValueWithDate[] convertToClosingPriceAndDate( final TradingDayPrices[] data ) {
		final ValueWithDate[] vd = new ValueWithDate[data.length];
		for (int i = 0; i < vd.length; i++) {
			vd[i] = new ValueWithDate( data[i].getDate(), data[i].getClosingPrice().getPrice() );
		}

		return vd;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return slowTimePeriods + signalTimePeriods + DAYS_OF_MACD;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.MACD;
	}
}
