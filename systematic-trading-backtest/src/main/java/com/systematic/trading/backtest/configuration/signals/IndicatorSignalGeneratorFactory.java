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

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.maths.formula.rs.RelativeStrengthCalculator;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLines;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals;
import com.systematic.trading.signals.indicator.macd.MovingAverageConvergenceDivergenceBullishSignalCalculator;
import com.systematic.trading.signals.indicator.macd.MovingAverageConvergenceDivergenceUptrendSignalCalculator;
import com.systematic.trading.signals.indicator.macd.MovingAveragingConvergenceDivergenceSignals;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexBearishSignalCalculator;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexBullishSignalCalculator;
import com.systematic.trading.signals.indicator.rsi.RelativeStrengthIndexSignals;

/**
 * Creates the signal instances.
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
	public IndicatorSignalGenerator create( final SignalConfiguration signal, final SignalRangeFilter filter,
	        final MathContext mathContext ) {

		if (signal instanceof MacdConfiguration) {
			return create((MacdConfiguration) signal, filter, mathContext);
		}
		if (signal instanceof MacdUptrendConfiguration) {
			return create((MacdUptrendConfiguration) signal, filter, mathContext);
		}
		if (signal instanceof RsiConfiguration) {
			return create((RsiConfiguration) signal, filter, mathContext);
		}
		if (signal instanceof SmaConfiguration) {
			return create((SmaConfiguration) signal, filter, mathContext);
		}

		throw new IllegalArgumentException(String.format("Signal type not catered for: %s", signal));
	}

	private IndicatorSignalGenerator create( final MacdUptrendConfiguration macdConfiguration,
	        final SignalRangeFilter filter, final MathContext mathContext ) {
		final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators = new ArrayList<>();
		signalCalculators.add(new MovingAverageConvergenceDivergenceUptrendSignalCalculator());

		return create(macdConfiguration.getFastTimePeriods(), macdConfiguration.getSlowTimePeriods(), 1, filter,
		        signalCalculators, mathContext);
	}

	private IndicatorSignalGenerator create( final MacdConfiguration macdConfiguration, final SignalRangeFilter filter,
	        final MathContext mathContext ) {

		final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators = new ArrayList<>();
		//TODO generate the down (bearish) signals too
		signalCalculators.add(new MovingAverageConvergenceDivergenceBullishSignalCalculator());

		return create(macdConfiguration.getFastTimePeriods(), macdConfiguration.getSlowTimePeriods(),
		        macdConfiguration.getSignalTimePeriods(), filter, signalCalculators, mathContext);
	}

	private IndicatorSignalGenerator create( final int fastTimePeriods, final int slowTimePeriod,
	        final int signalTimePeriods, final SignalRangeFilter filter,
	        final List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators,
	        final MathContext mathContext ) {

		final ExponentialMovingAverage fastEma = new ExponentialMovingAverageCalculator(fastTimePeriods,
		        new IllegalArgumentThrowingValidator(), mathContext);
		final ExponentialMovingAverage slowEma = new ExponentialMovingAverageCalculator(slowTimePeriod,
		        new IllegalArgumentThrowingValidator(), mathContext);
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator(signalTimePeriods,
		        new IllegalArgumentThrowingValidator(), mathContext);

		final int requiredNumberOfTradingDays = fastEma.getMinimumNumberOfPrices() + slowEma.getMinimumNumberOfPrices()
		        + signalEma.getMinimumNumberOfPrices();

		final MovingAverageConvergenceDivergence macd = new MovingAverageConvergenceDivergenceCalculator(fastEma,
		        slowEma, signalEma, new IllegalArgumentThrowingValidator());

		return new MovingAveragingConvergenceDivergenceSignals(macd, requiredNumberOfTradingDays, signalCalculators,
		        filter);
	}

	private IndicatorSignalGenerator create( final RsiConfiguration rsiConfiguration, final SignalRangeFilter filter,
	        final MathContext mathContext ) {

		final List<SignalCalculator<RelativeStrengthIndexLines>> signalCalculators = new ArrayList<>();
		signalCalculators.add(new RelativeStrengthIndexBullishSignalCalculator(rsiConfiguration.getOversold()));
		signalCalculators.add(new RelativeStrengthIndexBearishSignalCalculator(rsiConfiguration.getOverbought()));

		final RelativeStrengthIndexCalculator rsi = new RelativeStrengthIndexCalculator(
		        new RelativeStrengthCalculator(rsiConfiguration.getLookback(), new IllegalArgumentThrowingValidator(),
		                mathContext),
		        new IllegalArgumentThrowingValidator(), mathContext);

		return new RelativeStrengthIndexSignals(rsiConfiguration.getLookback(), rsi, signalCalculators, filter,
		        mathContext);
	}

	private IndicatorSignalGenerator create( final SmaConfiguration sma, final SignalRangeFilter filter,
	        final MathContext mathContext ) {
		return new SimpleMovingAverageGradientSignals(sma.getLookback(), sma.getDaysOfGradient(), sma.getGradient(),
		        filter, mathContext);
	}
}