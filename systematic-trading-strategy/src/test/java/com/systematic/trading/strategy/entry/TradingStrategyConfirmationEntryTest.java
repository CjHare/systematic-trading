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
package com.systematic.trading.strategy.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.strategy.confirmation.Confirmation;

/**
 * Verifies behaviour of the entry that uses two indicators, the first an anchor, the second as
 * confirmation.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyConfirmationEntryTest {

	@Mock
	private Entry anchorIndicator;

	@Mock
	private Confirmation confirmation;

	@Mock
	private Entry confirmationIndicator;

	private List<DatedSignal> anchorAnalysis;
	private List<DatedSignal> confirmationAnalysis;

	/** Entry instance being tested. */
	private TradingStrategyConfirmationEntry entry;

	@Before
	public void setUp() {

		entry = new TradingStrategyConfirmationEntry(anchorIndicator, confirmation, confirmationIndicator);
		anchorAnalysis = new ArrayList<DatedSignal>();
		confirmationAnalysis = new ArrayList<DatedSignal>();
	}

	@Test
	public void tradingDataPointsAnchor() {

		setUpTradingDataPoints(5, 1, 4);

		final int requiredPriceDataPoints = entry.requiredTradingPrices();

		verifyPriceDataPoints(6, requiredPriceDataPoints);
		verifyTradingDataPointsDelegation();
	}

	@Test
	public void tradingDataPointsConfirmation() {

		setUpTradingDataPoints(3, 1, 6);

		final int requiredPriceDataPoints = entry.requiredTradingPrices();

		verifyPriceDataPoints(7, requiredPriceDataPoints);
		verifyTradingDataPointsDelegation();
	}

	@Test
	public void analyseNoSignal() {

		final TradingDayPrices[] data = new TradingDayPrices[5];
		setUpNoSignals();

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysisEmpty(analysis);
		verifyAnchorAnalysisDelegation(data);
	}

	@Test
	public void analyseAnchorSignal() {

		final TradingDayPrices[] data = new TradingDayPrices[5];
		setUpAnchorSignals(bullishSignal());

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysisEmpty(analysis);
		verifyAnchorAnalysisDelegation(data);
	}

	@Test
	public void analyseUnconfirmedSignals() {

		final TradingDayPrices[] data = new TradingDayPrices[5];
		setUpAnchorSignals(bullishSignal());
		setUpConfirmationSignals(bullishSignal());

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysisEmpty(analysis);
		verifyAnchorAnalysisDelegation(data);
		verifyConfirmationAnalysisDelegation(data);
	}

	@Test
	public void analyseConfirmedSignals() {

		final TradingDayPrices[] data = new TradingDayPrices[5];
		final DatedSignal signal = bullishSignal();
		setUpAnchorSignals(signal);
		setUpConfirmationSignals(signal);
		setUpConfirmedSignals();

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysis(analysis, signal);
		verifyAnchorAnalysisDelegation(data);
		verifyConfirmationAnalysisDelegation(data);
	}

	/**
	 * When two confirmation signals match an anchor signal the latter is chosen.
	 */
	@Test
	public void analyseDoubleConfirmation() {

		final TradingDayPrices[] data = new TradingDayPrices[5];
		final DatedSignal firstSignal = bullishSignalYesterday();
		final DatedSignal secondSignal = bullishSignal();
		setUpAnchorSignals(firstSignal);
		setUpConfirmationSignals(firstSignal, secondSignal);
		setUpConfirmedSignals();

		final List<DatedSignal> analysis = analyse(data);

		verifyAnalysis(analysis, secondSignal);
		verifyAnchorAnalysisDelegation(data);
		verifyConfirmationAnalysisDelegation(data);
	}

	private DatedSignal bullishSignal() {

		return new DatedSignal(LocalDate.now(), SignalType.BULLISH);
	}

	private DatedSignal bullishSignalYesterday() {

		return new DatedSignal(LocalDate.now().minusDays(1), SignalType.BULLISH);
	}

	private void setUpConfirmedSignals() {

		when(confirmation.isConfirmedBy(any(DatedSignal.class), any(DatedSignal.class))).thenReturn(true);
	}

	private void setUpAnchorSignals( final DatedSignal anchorSignal ) {

		when(anchorIndicator.analyse(any(TradingDayPrices[].class))).thenReturn(anchorAnalysis);
		anchorAnalysis.add(anchorSignal);
	}

	private void setUpConfirmationSignals( final DatedSignal... anchorSignal ) {

		when(confirmationIndicator.analyse(any(TradingDayPrices[].class))).thenReturn(confirmationAnalysis);

		for (final DatedSignal signal : anchorSignal) {
			confirmationAnalysis.add(signal);
		}
	}

	private void setUpNoSignals() {

		when(anchorIndicator.analyse(any(TradingDayPrices[].class))).thenReturn(anchorAnalysis);
	}

	private void setUpTradingDataPoints( final int anchorIndicatorPoints, final int confirmationPoints,
	        final int confirmationIndicatorPoints ) {

		when(anchorIndicator.requiredTradingPrices()).thenReturn(anchorIndicatorPoints);
		when(confirmation.requiredTradingPrices()).thenReturn(confirmationPoints);
		when(confirmationIndicator.requiredTradingPrices()).thenReturn(confirmationIndicatorPoints);
	}

	private List<DatedSignal> analyse( final TradingDayPrices[] data ) {

		return entry.analyse(data);
	}

	private void verifyPriceDataPoints( final int expected, final int actual ) {

		assertEquals(expected, actual);
	}

	private void verifyTradingDataPointsDelegation() {

		verify(anchorIndicator).requiredTradingPrices();
		verify(confirmation).requiredTradingPrices();
		verify(confirmationIndicator).requiredTradingPrices();
	}

	private void verifyAnalysisEmpty( final List<DatedSignal> analysis ) {

		assertNotNull(analysis);
		assertEquals(true, analysis.isEmpty());
	}

	private void verifyAnalysis( final List<DatedSignal> analysis, final DatedSignal... expected ) {

		assertNotNull(analysis);
		assertEquals(false, analysis.isEmpty());
		assertEquals(expected.length, analysis.size());

		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], analysis.get(i));
		}
	}

	private void verifyConfirmationAnalysisDelegation( final TradingDayPrices[] data ) {

		verify(confirmationIndicator).analyse(data);
	}

	private void verifyAnchorAnalysisDelegation( final TradingDayPrices[] data ) {

		verify(anchorIndicator).analyse(data);
	}
}