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
package com.systematic.trading.backtest.configuration;

import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.ema.ClosingPriceExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverage;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageCalculator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageIndicator;
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageLine;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceCalculator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceIndicator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.maths.indicator.rs.ClosingPriceRelativeStrengthCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexIndicator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.maths.indicator.sma.ClosingPriceSimpleMovingAverageCalculator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageIndicator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signal.generator.SignalGenerator;
import com.systematic.trading.signal.generator.ema.ExponentialMovingAverageBullishGradientSignalGenerator;
import com.systematic.trading.signal.generator.macd.MovingAverageConvergenceDivergenceBullishSignalGenerator;
import com.systematic.trading.signal.generator.rsi.RelativeStrengthIndexBullishSignalGenerator;
import com.systematic.trading.signal.generator.sma.SimpleMovingAverageBullishGradientSignalGenerator;
import com.systematic.trading.signal.range.SignalRangeFilter;
import com.systematic.trading.strategy.indicator.Indicator;
import com.systematic.trading.strategy.indicator.TradingStrategyIndicator;
import com.systematic.trading.strategy.indicator.configuration.EmaUptrendConfiguration;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;
import com.systematic.trading.strategy.indicator.configuration.MacdConfiguration;
import com.systematic.trading.strategy.indicator.configuration.RsiConfiguration;
import com.systematic.trading.strategy.indicator.configuration.SmaUptrendConfiguration;
import com.systematic.trading.strategy.signal.SignalAnalysisListener;

/**
 * Factory methods for creating Indicators.
 * 
 * @author CJ Hare
 */
public class TradingStrategyIndicatorFactory {

	/**
	 * @param previousTradingDaySignalRange
	 *            how many days previous to latest trading date to generate signals on.
	 */
	public Indicator create( final IndicatorConfiguration signal, final SignalRangeFilter filter,
	        final SignalAnalysisListener signalListener, final int priceDataRange ) {

		if (signal instanceof MacdConfiguration) {
			return macd((MacdConfiguration) signal, filter, signalListener, priceDataRange);
		}
		if (signal instanceof RsiConfiguration) {
			return rsi((RsiConfiguration) signal, filter, signalListener, priceDataRange);
		}
		if (signal instanceof SmaUptrendConfiguration) {
			return smaUptrend((SmaUptrendConfiguration) signal, filter, signalListener, priceDataRange);
		}
		if (signal instanceof EmaUptrendConfiguration) {
			return emaUptrand((EmaUptrendConfiguration) signal, filter, signalListener, priceDataRange);
		}

		throw new IllegalArgumentException(String.format("Signal type not catered for: %s of %s", signal,
		        signal == null ? "null" : signal.getClass()));
	}

	private Indicator rsi( final RsiConfiguration rsiConfiguration, final SignalRangeFilter filter,
	        final SignalAnalysisListener signalListener, final int priceDataRange ) {

		final SignalGenerator<RelativeStrengthIndexLine> generator = new RelativeStrengthIndexBullishSignalGenerator(
		        rsiConfiguration.oversold());
		final RelativeStrengthIndexCalculator calculator = new RelativeStrengthIndexCalculator(
		        new ClosingPriceRelativeStrengthCalculator(rsiConfiguration.lookback(), priceDataRange,
		                new IllegalArgumentThrowingValidator()),
		        new IllegalArgumentThrowingValidator());

		return new TradingStrategyIndicator<RelativeStrengthIndexLine, RelativeStrengthIndexIndicator>(
		        rsiConfiguration.id(), calculator, generator, filter, signalListener);
	}

	private Indicator smaUptrend( final SmaUptrendConfiguration sma, final SignalRangeFilter filter,
	        final SignalAnalysisListener signalListener, final int priceDataRange ) {

		final int minimumNumberOfSmaValues = priceDataRange + sma.gradientPoints();
		final SignalGenerator<SimpleMovingAverageLine> generator = new SimpleMovingAverageBullishGradientSignalGenerator();
		final SimpleMovingAverageIndicator calculator = new ClosingPriceSimpleMovingAverageCalculator(sma.lookback(),
		        minimumNumberOfSmaValues, new IllegalArgumentThrowingValidator());

		return new TradingStrategyIndicator<SimpleMovingAverageLine, SimpleMovingAverageIndicator>(sma.id(), calculator,
		        generator, filter, signalListener);
	}

	private Indicator emaUptrand( final EmaUptrendConfiguration ema, final SignalRangeFilter filter,
	        final SignalAnalysisListener signalListener, final int priceDataRange ) {

		final SignalGenerator<ExponentialMovingAverageLine> generator = new ExponentialMovingAverageBullishGradientSignalGenerator();
		final int minimumNumberOfEmaValues = priceDataRange + ema.gradientPoints();
		final ExponentialMovingAverageIndicator calculator = new ClosingPriceExponentialMovingAverageCalculator(
		        ema.lookback(), minimumNumberOfEmaValues, new IllegalArgumentThrowingValidator());

		return new TradingStrategyIndicator<ExponentialMovingAverageLine, ExponentialMovingAverageIndicator>(ema.id(),
		        calculator, generator, filter, signalListener);
	}

	/**
	 * @param priceDataRange
	 *            the number of MACD values to create to use in evaluation (perhaps in conjunction
	 *            with other indicators).
	 */
	private Indicator macd( final MacdConfiguration macdConfiguration, final SignalRangeFilter filter,
	        final SignalAnalysisListener signalListener, final int priceDataRange ) {

		final SignalGenerator<MovingAverageConvergenceDivergenceLines> generator = new MovingAverageConvergenceDivergenceBullishSignalGenerator();
		final int minimumNumberOfEmaValues = priceDataRange + macdConfiguration.signalTimePeriods();
		final ExponentialMovingAverageIndicator fastEma = new ClosingPriceExponentialMovingAverageCalculator(
		        macdConfiguration.fastTimePeriods(), minimumNumberOfEmaValues, new IllegalArgumentThrowingValidator());
		final ExponentialMovingAverageIndicator slowEma = new ClosingPriceExponentialMovingAverageCalculator(
		        macdConfiguration.slowTimePeriods(), minimumNumberOfEmaValues, new IllegalArgumentThrowingValidator());
		final ExponentialMovingAverage signalEma = new ExponentialMovingAverageCalculator(
		        macdConfiguration.signalTimePeriods(), new IllegalArgumentThrowingValidator());
		final MovingAverageConvergenceDivergenceIndicator macd = new MovingAverageConvergenceDivergenceCalculator(
		        fastEma, slowEma, signalEma, new IllegalArgumentThrowingValidator());

		return new TradingStrategyIndicator<MovingAverageConvergenceDivergenceLines, MovingAverageConvergenceDivergenceIndicator>(
		        macdConfiguration.id(), macd, generator, filter, signalListener);
	}
}