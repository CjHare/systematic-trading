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
package com.systematic.trading.strategy.periodic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Verifying a weekly Trading Strategy Periodic performs the analysis correctly.
 * 
 * @author CJ Hare
 */
public class TradingStrategyPeriodicTest {

	/** Days in the week. */
	private static final Period WEEK = Period.ofDays(7);

	/** Today's date, the first order date of the periodic. */
	private static final LocalDate TODAY = LocalDate.now();

	/** Trading strategy periodic being tested. */
	private TradingStrategyPeriodic periodic;

	@Before
	public void setUp() {

		periodic = new TradingStrategyPeriodic(TODAY, WEEK, SignalType.BULLISH);
	}

	@Test
	public void analyseNoData() {

		final TradingDayPrices[] data = new TradingDayPrices[0];

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals);
	}

	@Test
	public void analyseNoSignal() {

		final TradingDayPrices[] data = new TradingDayPrices[1];
		data[0] = price(TODAY);

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals);
	}

	@Test
	public void analyseNoSignlDayBefore() {

		final TradingDayPrices[] data = new TradingDayPrices[2];
		data[0] = price(TODAY.minusDays(1));
		data[1] = price(TODAY);

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals);
	}

	@Test
	public void analyse() {

		final TradingDayPrices[] data = new TradingDayPrices[2];
		data[0] = price(TODAY);
		data[1] = price(TODAY.plus(WEEK));

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals, TODAY.plus(WEEK));
	}

	@Test
	public void analyseWithDayAfter() {

		final TradingDayPrices[] data = new TradingDayPrices[3];
		data[0] = price(TODAY);
		data[1] = price(TODAY.plus(WEEK));
		data[2] = price(TODAY.plus(WEEK).plusDays(1));

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals, TODAY.plus(WEEK));
	}

	@Test
	public void analyseWithDayBefore() {

		final TradingDayPrices[] data = new TradingDayPrices[3];
		data[0] = price(TODAY);
		data[1] = price(TODAY.plus(WEEK).minusDays(1));
		data[2] = price(TODAY.plus(WEEK));

		final List<DatedSignal> signals = analyse(data);

		verifySignals(signals, TODAY.plus(WEEK));
	}

	private TradingDayPrices price( final LocalDate date ) {

		return new TradingDayPricesImpl("tickerSymbol", date, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
		        BigDecimal.ZERO);
	}

	private List<DatedSignal> analyse( final TradingDayPrices[] data ) {

		return periodic.analyse(data);
	}

	private void verifySignals( final List<DatedSignal> signals, final LocalDate... signalDates ) {

		assertNotNull(signals);
		assertEquals(signalDates.length, signals.size());
	}
}