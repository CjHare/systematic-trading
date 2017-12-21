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
package com.systematic.trading.strategy.indicator.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.strategy.indicator.IndicatorId;

/**
 * Verifies the IndicatorSignalEvent.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class IndicatorSignalEventTest {

	@Mock
	private IndicatorId type;

	/** Signal instance being tested. */
	private IndicatorSignal signal;

	@Before
	public void setUp() {

		signal = new IndicatorSignal(LocalDate.now(), type, SignalType.BULLISH);
	}

	@Test
	public void signalType() {

		final IndicatorSignalEvent event = event();

		assertNotNull(event);
		assertEquals(type, event.signalType());
	}

	@Test
	public void date() {

		setUpIndicatorSignal(SignalType.BULLISH);

		final IndicatorSignalEvent event = event();

		assertNotNull(event);
		assertEquals(LocalDate.now(), event.signalDate());
	}

	@Test
	public void directionTypeBullish() {

		setUpIndicatorSignal(SignalType.BULLISH);

		final IndicatorSignalEvent event = event();

		verifyDirectionType(SignalType.BULLISH, event);
	}

	@Test
	public void directionTypeBearish() {

		setUpIndicatorSignal(SignalType.BEARISH);

		final IndicatorSignalEvent event = event();

		verifyDirectionType(SignalType.BEARISH, event);
	}

	private void verifyDirectionType( final SignalType expected, final IndicatorSignalEvent event ) {

		assertNotNull(event);
		assertEquals(expected, event.directionType());
	}

	private IndicatorSignalEvent event() {

		return new IndicatorSignalEvent(signal);
	}

	private void setUpIndicatorSignal( final SignalType direction ) {

		signal = new IndicatorSignal(LocalDate.now(), type, direction);
	}
}