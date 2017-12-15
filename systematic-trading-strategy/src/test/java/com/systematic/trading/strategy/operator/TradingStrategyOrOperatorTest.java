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
package com.systematic.trading.strategy.operator;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Verifies the trading strategy OR operator performs as a logical OR.
 * 
 * @author CJ Hare
 */
public class TradingStrategyOrOperatorTest {

	/** OR instance being tested. */
	private TradingStrategyOrOperator operator;

	@Before
	public void setUp() {
		operator = new TradingStrategyOrOperator();
	}

	@Test
	public void conjoin() {
		final List<DatedSignal> left = signals(LocalDate.of(2011, 4, 18));
		final List<DatedSignal> right = signals(LocalDate.of(2011, 5, 21), LocalDate.of(2011, 6, 26));

		final List<DatedSignal> signals = conjoin(left, right);

		verifySignals(signals(LocalDate.of(2011, 4, 18), LocalDate.of(2011, 5, 21), LocalDate.of(2011, 6, 26)),
		        signals);
	}

	@Test
	public void conjoinLeftOnly() {
		final List<DatedSignal> left = signals(LocalDate.of(2011, 5, 21));
		final List<DatedSignal> right = signals();

		final List<DatedSignal> signals = conjoin(left, right);

		verifySignals(signals(LocalDate.of(2011, 5, 21)), signals);
	}

	@Test
	public void conjoinRightOnly() {
		final List<DatedSignal> left = signals();
		final List<DatedSignal> right = signals(LocalDate.of(2011, 5, 21));

		final List<DatedSignal> signals = conjoin(left, right);

		verifySignals(signals(LocalDate.of(2011, 5, 21)), signals);
	}

	@Test
	public void conjoinBoth() {
		final List<DatedSignal> left = signals(LocalDate.of(2011, 5, 21));
		final List<DatedSignal> right = signals(LocalDate.of(2011, 5, 21));

		final List<DatedSignal> signals = conjoin(left, right);

		verifySignals(signals(LocalDate.of(2011, 5, 21)), signals);
	}

	private List<DatedSignal> conjoin( final List<DatedSignal> left, final List<DatedSignal> right ) {
		return operator.conjoin(left, right);
	}

	private List<DatedSignal> signals( final LocalDate... dates ) {
		final List<DatedSignal> signals = new ArrayList<>(dates.length);

		for (final LocalDate date : dates) {
			signals.add(signal(date));
		}

		return signals;
	}

	private DatedSignal signal( final LocalDate date ) {
		return new DatedSignal(date, SignalType.BULLISH);
	}

	private void verifySignals( final List<DatedSignal> expected, final List<DatedSignal> actual ) {
		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());

		for (int i = 0; i < expected.size(); i++) {

			assertEquals(expected.get(i).date(), actual.get(i).date());
			assertEquals(expected.get(i).type(), actual.get(i).type());
		}
	}
}