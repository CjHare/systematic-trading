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

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooManyDataPoints;

/**
 * Tests the ReuseIndicatorOutputStore.
 * 
 * @author CJ Hare
 */
public class ReuseIndicatorOutputStoreTest {

	@Test
	public void getStore() throws TooManyDataPoints {
		final int expectedLength = 6;
		final ReuseIndicatorOutputStore store = new ReuseIndicatorOutputStore( expectedLength );
		final TradingDayPrices[] data = new TradingDayPrices[expectedLength];

		final BigDecimal[] output = store.getStore( data.length );

		assertNotNull( output );
		assertEquals( expectedLength, output.length );
	}

	@Test(expected = TooManyDataPoints.class)
	public void getStoreTooMany() throws TooManyDataPoints {
		final int expectedLength = 6;
		final ReuseIndicatorOutputStore store = new ReuseIndicatorOutputStore( expectedLength );
		final TradingDayPrices[] data = new TradingDayPrices[expectedLength + 1];

		store.getStore( data.length );
	}

	@Test
	public void getStoreFewer() throws TooManyDataPoints {
		final int expectedLength = 6;
		final ReuseIndicatorOutputStore store = new ReuseIndicatorOutputStore( expectedLength );
		final TradingDayPrices[] data = new TradingDayPrices[expectedLength - 1];

		final BigDecimal[] output = store.getStore( data.length );

		assertNotNull( output );
		assertEquals( expectedLength, output.length );
	}

}
