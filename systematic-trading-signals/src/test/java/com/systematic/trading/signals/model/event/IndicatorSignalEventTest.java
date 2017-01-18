/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.signals.model.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Verifies the IndicatorSignalEvent.
 * 
 * @author CJ Hare
 */
public class IndicatorSignalEventTest {

	@Test
	public void getSignalType() {
		final LocalDate date = LocalDate.now();
		final IndicatorSignalType type = IndicatorSignalType.RSI;
		final IndicatorDirectionType direction = IndicatorDirectionType.BULLISH;

		final IndicatorSignal signal = new IndicatorSignal(date, type, direction);

		final IndicatorSignalEvent event = new IndicatorSignalEvent(signal);

		assertNotNull(event);
		assertEquals(type, event.getSignalType());
	}

	@Test
	public void getDate() {
		final LocalDate date = LocalDate.now();
		final IndicatorSignalType type = IndicatorSignalType.RSI;
		final IndicatorDirectionType direction = IndicatorDirectionType.BULLISH;
		final IndicatorSignal signal = new IndicatorSignal(date, type, direction);

		final IndicatorSignalEvent event = new IndicatorSignalEvent(signal);

		assertNotNull(event);
		assertEquals(date, event.getSignalDate());
	}

	@Test
	public void getDirectionType() {
		final LocalDate date = LocalDate.now();
		final IndicatorSignalType signal = IndicatorSignalType.RSI;
		final IndicatorDirectionType direction = IndicatorDirectionType.BULLISH;

		final IndicatorSignal indicatorSignal = new IndicatorSignal(date, signal, direction);

		final IndicatorSignalEvent event = new IndicatorSignalEvent(indicatorSignal);

		assertNotNull(event);
		assertEquals(direction, event.getDirectionType());
	}
}
