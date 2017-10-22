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
package com.systematic.trading.signals.generator.rsi;

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
import com.systematic.trading.signals.generator.rsi.RelativeStrengthIndexBearishSignalGenerator;
import com.systematic.trading.signals.model.DatedSignal;

/**
 * Verify the behaviour of the RelativeStrengthIndexBearishSignalGenerator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RelativeStrengthIndexBearishSignalGeneratorTest {

	/** Standard over brought of 70.*/
	private static final double OVER_BROUGHT = 0.7;

	@Mock
	private Predicate<LocalDate> signalRange;

	@Mock
	private RelativeStrengthIndexLine rsi;

	/** Generator instance being tested. */
	private RelativeStrengthIndexBearishSignalGenerator bearishRsi;

	@Before
	public void setUp() {
		setUpCalculator();
		setUpDateRange(true);
	}

	@Test
	public void getType() {
		assertEquals(SignalType.BEARISH, bearishRsi.getType());
	}

	@Test
	public void neverOversold() {
		setUpRsi(0.5, 0.6, 0.6, 0.5);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void alwaysOversold() {
		setUpRsi(0.7, 0.8, 0.9, 0.75);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void outsideDateRange() {
		setUpRsi(0.5, 0.7, 0.8, 0.75);
		setUpDateRange(false);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void oversoldCrossover() {
		setUpRsi(1, 0.69, 0.8, 0.7);

		final List<DatedSignal> signals = generate();

		verifySignals(1, signals);
		verfiyDatedSignal(1, signals.get(0));
		verifySignalRangeTests(4);
	}

	@Test
	/**
	 * No signal unless the RSI line crosses below the over sold threshold.
	 */
	public void touchOversold() {
		setUpRsi(1, 0.7, 0.8, 0.75);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void twiceCrossoverOversold() {
		setUpRsi(1, 0.4, 0.9, 0.6);

		final List<DatedSignal> signals = generate();

		verifySignals(2, signals);
		verfiyDatedSignal(1, signals.get(0));
		verfiyDatedSignal(3, signals.get(1));
		verifySignalRangeTests(4);
	}

	@Test
	public void onOversold() {
		setUpRsi(0.7, 0.7, 0.7, 0.7, 0.7);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(5);
	}

	@Test
	public void onOversoldThenCrossover() {
		setUpRsi(0.7, 0.7, 0.5, 0.8, 0.45);

		final List<DatedSignal> signals = generate();

		verifySignals(2, signals);
		verfiyDatedSignal(2, signals.get(0));
		verfiyDatedSignal(4, signals.get(1));
		verifySignalRangeTests(5);
	}

	private List<DatedSignal> generate() {
		return bearishRsi.generate(rsi, signalRange);
	}

	private void setUpRsi( final double... values ) {
		SortedMap<LocalDate, BigDecimal> line = new TreeMap<>();

		for (int i = 0; i < values.length; i++) {
			line.put(LocalDate.ofEpochDay(i), BigDecimal.valueOf(values[i]));
		}

		when(rsi.getRsi()).thenReturn(line);
	}

	private void setUpCalculator() {
		bearishRsi = new RelativeStrengthIndexBearishSignalGenerator(BigDecimal.valueOf(OVER_BROUGHT));
	}

	private void verifySignals( final int expectedSize, final List<DatedSignal> signals ) {
		assertNotNull(signals);
		assertEquals(expectedSize, signals.size());
	}

	private void verfiyDatedSignal( final int dateIndex, final DatedSignal signal ) {
		assertEquals(LocalDate.ofEpochDay(dateIndex), signal.getDate());
		assertEquals(SignalType.BEARISH, signal.getType());
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