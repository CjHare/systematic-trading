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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;

public class RelativeStrengthIndexSignalsTest {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private final double[] rs = { 88.89, 52.83, 61.83, 49.49, 38.27, 33.77, 40.48, 46.81, 37.11, 35.06, 40.48, 23.66,
			35.48, 32.43, 25.37, 31.97, 37.50, 42.20, 46.24, 57.98, 45.95, 46.81, 38.65, 35.06, 25.93, 29.08, 34.64,
			30.56, 18.03 };

	private final long[] dateValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 24, 25, 26, 27, 28 };

	@Test
	public void oversold() {
		final List<BigDecimal> rsValue = new ArrayList<BigDecimal>( rs.length );
		final TradingDayPrices[] dates = new TradingDayPrices[rs.length];

		for (int i = 0; i < rs.length; i++) {
			rsValue.add( BigDecimal.valueOf( rs[i] ) );

			dates[i] = new TradingDayPricesImpl( LocalDate.now().plusDays( dateValues[i] ), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO );
		}

		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 5, 30, 70, MATH_CONTEXT );

		final List<IndicatorSignal> signals = rsi.buySignals( rsValue, dates );

		assertNotNull( signals );
		assertEquals( 5, signals.size() );

		assertEquals( LocalDate.now().plusDays( 11 ), signals.get( 0 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 14 ), signals.get( 1 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 24 ), signals.get( 2 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 25 ), signals.get( 3 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 28 ), signals.get( 4 ).getDate() );
	}

	@Test
	public void getMaximumNumberOfTradingDaysRequired() {
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 5, 30, 70, MATH_CONTEXT );

		assertEquals( 57, rsi.getRequiredNumberOfTradingDays() );
	}
}
