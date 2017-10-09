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
package com.systematic.trading.signals.indicator.sma;

import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverage;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.IndicatorSignalsBase;
import com.systematic.trading.signals.indicator.SignalCalculator;

/**
 * The Simple Moving Average (SMA) gradient, whether it is negative (downward),flat
 * (no change) or positive (upward).
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignals extends IndicatorSignalsBase<SimpleMovingAverageLine>
        implements IndicatorSignalGenerator {

	/** Responsible for calculating the simple moving average. */
	private final SimpleMovingAverage sma;

	public SimpleMovingAverageGradientSignals( final IndicatorSignalId id, final int lookback, final int daysOfGradient,
	        final SimpleMovingAverage sma, final List<SignalCalculator<SimpleMovingAverageLine>> signalCalculators,
	        final SignalRangeFilter filter ) {
		super(id, lookback + daysOfGradient, signalCalculators, filter);
		this.sma = sma;
	}

	@Override
	protected SimpleMovingAverageLine indicatorCalculation( final TradingDayPrices[] data ) {
		return sma.sma(data);
	}
}