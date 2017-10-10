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
package com.systematic.trading.signals.indicator.rsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexLine;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Verify the behaviour of the RelativeStrengthIndexBearishSignalGenerator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexBullishSignalGeneratorTest {

	private static final double OVER_SOLD = 0.3;

	@Mock
	private Predicate<LocalDate> signalRange;

	@Mock
	private RelativeStrengthIndexLine rsi;

	private RelativeStrengthIndexBullishSignalGenerator bullishRsi;

	@Before
	public void setUp() {
		setUpCalculator();
		setUpDateRange(true);
	}

	@Test
	public void getType() {
		assertEquals(SignalType.BULLISH, bullishRsi.getType());
	}

	@Test
	public void neverUndersold() {
		setUpRsi(0.5, 0.4, 0.6, 0.5);
		final List<DatedSignal> signals = rsi();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void alwaysUndersold() {
		setUpRsi(0.1, 0.2, 0.25, 0.1);
		final List<DatedSignal> signals = rsi();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void outsideDateRange() {
		setUpRsi(0.5, 0.2, 0.1, 0.15);
		setUpDateRange(false);

		final List<DatedSignal> signals = rsi();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void undersoldCrossover() {
		setUpRsi(0.5, 0.4, 0.29, 0.5);

		final List<DatedSignal> signals = rsi();

		verifySignals(1, signals);
		verfiyDatedSignal(3, signals.get(0));
		verifySignalRangeTests(4);
	}

	@Test
	public void signalOnUndersold() {
		setUpRsi(0.3, 0.3, 0.3, 0.3);

		final List<DatedSignal> signals = rsi();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void touchUndersold() {
		setUpRsi(0.1, 0.3, 0.2, 0.15);

		final List<DatedSignal> signals = rsi();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void twiceUndersold() {
		setUpRsi(0.9, 0.2, 0.5, 0.3, 0.4, 0.3);

		final List<DatedSignal> signals = rsi();

		verifySignals(2, signals);
		verfiyDatedSignal(2, signals.get(0));
		verfiyDatedSignal(4, signals.get(1));
		verifySignalRangeTests(6);
	}

	@Test
	public void fallBelowThenOnUndersoldCrossover() {
		setUpRsi(0.4, 0.3, 0.3, 0.2, 0.5);

		final List<DatedSignal> signals = rsi();

		verifySignals(1, signals);
		verfiyDatedSignal(4, signals.get(0));
		verifySignalRangeTests(5);
	}

	private List<DatedSignal> rsi() {
		return bullishRsi.generate(rsi, signalRange);
	}

	private void setUpRsi( final double... values ) {
		SortedMap<LocalDate, BigDecimal> line = new TreeMap<>();

		for (int i = 0; i < values.length; i++) {
			line.put(LocalDate.ofEpochDay(i), BigDecimal.valueOf(values[i]));
		}

		when(rsi.getRsi()).thenReturn(line);
	}

	private void setUpCalculator() {
		bullishRsi = new RelativeStrengthIndexBullishSignalGenerator(BigDecimal.valueOf(OVER_SOLD));
	}

	private void verifySignals( final int expectedSize, final List<DatedSignal> signals ) {
		assertNotNull(signals);
		assertEquals(expectedSize, signals.size());
	}

	private void verfiyDatedSignal( final int dateIndex, final DatedSignal signal ) {
		assertEquals(LocalDate.ofEpochDay(dateIndex), signal.getDate());
		assertEquals(SignalType.BULLISH, signal.getType());
	}

	private void verifySignalRangeTests( final int size ) {
		final InOrder order = inOrder(signalRange);

		// Starting index @ 1, because there cannot be a signal on the first day :. excluded
		for (int i = 1; i < size; i++) {
			order.verify(signalRange).test(LocalDate.ofEpochDay(i));
		}

		verifyNoMoreInteractions(signalRange);
	}

	private void setUpDateRange( final boolean insideRange ) {
		when(signalRange.test(any(LocalDate.class))).thenReturn(insideRange);
	}
}