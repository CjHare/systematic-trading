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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.maths.DatedSignal;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signals.indicator.SignalCalculator;

/**
 * Verifying the signal generation criteria are being satisfied.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class MovingAverageConvergenceDivergenceBullishSignalCalculatorTest {

	@Mock
	private MovingAverageConvergenceDivergenceLines lines;

	@Mock
	private Predicate<LocalDate> signalRange;

	private SignalCalculator<MovingAverageConvergenceDivergenceLines> signalCalculator;
	private List<BigDecimal> macd;
	private List<BigDecimal> signaLine;
	private List<LocalDate> signalLineDates;

	@Before
	public void setUp() {
		signalCalculator = new MovingAverageConvergenceDivergenceBullishSignalCalculator();

		macd = new ArrayList<>();
		signaLine = new ArrayList<>();
		signalLineDates = new ArrayList<>();

		when(lines.getMacdValues()).thenReturn(macd);
		when(lines.getSignaLine()).thenReturn(signaLine);
		when(lines.getSignalLineDates()).thenReturn(signalLineDates);

		setUpDateRange(true);
	}

	@Test
	public void getType() {
		assertEquals(SignalType.BULLISH, signalCalculator.getType());
	}

	@Test
	public void calculateSignalsNoneFound() {

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(0);
	}

	@Test
	public void calculateSignalsOnceCrossnigSignalLine() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(0, 0.1, 0.2, 0.3);
		setUpMacd(-1, 0.2, 1, 1.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(1, signals);
		verfiyDatedSignal(1, signals.get(0));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void calculateSignalsOnceCrossnigSignalLineOutsideDateRange() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(0, 0.1, 0.2, 0.3);
		setUpMacd(-1, 0.2, 1, 1.2);
		setUpDateRange(false);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Extended crossing is when the MACD and Signal Line are equal for more then one time slice.
	 */
	public void calculateSignalsOnceExtendedCrossnigSignalLine() {
		final int numberSignalLinesDates = 5;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(0, 0.1, 0.1, 0.2, 0.3);
		setUpMacd(-1, 0.1, 0.1, 1, 1.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(2, signals);
		verfiyDatedSignal(1, signals.get(0));
		verfiyDatedSignal(3, signals.get(1));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Touch off is when the MACD moves down onto the Signal Line for one time slice, then move above.
	 */
	public void calculateSignalsOnceTouchOffCrossnigSignalLine() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(0, 0.1, 0.2, 0.3);
		setUpMacd(1, 0.1, 1, 1.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(1, signals);
		verfiyDatedSignal(2, signals.get(0));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Both MACD and Signal Lines are going up, but no crossing.
	 */
	public void calculateSignalsPositiveGradientsMacdBelowNoCrossnigSignalLine() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(3, 3.1, 3.2, 3.3);
		setUpMacd(1, 1.1, 1.2, 1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Both MACD and Signal Lines are going up, but no crossing.
	 */
	public void calculateSignalsPositiveGradientsMacdAboveoCrossnigSignalLine() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(0, 0.1, 0.2, 0.3);
		setUpMacd(1, 1.1, 1.2, 1.3);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void calculateSignalsCrossnigOrigin() {
		final int numberSignalLinesDates = 4;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(5, 5, 5, 5);
		setUpMacd(-1, 0.1, 1, 0);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(1, signals);
		verfiyDatedSignal(1, signals.get(0));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Extended crossing has the MACD sitting on the origin for a day.
	 */
	public void calculateSignalsExtendedCrossnigOrigin() {
		final int numberSignalLinesDates = 5;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(5, 5, 5, 5, 5);
		setUpMacd(-0.2, 0, 0, 0.1, 0.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(2, signals);
		verfiyDatedSignal(1, signals.get(0));
		verfiyDatedSignal(3, signals.get(1));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	/**
	 * Extended crossing has the MACD sitting on the origin for a day.
	 */
	public void calculateSignalsTouchDownCrossnigOrigin() {
		final int numberSignalLinesDates = 5;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(5, 5, 5, 5, 5);
		setUpMacd(0.2, 0, 0, 0.1, 0.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(1, signals);
		verfiyDatedSignal(3, signals.get(0));
		verifySignalRangeTests(numberSignalLinesDates);
	}

	@Test
	public void calculateSignalsMacdUpwardTrendBelowOrigin() {
		final int numberSignalLinesDates = 5;
		setUpDates(numberSignalLinesDates);
		setUpSignalLine(5, 5, 5, 5, 5);
		setUpMacd(-2, -1.4, -1.2, -0.8, -0.2);

		final List<DatedSignal> signals = signalCalculator.calculateSignals(lines, signalRange);

		verifySignals(0, signals);
		verifySignalRangeTests(numberSignalLinesDates);
	}

	private void setUpDateRange( final boolean insideRange ) {
		when(signalRange.test(any(LocalDate.class))).thenReturn(insideRange);
	}

	private void verifySignalRangeTests( final int size ) {

		final InOrder order = inOrder(lines, signalRange);
		order.verify(lines).getSignalLineDates();

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

	private void verfiyDatedSignal( final int dateIndex, final DatedSignal signal ) {
		assertEquals(LocalDate.ofEpochDay(dateIndex), signal.getDate());
		assertEquals(SignalType.BULLISH, signal.getType());
	}

	private void setUpDates( final int size ) {
		for (int i = 0; i < size; i++) {
			signalLineDates.add(LocalDate.ofEpochDay(i));
		}
	}

	private void setUpSignalLine( final double... values ) {
		for (final double value : values) {
			signaLine.add(BigDecimal.valueOf(value));
		}
	}

	private void setUpMacd( final double... values ) {
		for (final double value : values) {
			macd.add(BigDecimal.valueOf(value));
		}
	}
}