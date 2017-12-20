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
package com.systematic.trading.signal.range;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;
import com.systematic.trading.signal.range.TradingDaySignalRangeFilter;

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

	/** Filter instance being tested.*/
	private TradingDaySignalRangeFilter filter;

	@Test
	public void firstDayEarliestSinalDate() {

		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range);
		setUpFilter(range);

		final LocalDate earliest = earliestDate(data);

		verifySignalDate(0, earliest);
	}

	@Test
	public void secondDayEarliestSinalDate() {

		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range + 1);
		setUpFilter(range);

		final LocalDate earliest = earliestDate(data);

		verifySignalDate(1, earliest);
	}

	@Test
	public void tooFewFirstDayEarliestSinalDate() {

		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range - 1);
		setUpFilter(range);

		final LocalDate earliest = earliestDate(data);

		verifySignalDate(0, earliest);
	}

	@Test
	public void earliestSinalDateSingleDate() {

		final int range = 1;
		final TradingDayPrices[] data = createTradingDays(range);
		setUpFilter(range);

		final LocalDate earliest = earliestDate(data);

		verifySignalDate(0, earliest);
	}

	@Test
	public void latestSignalDate() {

		final int range = random();
		final TradingDayPrices[] data = createTradingDays(range);
		setUpFilter(range);

		final LocalDate latest = latestDate(data);

		verifySignalDate(range, latest);
	}

	@Test
	public void latestSignalDateSingleValue() {

		final int range = 1;
		final TradingDayPrices[] data = createTradingDays(range);
		setUpFilter(range);

		final LocalDate latest = latestDate(data);

		verifySignalDate(range, latest);
	}

	private void setUpFilter( final int range ) {

		filter = new TradingDaySignalRangeFilter(range);
	}

	private LocalDate earliestDate( final TradingDayPrices[] data ) {

		return filter.earliestSignalDate(data);
	}

	private LocalDate latestDate( final TradingDayPrices[] data ) {

		return filter.latestSignalDate(data);
	}

	private void verifySignalDate( final int expectedDaySinceEpoch, final LocalDate earliest ) {

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

		return RNG.nextInt(RANGE_MAXIMUM) + 5;
	}
}