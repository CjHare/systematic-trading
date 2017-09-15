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

import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals;

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
	public IndicatorSignalGenerator create( final SignalConfiguration signal, final int previousTradingDaySignalRange,
	        final MathContext mathContext ) {

		if (signal instanceof MacdConfiguration) {
			return create((MacdConfiguration) signal, previousTradingDaySignalRange, mathContext);
		}
		if (signal instanceof RsiConfiguration) {
			return create((RsiConfiguration) signal, mathContext);
		}
		if (signal instanceof SmaConfiguration) {
			return create((SmaConfiguration) signal, mathContext);
		}

		throw new IllegalArgumentException(String.format("Signal type not catered for: %s", signal));
	}

	private IndicatorSignalGenerator create( final MacdConfiguration macd, final int previousTradingDaySignalRange,
	        final MathContext mathContext ) {
		return new MovingAveragingConvergeDivergenceSignals(macd.getFastTimePeriods(), macd.getSlowTimePeriods(),
		        macd.getSignalTimePeriods(), previousTradingDaySignalRange, mathContext);
	}

	//TODO use signalFilterRange
	private IndicatorSignalGenerator create( final RsiConfiguration rsi, final MathContext mathContext ) {
		return new RelativeStrengthIndexSignals(rsi.getLookback(), rsi.getOversold(), rsi.getOverbought(), mathContext);
	}

	//TODO use signalFilterRange
	private IndicatorSignalGenerator create( final SmaConfiguration sma, final MathContext mathContext ) {
		return new SimpleMovingAverageGradientSignals(sma.getLookback(), sma.getDaysOfGradient(), sma.getGradient(),
		        mathContext);
	}
}