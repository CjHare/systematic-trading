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
package com.systematic.trading.backtest.configuration.signals;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageCalculator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.indicator.macd.MovingAverageConvergenceDivergenceBullishSignalCalculator;
import com.systematic.trading.signals.indicator.macd.MovingAverageConvergenceDivergenceUptrendSignalCalculator;
import com.systematic.trading.signals.indicator.macd.MovingAveragingConvergenceDivergenceSignals;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexBearishSignalCalculator;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexBullishSignalCalculator;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.sma.SimpleMovingAverageBullishGradientSignalCalculator;
import com.systematic.trading.signals.indicator.sma.SimpleMovingAverageGradientSignals;

/**
 * A singleton factory for creating signal instances.
 * 
 * @author CJ Hare
 */
public class IndicatorSignalGeneratorFactory {

	public static final IndicatorSignalGeneratorFactory INSTANCE = new IndicatorSignalGeneratorFactory();

	private IndicatorSignalGeneratorFactory() {
	}

	public static final IndicatorSignalGeneratorFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * @param previousTradingDaySignalRange how many days previous to latest trading date to generate signals on.
	 */
	public IndicatorSignalGenerator create( final SignalConfiguration signal, final SignalRangeFilter filter ) {

		if (signal instanceof MacdConfiguration) {
			return create((MacdConfiguration) signal, filter);
		}
		if (signal instanceof MacdUptrendConfiguration) {
			return create((MacdUptrendConfiguration) signal, filter);
		}
		if (signal instanceof RsiConfiguration) {
			return create((RsiConfiguration) signal, filter);
		}
		if (signal instanceof SmaUptrendConfiguration) {
			return create((SmaUptrendConfiguration) signal, filter);
		}

		throw new IllegalArgumentException(String.format("Signal type not catered for: %s", signal));
	}

	private IndicatorSignalGenerator create( final MacdUptrendConfiguration macdConfiguration,
	        final SignalRangeFilter filter ) {
		final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators = new ArrayList<>();
		signalCalculators.add(new MovingAverageConvergenceDivergenceUptrendSignalCalculator());

		return create(macdConfiguration.getFastTimePeriods(), macdConfiguration.getSlowTimePeriods(), 1,
		        new IndicatorSignalId(macdConfiguration.getDescription()), filter, signalCalculators);
	}

	private IndicatorSignalGenerator create( final MacdConfiguration macdConfiguration,
	        final SignalRangeFilter filter ) {

		final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators = new ArrayList<>();
		//TODO generate the down (bearish) signals too
		signalCalculators.add(new MovingAverageConvergenceDivergenceBullishSignalCalculator());

		return create(macdConfiguration.getFastTimePeriods(), macdConfiguration.getSlowTimePeriods(),
		        macdConfiguration.getSignalTimePeriods(), macdConfiguration.getType(), filter, signalCalculators);
	}

	private IndicatorSignalGenerator create( final int fastTimePeriods, final int slowTimePeriod,
	        final int signalTimePeriods, final IndicatorSignalId id, final SignalRangeFilter filter,
	        final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators ) {

		final ExponentialMovingAverage fastEma = new ExponentialMovingAverageCalculator(fastTimePeriods,
		        new IllegalArgumentThrowingValidator());
		final ExponentialMovingAverage slowEma = new ExponentialMovingAverageCalculator(slowTimePeriod,
		        new IllegalArgumentThrowingValidator());
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator(signalTimePeriods,
		        new IllegalArgumentThrowingValidator());

		final int requiredNumberOfTradingDays = fastEma.getMinimumNumberOfPrices() + slowEma.getMinimumNumberOfPrices()
		        + signalEma.getMinimumNumberOfPrices();

		final MovingAverageConvergenceDivergence macd = new MovingAverageConvergenceDivergenceCalculator(fastEma,
		        slowEma, signalEma, new IllegalArgumentThrowingValidator());

		return new MovingAveragingConvergenceDivergenceSignals(id, macd, requiredNumberOfTradingDays, signalCalculators,
		        filter);
	}

	private IndicatorSignalGenerator create( final RsiConfiguration rsiConfiguration, final SignalRangeFilter filter ) {

		final List<SignalCalculator<RelativeStrengthIndexLine>> signalCalculators = new ArrayList<>();
		signalCalculators.add(new RelativeStrengthIndexBullishSignalCalculator(rsiConfiguration.getOversold()));
		signalCalculators.add(new RelativeStrengthIndexBearishSignalCalculator(rsiConfiguration.getOverbought()));

		final RelativeStrengthIndexCalculator rsi = new RelativeStrengthIndexCalculator(
		        new RelativeStrengthCalculator(rsiConfiguration.getLookback(), new IllegalArgumentThrowingValidator()),
		        new IllegalArgumentThrowingValidator());

		return new RelativeStrengthIndexSignals(rsiConfiguration.getType(), rsiConfiguration.getLookback(), rsi,
		        signalCalculators, filter);
	}

	private IndicatorSignalGenerator create( final SmaUptrendConfiguration sma, final SignalRangeFilter filter ) {

		final List<SignalCalculator<SimpleMovingAverageLine>> signalCalculators = new ArrayList<>();
		signalCalculators.add(new SimpleMovingAverageBullishGradientSignalCalculator());

		final SimpleMovingAverage calculator = new SimpleMovingAverageCalculator(sma.getLookback(),
		        sma.getDaysOfGradient(), new IllegalArgumentThrowingValidator());

		return new SimpleMovingAverageGradientSignals(sma.getType(), sma.getLookback(), sma.getDaysOfGradient(),
		        calculator, signalCalculators, filter);
	}
}