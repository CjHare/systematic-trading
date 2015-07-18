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
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.maths.exception.TooFewDataPoints;

public class RelativeStrengthIndexSignalsTest {
	private final double[] rsi = { 88.89, 52.83, 61.83, 49.49, 38.27, 33.77, 40.48, 46.81, 37.11, 35.06, 40.48, 23.66,
			35.48, 32.43, 25.37, 31.97, 37.50, 42.20, 46.24, 57.98, 45.95, 46.81, 38.65, 35.06, 25.93, 29.08, 34.64,
			30.56, 18.03 };

	private final long[] dateValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 24, 25, 26, 27, 28 };

	@Test
	public void oversold() throws TooFewDataPoints {
		final BigDecimal[] rsiValue = new BigDecimal[rsi.length];
		final DataPoint[] dates = new DataPoint[rsi.length];

		for (int i = 0; i < rsiValue.length; i++) {
			rsiValue[i] = BigDecimal.valueOf( rsi[i] );

			dates[i] = new DataPointImpl( LocalDate.now().plusDays( dateValues[i] ), BigDecimal.ZERO, BigDecimal.ZERO,
					BigDecimal.ZERO );
		}

		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 30, 70 );

		final List<IndicatorSignal> signals = rsi.buySignals( rsiValue, dates );

		assertNotNull( signals );
		assertEquals( 3, signals.size() );
		assertEquals( LocalDate.now().plusDays( 12 ), signals.get( 0 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 15 ), signals.get( 1 ).getDate() );
		assertEquals( LocalDate.now().plusDays( 26 ), signals.get( 2 ).getDate() );
	}

	class DataPointImpl implements DataPoint {

		private final LocalDate date;
		private final BigDecimal closingPrice;
		private final BigDecimal lowstPrice;
		private final BigDecimal highestPrice;

		public DataPointImpl( final LocalDate date, final BigDecimal lowstPrice, final BigDecimal highestPrice,
				final BigDecimal closingPrice ) {
			this.date = date;
			this.closingPrice = closingPrice;
			this.lowstPrice = lowstPrice;
			this.highestPrice = highestPrice;
		}

		public LocalDate getDate() {
			return date;
		}

		public BigDecimal getClosingPrice() {
			return closingPrice;
		}

		public BigDecimal getLowestPrice() {
			return lowstPrice;
		}

		public BigDecimal getHighestPrice() {
			return highestPrice;
		}

		@Override
		public String getTickerSymbol() {
			return "";
		}
	}
}
