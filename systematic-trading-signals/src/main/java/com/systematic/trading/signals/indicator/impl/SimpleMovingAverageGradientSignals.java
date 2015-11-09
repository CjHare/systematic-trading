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
package com.systematic.trading.signals.indicator.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.ValueWithDate;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.indicator.SimpleMovingAverage;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalType;
import com.systematic.trading.signals.indicator.SignalGenerator;

/**
 * Interested in the Simple Moving Average (SMA) gradient, whether it is negative (downward),flat
 * (no change) or positive (upward).
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignals implements SignalGenerator {

	public enum Gradient {
		NEGATIVE,
		FLAT,
		POSITIVE
	}

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** On which type of gradient does a signal get generated. */
	private final Gradient signalGenerated;

	/** Number of days to average the value on. */
	private final int lookback;

	public SimpleMovingAverageGradientSignals( final int lookback, final Gradient signalGenerated,
			final MathContext mathContext ) {
		this.signalGenerated = signalGenerated;
		this.lookback = lookback;
		this.mathContext = mathContext;
	}

	public List<IndicatorSignal> calculate( final TradingDayPrices[] data ) throws TooFewDataPoints {
		final List<IndicatorSignal> signals = new ArrayList<IndicatorSignal>();

		final ValueWithDate[] vd = convertToClosingPriceAndDate( data );
		final BigDecimal[] sma = new SimpleMovingAverage( lookback ).sma( vd );

		// Find the first non-null value
		int index = 0;
		while (index < sma.length && sma[index] == null) {
			index++;
		}

		BigDecimal previous = sma[index];
		for (; index < sma.length; index++) {

			switch (signalGenerated) {
				case POSITIVE:
					if (isPositiveGardient( previous, sma[index] )) {
						signals.add( new IndicatorSignal( data[index].getDate(), IndicatorSignalType.SMA ) );
					}
					break;
				case FLAT:
					if (isFlatGardient( previous, sma[index] )) {
						signals.add( new IndicatorSignal( data[index].getDate(), IndicatorSignalType.SMA ) );
					}
					break;
				case NEGATIVE:
					if (isNegativeGardient( previous, sma[index] )) {
						signals.add( new IndicatorSignal( data[index].getDate(), IndicatorSignalType.SMA ) );
					}
					break;
				default:
					throw new IllegalArgumentException( String.format( "%s enum is unexpected", signalGenerated ) );
			}

			previous = sma[index];
		}

		return signals;
	}

	private boolean isPositiveGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract( previous, mathContext ).compareTo( BigDecimal.ZERO ) > 0;
	}

	private boolean isNegativeGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract( previous, mathContext ).compareTo( BigDecimal.ZERO ) < 0;
	}

	private boolean isFlatGardient( final BigDecimal previous, final BigDecimal current ) {
		return current.subtract( previous, mathContext ).compareTo( BigDecimal.ZERO ) == 0;
	}

	private ValueWithDate[] convertToClosingPriceAndDate( final TradingDayPrices[] data ) {
		final ValueWithDate[] vd = new ValueWithDate[data.length];
		for (int i = 0; i < vd.length; i++) {
			vd[i] = new ValueWithDate( data[i].getDate(), data[i].getClosingPrice().getPrice() );
		}

		return vd;
	}

	@Override
	public int getMaximumNumberOfTradingDaysRequired() {
		return lookback;
	}
}
