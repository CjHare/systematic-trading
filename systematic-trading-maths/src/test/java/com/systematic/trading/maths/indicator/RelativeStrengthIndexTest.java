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
package com.systematic.trading.maths.indicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.TradingDayPricesImpl;
import com.systematic.trading.maths.exception.TooFewDataPoints;

public class RelativeStrengthIndexTest {
	@Test
	public void anotherRsi() throws TooFewDataPoints {
		final double[] prices = { 15.55, 15.70, 15.80, 15.98, 16.20, 16.15, 16.15, 16.25, 16.12, 16.40, 16.67, 16.22,
				16.43, 16.20, 15.93, 15.80, 15.90, 16.00, 15.80, 15.76, 15.82, 15.40, 15.55, 15.48, 15.28, 15.35,
				15.41, 15.46, 15.50, 15.63, 15.50, 15.51, 15.42, 15.38, 15.24, 15.26, 15.29, 15.25, 15.05 };
		final TradingDayPrices[] closingPrices = new TradingDayPrices[prices.length];
		for (int i = 0; i < closingPrices.length; i++) {
			closingPrices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
					BigDecimal.valueOf( prices[i] ) );
		}

		final RelativeStrengthIndex rsi = new RelativeStrengthIndex( 10 );
		final BigDecimal[] rsiValues = rsi.rsi( closingPrices );

		assertNotNull( rsiValues );
		assertEquals( closingPrices.length, rsiValues.length );

		int index = 10;
		assertEquals( "88.89", rsiValues[index++].toString() );
		assertEquals( "52.83", rsiValues[index++].toString() );
		assertEquals( "61.83", rsiValues[index++].toString() );
		assertEquals( "49.49", rsiValues[index++].toString() );
		assertEquals( "38.27", rsiValues[index++].toString() );
		assertEquals( "33.77", rsiValues[index++].toString() );
		assertEquals( "40.48", rsiValues[index++].toString() );
		assertEquals( "46.81", rsiValues[index++].toString() );
		assertEquals( "37.11", rsiValues[index++].toString() );
		assertEquals( "35.06", rsiValues[index++].toString() );
		assertEquals( "40.48", rsiValues[index++].toString() );
		assertEquals( "23.66", rsiValues[index++].toString() );
		assertEquals( "35.48", rsiValues[index++].toString() );
		assertEquals( "32.43", rsiValues[index++].toString() );
		assertEquals( "25.37", rsiValues[index++].toString() );
		assertEquals( "31.97", rsiValues[index++].toString() );
		assertEquals( "37.50", rsiValues[index++].toString() );
		assertEquals( "42.20", rsiValues[index++].toString() );
		assertEquals( "46.24", rsiValues[index++].toString() );
		assertEquals( "57.98", rsiValues[index++].toString() );
		assertEquals( "45.95", rsiValues[index++].toString() );
		assertEquals( "46.81", rsiValues[index++].toString() );
		assertEquals( "38.65", rsiValues[index++].toString() );
		assertEquals( "35.06", rsiValues[index++].toString() );
		assertEquals( "25.93", rsiValues[index++].toString() );
		assertEquals( "29.08", rsiValues[index++].toString() );
		assertEquals( "34.64", rsiValues[index++].toString() );
		assertEquals( "30.56", rsiValues[index++].toString() );
		assertEquals( "18.03", rsiValues[index++].toString() );
	}

	@Test
	public void rsi() throws TooFewDataPoints {
		final BigDecimal[] closingPricesDb = { BigDecimal.valueOf( 93.15 ), BigDecimal.valueOf( 93.47 ),
				BigDecimal.valueOf( 90.00 ), BigDecimal.valueOf( 90.71 ), BigDecimal.valueOf( 91.14 ),
				BigDecimal.valueOf( 90.21 ), BigDecimal.valueOf( 90.41 ), BigDecimal.valueOf( 91.04 ),
				BigDecimal.valueOf( 90.87 ), BigDecimal.valueOf( 90.42 ), BigDecimal.valueOf( 91.92 ),
				BigDecimal.valueOf( 92.55 ), BigDecimal.valueOf( 91.92 ), BigDecimal.valueOf( 91.13 ),
				BigDecimal.valueOf( 91.12 ), BigDecimal.valueOf( 91.09 ), BigDecimal.valueOf( 90.52 ),
				BigDecimal.valueOf( 90.68 ), BigDecimal.valueOf( 90.52 ), BigDecimal.valueOf( 91.82 ),
				BigDecimal.valueOf( 91.32 ) };

		final TradingDayPrices[] closingPrices = new TradingDayPrices[closingPricesDb.length];
		for (int i = 0; i < closingPrices.length; i++) {
			closingPrices[i] = new TradingDayPricesImpl( LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
					closingPricesDb[i] );

		}

		final RelativeStrengthIndex rsi = new RelativeStrengthIndex( 10 );
		final BigDecimal[] rsiValues = rsi.rsi( closingPrices );

		assertNotNull( rsiValues );
		assertEquals( closingPrices.length, rsiValues.length );

		for (int i = 0; i < rsiValues.length; i++) {
			System.out.println( rsiValues[i] );
		}

		for (int i = 0; i < 10; i++) {
			assertEquals( null, rsiValues[i] );
		}

		assertEquals( "53.05", rsiValues[10].toString() );
		assertEquals( "59.51", rsiValues[11].toString() );
		assertEquals( "50.98", rsiValues[12].toString() );
		assertEquals( "41.52", rsiValues[13].toString() );
		assertEquals( "41.52", rsiValues[14].toString() );
		assertEquals( "41.18", rsiValues[15].toString() );
		assertEquals( "33.33", rsiValues[16].toString() );
		assertEquals( "37.50", rsiValues[17].toString() );
		assertEquals( "34.64", rsiValues[18].toString() );
		assertEquals( "61.54", rsiValues[19].toString() );
		assertEquals( "51.46", rsiValues[20].toString() );
	}

	@Test(expected = TooFewDataPoints.class)
	public void tooFewPrices() throws TooFewDataPoints {
		final RelativeStrengthIndex rsi = new RelativeStrengthIndex( 10 );

		final TradingDayPrices[] closingPrices = {
				new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( 93.15 ), BigDecimal.ZERO, BigDecimal.ZERO,
						BigDecimal.ZERO ),
				new TradingDayPricesImpl( LocalDate.now(), BigDecimal.valueOf( 93.47 ), BigDecimal.ZERO, BigDecimal.ZERO,
						BigDecimal.ZERO ) };

		rsi.rsi( closingPrices );
	}

	@Test
	public void divideByZero() throws TooFewDataPoints {
		// Never any downward momentum
		final BigDecimal[] closingPricesDb = { BigDecimal.valueOf( 1.1 ), BigDecimal.valueOf( 2.2 ),
				BigDecimal.valueOf( 3.3 ), BigDecimal.valueOf( 4.4 ), BigDecimal.valueOf( 5.5 ),
				BigDecimal.valueOf( 6.6 ), BigDecimal.valueOf( 7.7 ), BigDecimal.valueOf( 8.8 ),
				BigDecimal.valueOf( 10.1 ), BigDecimal.valueOf( 11.2 ), BigDecimal.valueOf( 12.3 ),
				BigDecimal.valueOf( 13.4 ), BigDecimal.valueOf( 14.5 ), BigDecimal.valueOf( 15.6 ),
				BigDecimal.valueOf( 20 ), BigDecimal.valueOf( 25 ), BigDecimal.valueOf( 30 ),
				BigDecimal.valueOf( 30.1 ), BigDecimal.valueOf( 40 ), BigDecimal.valueOf( 50 ), BigDecimal.valueOf( 60 ) };

		final TradingDayPrices[] closingPrices = new TradingDayPrices[closingPricesDb.length];
		for (int i = 0; i < closingPrices.length; i++) {
			closingPrices[i] = new TradingDayPricesImpl( LocalDate.now(), closingPricesDb[i], BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO );
		}

		final RelativeStrengthIndex rsi = new RelativeStrengthIndex( 10 );
		final BigDecimal[] rsiValues = rsi.rsi( closingPrices );

		assertNotNull( rsiValues );
		assertEquals( closingPrices.length, rsiValues.length );

		for (int i = 0; i < rsiValues.length; i++) {
			System.out.println( rsiValues[i] );
		}

		for (int i = 0; i < 10; i++) {
			assertEquals( null, rsiValues[i] );
		}

		assertEquals( "99.01", rsiValues[10].toString() );
		assertEquals( "99.01", rsiValues[11].toString() );
		assertEquals( "99.01", rsiValues[12].toString() );
		assertEquals( "99.01", rsiValues[13].toString() );
		assertEquals( "99.01", rsiValues[14].toString() );
		assertEquals( "99.01", rsiValues[15].toString() );
		assertEquals( "99.01", rsiValues[16].toString() );
		assertEquals( "99.01", rsiValues[17].toString() );
		assertEquals( "99.01", rsiValues[18].toString() );
		assertEquals( "99.01", rsiValues[19].toString() );
		assertEquals( "99.01", rsiValues[20].toString() );
	}
}
