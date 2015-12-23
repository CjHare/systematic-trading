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
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageCalculator;
import com.systematic.trading.maths.indicator.stochastic.StochasticPercentageK;
import com.systematic.trading.maths.indicator.stochastic.StochasticPercentageKCalculator;
import com.systematic.trading.maths.model.DatedValue;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Stochastic Oscillator is a momentum indicator that shows the location of the close relative to
 * the high-low range over a set number of periods.
 * <p/>
 * Developed by George C. Lane in the late 1950s, According to an interview with Lane, the
 * Stochastic Oscillator ï¿½doesn't follow price, it doesn't follow volume or anything like that. It
 * follows the speed or the momentum of price.
 * 
 * @author CJ Hare
 */
public class StochasticOscillatorSignals implements IndicatorSignalGenerator {

	/** Number of trading days to read the ranges on. */
	private final int lookback;

	/** Number of days for the simple moving average to smooth the %K. */
	final SimpleMovingAverage smaFullK;

	/** Number of days for the simple moving average to smooth the %D. */
	final SimpleMovingAverage smaFullD;

	/** Stochastic %K calculator */
	final StochasticPercentageK percentageK;

	public StochasticOscillatorSignals( final int lookback, final int daysOfStocastic, final int smaK, final int smaD,
			final MathContext mathContext ) {
		this.lookback = lookback;

		this.smaFullK = new SimpleMovingAverageCalculator( smaK, daysOfStocastic, new IllegalArgumentThrowingValidator(),
				mathContext );
		this.smaFullD = new SimpleMovingAverageCalculator( smaD, daysOfStocastic, new IllegalArgumentThrowingValidator(),
				mathContext );
		this.percentageK = new StochasticPercentageKCalculator( lookback, daysOfStocastic,
				new IllegalArgumentThrowingValidator(), mathContext );
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		final List<BigDecimal> fastK = percentageK.percentageK( data );
		final List<BigDecimal> fullK = smaFullK.sma( merge( data, fastK ) );
		final List<BigDecimal> fullD = smaFullD.sma( merge( data, fullK ) );

		return buySignals( merge( data, fullK ), fullD );
	}

	private DatedValue[] merge( final TradingDayPrices[] dates, final List<BigDecimal> values ) {
		final DatedValue[] merged = new DatedValue[dates.length];

		// Use the right most date values
		final int offset = dates.length - values.size();

		for (int index = 0; index < values.size(); index++) {
			merged[index] = new DatedValue( dates[index + offset].getDate(), values.get( index ) );
		}

		return merged;
	}

	protected List<IndicatorSignal> buySignals( final DatedValue[] dataPoint, final List<BigDecimal> signaline ) {
		final List<IndicatorSignal> buySignals = new ArrayList<IndicatorSignal>();

		// Use the right most date values
		final int offset = dataPoint.length - signaline.size();

		// Buy signal is from a cross over of the signal line
		BigDecimal pointToday, pointYesterday, signalLineToday, signalLineYesterday;

		for (int index = 1; index < signaline.size(); index++) {
			pointToday = dataPoint[index + offset].getValue();
			pointYesterday = dataPoint[index - 1 + offset].getValue();
			signalLineToday = signaline.get( index );
			signalLineYesterday = signaline.get( index );

			// The MACD trends up, with crossing the signal line
			// OR trending up and crossing the zero line
			if (crossingSignalLine( pointYesterday, pointToday, signalLineToday, signalLineYesterday )) {
				buySignals.add(
						new IndicatorSignal( dataPoint[index + offset].getDate(), IndicatorSignalType.STOCHASTIC ) );
			}

		}

		return buySignals;
	}

	private boolean crossingSignalLine( final BigDecimal pointYesterday, final BigDecimal pointToday,
			final BigDecimal signalLineYesterday, final BigDecimal signalLineToday ) {
		/* Between yesterday and today: - MACD need to be moving upwards - today's MACD needs to be
		 * above today's signal line - yesterday's MACD needs to be below yesterday's signal line */
		return pointToday.compareTo( pointYesterday ) > 0 && pointToday.compareTo( signalLineToday ) > 0
				&& signalLineYesterday.compareTo( pointYesterday ) > 0;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return lookback;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.STOCHASTIC;
	}
}
