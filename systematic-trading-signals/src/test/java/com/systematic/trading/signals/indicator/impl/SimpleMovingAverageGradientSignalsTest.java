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
package com.systematic.trading.signals.indicator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.TradingDayPricesImpl;
import com.systematic.trading.signals.indicator.impl.SimpleMovingAverageGradientSignals;
import com.systematic.trading.signals.indicator.impl.SimpleMovingAverageGradientSignals.Gradient;

/**
 * Verifies the behaviour of SimpleMovingAverageGradient.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignalsTest {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private final int lookback = 10;

	private final double[] closingPrice = { 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2, 2, 2.1, 2.1, 1.8, 1.4,
			0.9, 0.8, 1.0, 1.2 };

	private final long[] dateValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

	@Test(expected = TooFewDataPoints.class)
	public void tooFewDataPoints() throws TooFewDataPoints {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback, Gradient.POSITIVE,
				MATH_CONTEXT );

		final int tooFewDataPoints = lookback - 1;
		final TradingDayPrices[] data = new TradingDayPrices[tooFewDataPoints];
		for (int i = 0; i < tooFewDataPoints; i++) {
			data[i] = new TradingDayPricesImpl( LocalDate.now().plusDays( dateValues[i] ), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf( closingPrice[i] ) );
		}

		smaGradient.calculate( data );
	}

	@Test(expected = NullPointerException.class)
	public void unexpectedDataPoint() throws TooFewDataPoints {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback, null, MATH_CONTEXT );

		final TradingDayPrices[] data = createTradingPrices();
		smaGradient.calculate( data );
	}

	@Test
	public void signalsPositive() throws TooFewDataPoints {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback, Gradient.POSITIVE,
				MATH_CONTEXT );

		final TradingDayPrices[] data = createTradingPrices();
		final List<IndicatorSignal> signals = smaGradient.calculate( data );

		assertNotNull( signals );
		assertEquals( 5, signals.size() );
		assertEquals( data[10].getDate(), signals.get( 0 ).getDate() );
		assertEquals( data[11].getDate(), signals.get( 1 ).getDate() );
		assertEquals( data[12].getDate(), signals.get( 2 ).getDate() );
		assertEquals( data[13].getDate(), signals.get( 3 ).getDate() );
		assertEquals( data[14].getDate(), signals.get( 4 ).getDate() );
	}

	@Test
	public void signalsFlat() throws TooFewDataPoints {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback, Gradient.FLAT,
				MATH_CONTEXT );

		final TradingDayPrices[] data = createTradingPrices();
		final List<IndicatorSignal> signals = smaGradient.calculate( data );

		assertNotNull( signals );
		assertEquals( 1, signals.size() );
		assertEquals( data[9].getDate(), signals.get( 0 ).getDate() );
	}

	@Test
	public void signalsNegative() throws TooFewDataPoints {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback, Gradient.NEGATIVE,
				MATH_CONTEXT );

		final TradingDayPrices[] data = createTradingPrices();
		final List<IndicatorSignal> signals = smaGradient.calculate( data );

		assertNotNull( signals );
		assertEquals( 5, signals.size() );
		assertEquals( data[15].getDate(), signals.get( 0 ).getDate() );
		assertEquals( data[16].getDate(), signals.get( 1 ).getDate() );
		assertEquals( data[17].getDate(), signals.get( 2 ).getDate() );
		assertEquals( data[18].getDate(), signals.get( 3 ).getDate() );
		assertEquals( data[19].getDate(), signals.get( 4 ).getDate() );
	}

	private TradingDayPrices[] createTradingPrices() {
		final TradingDayPrices[] data = new TradingDayPrices[dateValues.length];
		for (int i = 0; i < dateValues.length; i++) {
			data[i] = new TradingDayPricesImpl( LocalDate.now().plusDays( dateValues[i] ), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf( closingPrice[i] ) );
		}

		return data;
	}
	
	@Test
	public void getMaximumNumberOfTradingDaysRequired() {
		final SimpleMovingAverageGradientSignals smaGradient = new SimpleMovingAverageGradientSignals( lookback,
				Gradient.NEGATIVE, MATH_CONTEXT );

		assertEquals( lookback, smaGradient.getMaximumNumberOfTradingDaysRequired() );
	}
}