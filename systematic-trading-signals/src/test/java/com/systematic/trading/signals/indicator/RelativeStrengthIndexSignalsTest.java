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
package com.systematic.trading.signals.indicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class RelativeStrengthIndexSignalsTest extends SignalTest {

	@Test
	public void oversold() {
		final int buyPriceSpike = 7;

		final RelativeStrengthIndexSignals signals = new RelativeStrengthIndexSignals(5, BigDecimal.valueOf(30),
		        BigDecimal.valueOf(70), MATH_CONTEXT);

		// Create a down, then an up-spike
		final TradingDayPrices[] data = addStep(15, 15, -100,
		        addStep(30, buyPriceSpike, 100, createFlatTradingDayPrices(37, 25)));

		final List<IndicatorSignal> results = signals.calculateSignals(data);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(IndicatorSignalType.RSI, results.get(0).getSignal());
		assertEquals(IndicatorDirectionType.UP, results.get(0).getDirection());
		assertEquals(LocalDate.now().minus(buyPriceSpike, ChronoUnit.DAYS), results.get(0).getDate());
	}

	@Test
	public void overbrought() {
		final int buyPriceSpike = 7;

		final RelativeStrengthIndexSignals signals = new RelativeStrengthIndexSignals(5, BigDecimal.valueOf(30),
		        BigDecimal.valueOf(70), MATH_CONTEXT);

		// Create a up then an down-spike
		final TradingDayPrices[] data = addLinearChange(10, 10, -50, createFlatTradingDayPrices(37, 25));

		final List<IndicatorSignal> results = signals.calculateSignals(data);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(IndicatorSignalType.RSI, results.get(0).getSignal());
		assertEquals(IndicatorDirectionType.DOWN, results.get(0).getDirection());
		assertEquals(LocalDate.now().minus(buyPriceSpike, ChronoUnit.DAYS), results.get(0).getDate());
	}

	@Test
	public void getMaximumNumberOfTradingDaysRequired() {
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals(5, BigDecimal.valueOf(30),
		        BigDecimal.valueOf(70), MATH_CONTEXT);

		assertEquals(57, rsi.getRequiredNumberOfTradingDays());
	}

	@Test
	public void getSignalType() {
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals(5, BigDecimal.valueOf(30),
		        BigDecimal.valueOf(70), MATH_CONTEXT);

		assertEquals(IndicatorSignalType.RSI, rsi.getSignalType());
	}
}
