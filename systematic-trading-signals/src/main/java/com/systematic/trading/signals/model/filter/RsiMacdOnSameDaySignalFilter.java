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
package com.systematic.trading.signals.model.filter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalType;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.IndicatorSignalDateComparator;

/**
 * Signal generated when there is a RSI and MACD filter on the same day.
 * 
 * @author CJ Hare
 */
public class RsiMacdOnSameDaySignalFilter implements SignalFilter {

	private static final IndicatorSignalDateComparator ORDER_BY_DATE = new IndicatorSignalDateComparator();

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalType, List<IndicatorSignal>> signals,
			final Comparator<BuySignal> ordering, final LocalDate latestTradingDate ) {
		validateInput( signals );

		final SortedSet<BuySignal> passedSignals = new TreeSet<BuySignal>( ordering );

		final List<IndicatorSignal> macd = signals.get( IndicatorSignalType.MACD );
		Collections.sort( macd, ORDER_BY_DATE );

		final List<IndicatorSignal> rsi = signals.get( IndicatorSignalType.RSI );
		Collections.sort( rsi, ORDER_BY_DATE );

		for (final IndicatorSignal macdSignal : macd) {

			final LocalDate date = macdSignal.getDate();
			for (final IndicatorSignal rsiSignal : rsi) {

				if (date.equals( rsiSignal.getDate() )) {
					passedSignals.add( new BuySignal( date ) );
					break;
				}
			}
		}

		return passedSignals;
	}

	private void validateInput( final Map<IndicatorSignalType, List<IndicatorSignal>> signals ) {
		if (signals.get( IndicatorSignalType.MACD ) == null) {
			throw new IllegalArgumentException( "Expecting a non-null MACD list" );
		}
		if (signals.get( IndicatorSignalType.RSI ) == null) {
			throw new IllegalArgumentException( "Expecting a non-null MACD list" );
		}
	}
}
