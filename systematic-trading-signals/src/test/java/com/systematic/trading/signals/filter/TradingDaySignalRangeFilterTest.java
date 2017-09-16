/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.signals.filter;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;

/**
 * Verifying the expected behaviour of a TradingDaySignalRangeFilter.
 * 
 * @author CJ Hare
 */
public class TradingDaySignalRangeFilterTest {

	/** Prevent any crazy long tests. */
	private static final int RANGE_MAXIMUM = 50;

	/** Randomize the range. */
	private static final Random RNG = new Random();

	@Test
	public void firstDayEarliestSinalDate() {
		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range);

		final LocalDate earliest = new TradingDaySignalRangeFilter(range).getEarliestSignalDate(data);

		verifyEarliest(0, earliest);
	}

	@Test
	public void secondDayEarliestSinalDate() {
		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range + 1);

		final LocalDate earliest = new TradingDaySignalRangeFilter(range).getEarliestSignalDate(data);

		verifyEarliest(1, earliest);
	}

	@Test
	public void tooFewFirstDayEarliestSinalDate() {
		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range - 1);

		final LocalDate earliest = new TradingDaySignalRangeFilter(range).getEarliestSignalDate(data);

		verifyEarliest(0, earliest);
	}

	private void verifyEarliest( final int expectedDaySinceEpoch, final LocalDate earliest ) {
		assertEquals(LocalDate.ofEpochDay(expectedDaySinceEpoch), earliest);
	}

	private TradingDayPrices[] createTradingDays( final int numberOfDays ) {
		final int size = numberOfDays + 1;
		final TradingDayPrices[] days = new TradingDayPrices[size];

		for (int i = 0; i < days.length; i++) {
			days[i] = new TradingDayPricesImpl("ABC", LocalDate.ofEpochDay(i), BigDecimal.ONE, BigDecimal.ONE,
			        BigDecimal.ONE, BigDecimal.ONE);
		}

		return days;
	}

	private int random() {
		return RNG.nextInt(RANGE_MAXIMUM);
	}
}