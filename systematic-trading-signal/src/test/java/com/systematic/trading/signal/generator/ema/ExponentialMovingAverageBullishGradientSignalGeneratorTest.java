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
package com.systematic.trading.signal.generator.ema;

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
import com.systematic.trading.maths.indicator.ema.ExponentialMovingAverageLine;
import com.systematic.trading.signal.generator.SignalGenerator;
import com.systematic.trading.signal.generator.ema.ExponentialMovingAverageBullishGradientSignalGenerator;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Verifying the behaviour of the ExponentialMovingAverageBullishGradientSignalGenerator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ExponentialMovingAverageBullishGradientSignalGeneratorTest {

	@Mock
	private ExponentialMovingAverageLine line;

	@Mock
	private Predicate<LocalDate> signalRange;

	/** Data returned from the line. */
	private SortedMap<LocalDate, BigDecimal> ema;

	/** Signal generator instance being tested. */
	private SignalGenerator<ExponentialMovingAverageLine> signalGenerators;

	@Before
	public void setUp() {
		signalGenerators = new ExponentialMovingAverageBullishGradientSignalGenerator();

		// Default data set of no results
		ema = new TreeMap<>();
		when(line.getEma()).thenReturn(ema);

		setUpDateRange(true);
	}

	@Test
	public void getTYpe() {
		assertEquals(SignalType.BULLISH, signalGenerators.getType());
	}

	@Test
	public void outOfDateRange() {
		setUpDateRange(false);
		setUpSma(1, 1.1, 1.2);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(3);
	}

	@Test
	public void tooFewValues() {
		setUpSma(0.5);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(0);
	}

	@Test
	public void noValues() {

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(0);
	}

	@Test
	public void flatline() {
		setUpSma(0.5, 0.5, 0.5, 0.5);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void downardGradient() {
		setUpSma(0.5, 0.4, 0.3, 0.2);

		final List<DatedSignal> signals = generate();

		verifySignals(0, signals);
		verifySignalRangeTests(4);
	}

	@Test
	public void upwardGradient() {
		setUpSma(0.5, 0.6, 0.7, 0.8);

		final List<DatedSignal> signals = generate();

		verifySignals(3, signals);
		verfiyDatedSignal(1, signals.get(0));
		verfiyDatedSignal(2, signals.get(1));
		verfiyDatedSignal(3, signals.get(2));
		verifySignalRangeTests(4);
	}

	@Test
	public void upwardGradientThenFlat() {
		setUpSma(0.5, 0.6, 0.6);

		final List<DatedSignal> signals = generate();

		verifySignals(1, signals);
		verfiyDatedSignal(1, signals.get(0));
		verifySignalRangeTests(3);
	}

	@Test
	public void downwardThenUpwardGradientThenFlat() {
		setUpSma(0.55, 0.5, 0.4, 0.4, 0.5);

		final List<DatedSignal> signals = generate();

		verifySignals(1, signals);
		verfiyDatedSignal(4, signals.get(0));
		verifySignalRangeTests(5);
	}

	private List<DatedSignal> generate() {
		return signalGenerators.generate(line, signalRange);
	}

	private void verifySignalRangeTests( final int size ) {

		if (size == 0) {
			verifyNoMoreInteractions(signalRange);
			return;
		}

		final InOrder order = inOrder(line, signalRange);

		// Starting index @ 1, because there cannot be a signal on the first day :. excluded
		for (int i = 1; i < size; i++) {
			order.verify(signalRange).test(LocalDate.ofEpochDay(i));
		}

		verifyNoMoreInteractions(signalRange);
	}

	private void verifySignals( final int expectedSize, final List<DatedSignal> signals ) {
		assertNotNull(signals);
		assertEquals(expectedSize, signals.size());
	}

	private void setUpSma( final double... values ) {
		for (int i = 0; i < values.length; i++)
			ema.put(LocalDate.ofEpochDay(i), BigDecimal.valueOf(values[i]));
	}

	private void verfiyDatedSignal( final int dateIndex, final DatedSignal signal ) {
		assertEquals(LocalDate.ofEpochDay(dateIndex), signal.date());
		assertEquals(SignalType.BULLISH, signal.type());
	}

	private void setUpDateRange( final boolean insideRange ) {
		when(signalRange.test(any(LocalDate.class))).thenReturn(insideRange);
	}
}