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
package com.systematic.trading.strategy.indicator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signal.range.SignalRangeFilter;
import com.systematic.trading.strategy.indicator.configuration.EmaUptrendConfiguration;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;
import com.systematic.trading.strategy.indicator.configuration.MacdConfiguration;
import com.systematic.trading.strategy.indicator.configuration.RsiConfiguration;
import com.systematic.trading.strategy.indicator.configuration.SmaUptrendConfiguration;

/**
 * Verifies the indicator factory behaves correctly.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyIndicatorFactoryTest {

	private static final int PRICE_TICKS = 5;

	@Mock
	private SignalRangeFilter filter;

	@Mock
	private SignalAnalysisListener signalListener;

	/** Factory instance being tested. */
	private TradingStrategyIndicatorFactory factory;

	@Before
	public void setUp() {
		factory = new TradingStrategyIndicatorFactory();
	}

	@Test
	public void macd() {
		final MacdConfiguration configuration = macdConfig();

		final Indicator indicator = create(configuration);

		verifyIndicator(indicator);
	}

	@Test
	public void rsi() {
		final RsiConfiguration configuration = rsiConfig();

		final Indicator indicator = create(configuration);

		verifyIndicator(indicator);
	}

	@Test
	public void smaUptrend() {
		final SmaUptrendConfiguration configuration = smaUptrendConfig();

		final Indicator indicator = create(configuration);

		verifyIndicator(indicator);
	}

	@Test
	public void emaUptrend() {
		final EmaUptrendConfiguration configuration = emaUptrendConfig();

		final Indicator indicator = create(configuration);

		verifyIndicator(indicator);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noIndicatorConfiguration() {
		final IndicatorConfiguration configuration = null;

		create(configuration);
	}

	private Indicator create( final IndicatorConfiguration signal ) {
		return factory.create(signal, filter, signalListener, PRICE_TICKS);

	}

	private MacdConfiguration macdConfig() {
		final MacdConfiguration macd = mock(MacdConfiguration.class);
		when(macd.fastTimePeriods()).thenReturn(5);
		when(macd.slowTimePeriods()).thenReturn(10);
		when(macd.signalTimePeriods()).thenReturn(2);
		return macd;
	}

	private RsiConfiguration rsiConfig() {
		final RsiConfiguration rsi = mock(RsiConfiguration.class);
		when(rsi.lookback()).thenReturn(8);
		return rsi;
	}

	private SmaUptrendConfiguration smaUptrendConfig() {
		final SmaUptrendConfiguration sma = mock(SmaUptrendConfiguration.class);
		when(sma.lookback()).thenReturn(18);
		return sma;
	}

	private EmaUptrendConfiguration emaUptrendConfig() {
		final EmaUptrendConfiguration sma = mock(EmaUptrendConfiguration.class);
		when(sma.lookback()).thenReturn(15);
		return sma;
	}

	private void verifyIndicator( final Indicator indicator ) {
		assertNotNull(indicator);
		assertTrue("Expecting instance of TradingStrategyIndicator", indicator instanceof TradingStrategyIndicator);
	}
}