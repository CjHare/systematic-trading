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

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.IndicatorInputValidator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceCalculator;
import com.systematic.trading.maths.model.DatedSignal;
import com.systematic.trading.maths.store.ReuseIndicatorOutputStore;
import com.systematic.trading.maths.store.StandardIndicatorOutputStore;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class MovingAveragingConvergeDivergenceSignals implements IndicatorSignalGenerator {

	private final MovingAverageConvergenceDivergence macd;

	private final int slowTimePeriods;
	private final int signalTimePeriods;

	public MovingAveragingConvergeDivergenceSignals( final int fastTimePeriods, final int slowTimePeriods,
			final int signalTimePeriods, final int maximumTradingDays, final MathContext mathContext ) {
		this.slowTimePeriods = slowTimePeriods;
		this.signalTimePeriods = signalTimePeriods;

		final ExponentialMovingAverage slowEma = new ExponentialMovingAverageCalculator( slowTimePeriods,
				new IndicatorInputValidator(), new ReuseIndicatorOutputStore( maximumTradingDays ), mathContext );
		final ExponentialMovingAverage fastEma = new ExponentialMovingAverageCalculator( fastTimePeriods,
				new IndicatorInputValidator(), new ReuseIndicatorOutputStore( maximumTradingDays ), mathContext );
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator( signalTimePeriods,
				new IndicatorInputValidator(), new ReuseIndicatorOutputStore( maximumTradingDays ), mathContext );

		this.macd = new MovingAverageConvergenceDivergenceCalculator( fastEma, slowEma, signalEma,
				new ReuseIndicatorOutputStore( maximumTradingDays ) );
	}

	public MovingAveragingConvergeDivergenceSignals( final int fastTimePeriods, final int slowTimePeriods,
			final int signalTimePeriods, final MathContext mathContext ) {
		this.slowTimePeriods = slowTimePeriods;
		this.signalTimePeriods = signalTimePeriods;

		final ExponentialMovingAverage slowEma = new ExponentialMovingAverageCalculator( slowTimePeriods,
				new IndicatorInputValidator(), new StandardIndicatorOutputStore(), mathContext );
		final ExponentialMovingAverage fastEma = new ExponentialMovingAverageCalculator( fastTimePeriods,
				new IndicatorInputValidator(), new StandardIndicatorOutputStore(), mathContext );
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator( signalTimePeriods,
				new IndicatorInputValidator(), new StandardIndicatorOutputStore(), mathContext );

		this.macd = new MovingAverageConvergenceDivergenceCalculator( fastEma, slowEma, signalEma,
				new StandardIndicatorOutputStore() );
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data )
			throws TooFewDataPoints, TooManyDataPoints {
		final List<DatedSignal> signals = macd.macd( data );

		final List<IndicatorSignal> converted = new ArrayList<IndicatorSignal>( signals.size() );

		for (final DatedSignal signal : signals) {
			converted.add( new IndicatorSignal( signal.getDate(), IndicatorSignalType.MACD ) );
		}

		return converted;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return slowTimePeriods + signalTimePeriods;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.MACD;
	}
}
