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
package com.systematic.trading.signals.indicator.macd;

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
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Verifying the MovingAverageConvergenceDivergenceUptrendSignalCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class MovingAverageConvergenceDivergenceUptrendSignalCalculatorTest {

	@Mock
	private MovingAverageConvergenceDivergenceLines lines;

	@Mock
	private Predicate<LocalDate> signalRange;

	private SignalCalculator<MovingAverageConvergenceDivergenceLines> signalCalculator;
	private SortedMap<LocalDate, BigDecimal> macd;

	@Before
	public void setUp() {
		signalCalculator = new MovingAverageConvergenceDivergenceUptrendSignalCalculator();

		macd = new TreeMap<>();

		when(lines.getMacd()).thenReturn(macd);

		setUpDateRange(true);
	}

	@Test
	public void getTYpe() {
		assertEquals(SignalType.BULLISH, signalCalculator.getType());
	}

	@Test
	public void aboveOrigin() {
		final int numberSignalLinesDates = 4;
		setUpMacd(1, 1.1, 1.2, 1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(4, signals);
		verfiyDatedSignal(0, signals.get(0));
		verfiyDatedSignal(1, signals.get(1));
		verfiyDatedSignal(2, signals.get(2));
		verfiyDatedSignal(3, signals.get(3));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void aboveOriginOutsideDateRange() {
		setUpDateRange(false);
		final int numberSignalLinesDates = 3;
		setUpMacd(1, 1.1, 1.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void crossingOrigin() {
		final int numberSignalLinesDates = 4;
		setUpMacd(-1, -0.5, 0.25, 1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(2, signals);
		verfiyDatedSignal(2, signals.get(0));
		verfiyDatedSignal(3, signals.get(1));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void fallingThenBoucingFromOrigin() {
		final int numberSignalLinesDates = 4;
		setUpMacd(1, 0, 0, 1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(2, signals);
		verfiyDatedSignal(0, signals.get(0));
		verfiyDatedSignal(3, signals.get(1));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void belowOrigin() {
		final int numberSignalLinesDates = 4;
		setUpMacd(-1, -0.6, -0.10, -1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	private void verifySignalRangeTests( final int size ) {

		final InOrder order = inOrder(lines, signalRange);

		// Starting index @ 1, because there cannot be a signal on the first day :. excluded
		for (int i = 0; i < size; i++) {
			order.verify(signalRange).test(LocalDate.ofEpochDay(i));
		}

		verifyNoMoreInteractions(signalRange);
	}

	private void verifySignals( final int expectedSize, final List<DatedSignal> signals ) {
		assertNotNull(signals);
		assertEquals(expectedSize, signals.size());
	}

	private void setUpMacd( final double... values ) {
		for (int i = 0; i < values.length; i++)
			macd.put(LocalDate.ofEpochDay(i), BigDecimal.valueOf(values[i]));
	}

	private void verfiyDatedSignal( final int dateIndex, final DatedSignal signal ) {
		assertEquals(LocalDate.ofEpochDay(dateIndex), signal.getDate());
		assertEquals(SignalType.BULLISH, signal.getType());
	}

	private void setUpDateRange( final boolean insideRange ) {
		when(signalRange.test(any(LocalDate.class))).thenReturn(insideRange);
	}
}