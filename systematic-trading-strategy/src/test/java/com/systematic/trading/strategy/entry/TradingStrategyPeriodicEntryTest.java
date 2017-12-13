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
package com.systematic.trading.strategy.entry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.strategy.periodic.Periodic;

/**
 * Periodic entry delegate, with a single trading data point required.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyPeriodicEntryTest {

	@Mock
	private Periodic frequency;

	/** Entry instance being tested. */
	private TradingStrategyPeriodicEntry entry;

	@Before
	public void setUp() {
		entry = new TradingStrategyPeriodicEntry(frequency);
	}

	@Test
	public void tradingDataPoints() {

		final int requiredPriceDataPoints = entry.numberOfTradingDaysRequired();

		verifyPriceDataPoints(requiredPriceDataPoints);
	}

	@Test
	public void analyse() {
		final TradingDayPrices[] data = new TradingDayPrices[5];

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysis(data, analysis);
		verifyDelegation(data);
	}

	private List<DatedSignal> analyse( final TradingDayPrices[] data ) {
		return entry.analyse(data);
	}

	private void verifyAnalysis( final TradingDayPrices[] data, final List<DatedSignal> analysis ) {
		assertEquals(data, analysis);
	}

	private void verifyPriceDataPoints( final int actual ) {
		assertEquals(1, actual);
	}

	private void verifyDelegation( final TradingDayPrices[] data ) {
		verify(frequency).analyse(data);
	}
}