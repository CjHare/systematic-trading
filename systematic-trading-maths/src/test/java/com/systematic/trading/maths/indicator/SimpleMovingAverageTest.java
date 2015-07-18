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
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.maths.ValueWithDate;
import com.systematic.trading.maths.exception.TooFewDataPoints;

public class SimpleMovingAverageTest {

	@Test
	public void sma() throws TooFewDataPoints {
		final int smoothingRange = 3;
		final SimpleMovingAverage sma = new SimpleMovingAverage( smoothingRange );

		final ValueWithDate[] data = { null, null, null, null, null,
				new ValueWithDate( LocalDate.now().plusDays( 0 ), BigDecimal.valueOf( 10 ) ),
				new ValueWithDate( LocalDate.now().plusDays( 1 ), BigDecimal.valueOf( 20 ) ),
				new ValueWithDate( LocalDate.now().plusDays( 2 ), BigDecimal.valueOf( 30 ) ),
				new ValueWithDate( LocalDate.now().plusDays( 3 ), BigDecimal.valueOf( 30 ) ),
				new ValueWithDate( LocalDate.now().plusDays( 4 ), BigDecimal.valueOf( 30 ) ) };

		BigDecimal[] simpleMovingAverage = sma.sma( data );

		assertNotNull( simpleMovingAverage );
		assertEquals( data.length, simpleMovingAverage.length );
		assertNull( simpleMovingAverage[0] );
		assertNull( simpleMovingAverage[1] );
		assertNull( simpleMovingAverage[2] );
		assertNull( simpleMovingAverage[3] );
		assertNull( simpleMovingAverage[4] );
		assertNull( simpleMovingAverage[5] );
		assertNull( simpleMovingAverage[6] );
		assertEquals( 0, BigDecimal.valueOf( 20 ).compareTo( simpleMovingAverage[7] ) );
		assertEquals( 26, simpleMovingAverage[8].intValue() );
		assertEquals( 0, BigDecimal.valueOf( 30 ).compareTo( simpleMovingAverage[9] ) );
	}

	@Test
	public void smaMinimal() throws TooFewDataPoints {
		final int smoothingRange = 2;
		final SimpleMovingAverage sma = new SimpleMovingAverage( smoothingRange );

		final ValueWithDate[] data = { null, null, null, null, null,
				new ValueWithDate( LocalDate.now().plusDays( 0 ), BigDecimal.valueOf( 10 ) ),
				new ValueWithDate( LocalDate.now().plusDays( 1 ), BigDecimal.valueOf( 20 ) ) };

		BigDecimal[] simpleMovingAverage = sma.sma( data );

		assertNotNull( simpleMovingAverage );
		assertEquals( data.length, simpleMovingAverage.length );
		assertNull( simpleMovingAverage[0] );
		assertNull( simpleMovingAverage[1] );
		assertNull( simpleMovingAverage[2] );
		assertNull( simpleMovingAverage[3] );
		assertNull( simpleMovingAverage[4] );
		assertNull( simpleMovingAverage[5] );
		assertEquals( 0, BigDecimal.valueOf( 15 ).compareTo( simpleMovingAverage[6] ) );
	}
}
