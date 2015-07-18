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
package com.systematic.trading.signals.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalType;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.StochasticOscillatorSignals;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.SignalFilter;

public class AnalysisLongBuySignals {

	private static final DataPointComparator DATA_POINT_COMPARATOR = new DataPointComparator();

	private final RelativeStrengthIndexSignals rsi;
	private final MovingAveragingConvergeDivergenceSignals macd;
	private final StochasticOscillatorSignals stochastic;
	private final List<SignalFilter> filters;

	public AnalysisLongBuySignals( final LongBuySignalConfiguration configuration, final List<SignalFilter> filters ) {
		this.rsi = configuration.getRelativeStrengthIndexSignals();
		this.macd = configuration.getMovingAveragingConvergeDivergenceSignals();
		this.stochastic = configuration.getStochasticOscillatorSignals();
		this.filters = filters;
	}

	public List<BuySignal> analyse( final DataPoint[] data ) throws TooFewDataPoints {

		// Correct the ordering from earliest to latest
		Arrays.sort( data, DATA_POINT_COMPARATOR );

		// Generate the indicator signals
		final Map<IndicatorSignalType, List<IndicatorSignal>> indicatorSignals = new EnumMap<IndicatorSignalType, List<IndicatorSignal>>(
				IndicatorSignalType.class );
		indicatorSignals.put( IndicatorSignalType.MACD, macd.calculate( data ) );
		indicatorSignals.put( IndicatorSignalType.RSI, rsi.calculate( data ) );
		indicatorSignals.put( IndicatorSignalType.STOCHASTIC, stochastic.calculate( data ) );

		// Apply the rule filters
		final List<BuySignal> signals = new ArrayList<BuySignal>();

		for (final SignalFilter filter : filters) {
			signals.addAll( filter.apply( indicatorSignals ) );
		}

		return signals;
	}

}
