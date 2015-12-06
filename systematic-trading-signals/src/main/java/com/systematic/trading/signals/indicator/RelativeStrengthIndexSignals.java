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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;
import com.systematic.trading.maths.indicator.StandardIndicatorOutputStore;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Takes the output from RSI and identifies any buy signals using a double cross over of RSI
 * signals.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexSignals implements IndicatorSignalGenerator {

	private static final int DAYS_OF_RSI_VALUES = 1;
	private static final int FIVE_TRADING_DAYS = 5;
	private static final int TEN_TRADING_DAYS = 10;

	private RelativeStrengthIndex shortSignal;

	private RelativeStrengthIndex longSignal;

	private final BigDecimal oversold, overbought;

	public RelativeStrengthIndexSignals( final int oversold, final int overbought, final MathContext mathContext ) {
		this.oversold = BigDecimal.valueOf( oversold );
		this.overbought = BigDecimal.valueOf( overbought );

		// TODO maximum number of trading days? from where & use the Reuse stores
		this.shortSignal = new RelativeStrengthIndexCalculator( FIVE_TRADING_DAYS, new StandardIndicatorOutputStore(),
				new StandardIndicatorOutputStore(), mathContext );
		this.longSignal = new RelativeStrengthIndexCalculator( TEN_TRADING_DAYS, new StandardIndicatorOutputStore(),
				new StandardIndicatorOutputStore(), mathContext );
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data )
			throws TooFewDataPoints, TooManyDataPoints {
		// 5 day RSI
		final BigDecimal[] fiveDayRsi = shortSignal.rsi( data );

		// 14 day RSI
		final BigDecimal[] tenDayRsi = longSignal.rsi( data );

		/* RSI triggers a buy signal when crossing the over brought level (e.g. 30) */
		final List<IndicatorSignal> tenDayBuy = buySignals( tenDayRsi, data );
		final List<IndicatorSignal> fiveDayBuy = buySignals( fiveDayRsi, data );

		// Create buy signals for 5 & 14 RSI then keep those contained in both
		return intersection( fiveDayBuy, tenDayBuy );
	}

	protected List<IndicatorSignal> buySignals( final BigDecimal[] rsi, final TradingDayPrices[] data ) {
		final List<IndicatorSignal> buySignals = new ArrayList<IndicatorSignal>();

		// Skip the initial null entries from the RSI array
		int index = 1;
		for (; index < rsi.length - 1; index++) {
			if (rsi[index] != null) {
				// Increment the index to avoid comparison against null
				index++;
				break;
			}
		}

		// Moving from below over brought to above
		for (; index < rsi.length - 1; index++) {
			if (rsi[index].compareTo( rsi[index - 1] ) > 0 && oversold.compareTo( rsi[index] ) <= 0
					&& oversold.compareTo( rsi[index - 1] ) > 0) {
				buySignals.add( new IndicatorSignal( data[index].getDate(), IndicatorSignalType.RSI ) );
			}
		}

		return buySignals;
	}

	protected List<IndicatorSignal> intersection( final List<IndicatorSignal> a, final List<IndicatorSignal> b ) {
		final List<IndicatorSignal> intersection = new ArrayList<IndicatorSignal>();
		final List<IndicatorSignal> shorter = a.size() < b.size() ? a : b;
		final List<IndicatorSignal> larger = a.size() >= b.size() ? a : b;

		for (final IndicatorSignal signal : shorter) {
			// Match on the dates
			final LocalDate aDate = signal.getDate();
			if (contains( aDate, larger )) {
				intersection.add( signal );
			}
		}

		return intersection;
	}

	private boolean contains( final LocalDate date, final List<IndicatorSignal> signals ) {
		for (final IndicatorSignal signal : signals) {
			if (date.equals( signal.getDate() )) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return TEN_TRADING_DAYS + DAYS_OF_RSI_VALUES;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.RSI;
	}
}
