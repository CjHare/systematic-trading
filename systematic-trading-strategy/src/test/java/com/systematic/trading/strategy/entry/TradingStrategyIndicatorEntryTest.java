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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.strategy.indicator.Indicator;

/**
 * Indicator entry delegate.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyIndicatorEntryTest {

	@Mock
	private Indicator indicator;

	@Mock
	private List<DatedSignal> expectedAnalysis;

	/** Entry instance being tested. */
	private TradingStrategyIndicatorEntry entry;

	@Before
	public void setUp() {

		entry = new TradingStrategyIndicatorEntry(indicator);
		setUpIndicator();
	}

	@Test
	public void tradingDataPoints() {

		setUpTradingDataPoints(7);

		final int requiredPriceDataPoints = entry.requiredTradingPrices();

		verifyPriceDataPoints(7, requiredPriceDataPoints);
		verifyTradingDataPointsDelegation();
	}

	@Test
	public void analyse() {

		final TradingDayPrices[] data = new TradingDayPrices[5];

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysis(analysis);
		verifyAnalysisDelegation(data);
	}

	private void setUpTradingDataPoints( final int dataPoints ) {

		when(indicator.requiredTradingPrices()).thenReturn(dataPoints);
	}

	private void setUpIndicator() {

		when(indicator.analyse(any(TradingDayPrices[].class))).thenReturn(expectedAnalysis);
	}

	private List<DatedSignal> analyse( final TradingDayPrices[] data ) {

		return entry.analyse(data);
	}

	private void verifyAnalysis( final List<DatedSignal> analysis ) {

		assertEquals(expectedAnalysis, analysis);
	}

	private void verifyPriceDataPoints( final int expected, final int actual ) {

		assertEquals(expected, actual);
	}

	private void verifyTradingDataPointsDelegation() {

		verify(indicator).requiredTradingPrices();
	}

	private void verifyAnalysisDelegation( final TradingDayPrices[] data ) {

		verify(indicator).analyse(data);
	}
}