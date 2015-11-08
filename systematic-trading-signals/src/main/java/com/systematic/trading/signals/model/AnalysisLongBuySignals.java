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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalType;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradient;
import com.systematic.trading.signals.indicator.StochasticOscillatorSignals;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.SignalFilter;

public class AnalysisLongBuySignals implements AnalysisBuySignals {

	/** Most number of trading day data used by the signal generators. */
	private static final int MAXIMUM_NUMBER_OF_TRADING_DAYS = 50;

	/** Default ordering of signals. */
	private static final BuySignalDateComparator BUY_SIGNAL_ORDER_BY_DATE = new BuySignalDateComparator();

	/** Default ordering of prices. */
	private static final TradingDayPricesDateOrder TRADING_DAY_ORDER_BY_DATE = new TradingDayPricesDateOrder();

	private final RelativeStrengthIndexSignals rsi;
	private final MovingAveragingConvergeDivergenceSignals macd;
	private final StochasticOscillatorSignals stochastic;
	private final SimpleMovingAverageGradient sma;
	private final List<SignalFilter> filters;

	public AnalysisLongBuySignals( final LongBuySignalConfiguration configuration, final List<SignalFilter> filters ) {
		this.rsi = configuration.getRelativeStrengthIndexSignals();
		this.macd = configuration.getMovingAveragingConvergeDivergenceSignals();
		this.stochastic = configuration.getStochasticOscillatorSignals();
		this.sma = configuration.getSimpleMovingAverageGradient();
		this.filters = filters;
	}

	@Override
	public List<BuySignal> analyse( final TradingDayPrices[] data ) throws TooFewDataPoints {

		// Correct the ordering from earliest to latest
		Arrays.sort( data, TRADING_DAY_ORDER_BY_DATE );

		// Generate the indicator signals
		final Map<IndicatorSignalType, List<IndicatorSignal>> indicatorSignals = new EnumMap<IndicatorSignalType, List<IndicatorSignal>>(
				IndicatorSignalType.class );
		indicatorSignals.put( IndicatorSignalType.MACD, macd.calculate( data ) );
		indicatorSignals.put( IndicatorSignalType.RSI, rsi.calculate( data ) );
		indicatorSignals.put( IndicatorSignalType.SMA, sma.calculate( data ) );
		indicatorSignals.put( IndicatorSignalType.STOCHASTIC, stochastic.calculate( data ) );

		final LocalDate latestTradingDate = data[data.length - 1].getDate();
		final List<BuySignal> signals = new ArrayList<BuySignal>();

		// Apply the rule filters
		for (final SignalFilter filter : filters) {
			signals.addAll( filter.apply( indicatorSignals, BUY_SIGNAL_ORDER_BY_DATE, latestTradingDate ) );
		}

		return signals;
	}

	@Override
	public int getMaximumNumberOfTradingDays() {
		return MAXIMUM_NUMBER_OF_TRADING_DAYS;
	}
}
