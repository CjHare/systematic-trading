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
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.maths.model.DatedValue;

public class StochasticOscillatorSignalsTest {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Test
	public void buySignalsFlatLine() {
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 1, 1, 1, MATH_CONTEXT );

		final DatedValue[] dataPoint = { new DatedValue( LocalDate.now().plusDays( 0 ), BigDecimal.valueOf( 25 ) ),
				new DatedValue( LocalDate.now().plusDays( 1 ), BigDecimal.valueOf( 31 ) ),
				new DatedValue( LocalDate.now().plusDays( 2 ), BigDecimal.valueOf( 28 ) ),
				new DatedValue( LocalDate.now().plusDays( 3 ), BigDecimal.valueOf( 34 ) ),
				new DatedValue( LocalDate.now().plusDays( 4 ), BigDecimal.valueOf( 60 ) ) };
		final BigDecimal[] signaline = { BigDecimal.valueOf( 30 ), BigDecimal.valueOf( 30 ), BigDecimal.valueOf( 30 ),
				BigDecimal.valueOf( 30 ), BigDecimal.valueOf( 30 ) };

		final List<IndicatorSignal> signals = stochastic.buySignals( dataPoint, signaline );

		assertNotNull( signals );
		assertEquals( 2, signals.size() );
		assertEquals( dataPoint[1].getDate(), signals.get( 0 ).getDate() );
		assertEquals( dataPoint[3].getDate(), signals.get( 1 ).getDate() );
	}

	@Test
	public void buySignalsTrendingUp() {
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 1, 1, 1, MATH_CONTEXT );

		final DatedValue[] dataPoint = { new DatedValue( LocalDate.now().plusDays( 0 ), BigDecimal.valueOf( 25 ) ),
				new DatedValue( LocalDate.now().plusDays( 1 ), BigDecimal.valueOf( 32 ) ),
				new DatedValue( LocalDate.now().plusDays( 2 ), BigDecimal.valueOf( 28 ) ),
				new DatedValue( LocalDate.now().plusDays( 3 ), BigDecimal.valueOf( 34 ) ),
				new DatedValue( LocalDate.now().plusDays( 4 ), BigDecimal.valueOf( 60 ) ) };
		final BigDecimal[] signaline = { BigDecimal.valueOf( 30 ), BigDecimal.valueOf( 31 ), BigDecimal.valueOf( 32 ),
				BigDecimal.valueOf( 33 ), BigDecimal.valueOf( 34 ) };

		final List<IndicatorSignal> signals = stochastic.buySignals( dataPoint, signaline );

		assertNotNull( signals );
		assertEquals( 2, signals.size() );
		assertEquals( dataPoint[1].getDate(), signals.get( 0 ).getDate() );
		assertEquals( dataPoint[3].getDate(), signals.get( 1 ).getDate() );
	}

	@Test
	public void buySignalsTrendingDown() {
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 1, 1, 1, MATH_CONTEXT );

		final DatedValue[] dataPoint = { new DatedValue( LocalDate.now().plusDays( 0 ), BigDecimal.valueOf( 25 ) ),
				new DatedValue( LocalDate.now().plusDays( 1 ), BigDecimal.valueOf( 32 ) ),
				new DatedValue( LocalDate.now().plusDays( 2 ), BigDecimal.valueOf( 24 ) ),
				new DatedValue( LocalDate.now().plusDays( 3 ), BigDecimal.valueOf( 34 ) ),
				new DatedValue( LocalDate.now().plusDays( 4 ), BigDecimal.valueOf( 60 ) ) };
		final BigDecimal[] signaline = { BigDecimal.valueOf( 30 ), BigDecimal.valueOf( 29 ), BigDecimal.valueOf( 28 ),
				BigDecimal.valueOf( 27 ), BigDecimal.valueOf( 26 ) };

		final List<IndicatorSignal> signals = stochastic.buySignals( dataPoint, signaline );

		assertNotNull( signals );
		assertEquals( 2, signals.size() );
		assertEquals( dataPoint[1].getDate(), signals.get( 0 ).getDate() );
		assertEquals( dataPoint[3].getDate(), signals.get( 1 ).getDate() );
	}

	@Test
	public void getMaximumNumberOfTradingDaysRequired() {
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 2, 1, 1, MATH_CONTEXT );

		assertEquals( 2, stochastic.getRequiredNumberOfTradingDays() );
	}

}
