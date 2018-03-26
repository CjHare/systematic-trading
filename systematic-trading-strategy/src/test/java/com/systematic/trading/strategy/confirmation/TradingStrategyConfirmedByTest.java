/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.strategy.confirmation;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Verifying the trading strategy confirm by, actually confirms an anchor with a confirmation
 * signal.
 * 
 * @author CJ Hare
 */
public class TradingStrategyConfirmedByTest {

	/** ConfirmBy being tested. */
	private TradingStrategyConfirmedBy confirmBy;

	@Test
	public void requiredTradingPrices() {

		setUpConfirmBy(1, 5);

		final int pricePoints = priceDataPoints();

		assertEquals(6, pricePoints);
	}

	@Test
	public void requiredTradingPricesSwitch() {

		setUpConfirmBy(2, 3);

		final int pricePoints = priceDataPoints();

		assertEquals(5, pricePoints);
	}

	@Test
	public void confirmationTooEarly() {

		final DatedSignal anchor = signal(LocalDate.of(2015, 2, 18));
		final DatedSignal confirmation = signal(LocalDate.of(2015, 2, 19));
		setUpConfirmBy(3, 2);

		final boolean confirmed = confirmBy.isConfirmedBy(anchor, confirmation);

		verifyNoConfirmation(confirmed);
	}

	@Test
	public void confirmationTooLate() {

		final DatedSignal anchor = signal(LocalDate.of(2015, 2, 18));
		final DatedSignal confirmation = signal(LocalDate.of(2015, 2, 24));
		setUpConfirmBy(3, 2);

		final boolean confirmed = confirmBy.isConfirmedBy(anchor, confirmation);

		verifyNoConfirmation(confirmed);
	}

	@Test
	public void earliestConfirmationDate() {

		final DatedSignal anchor = signal(LocalDate.of(2015, 2, 18));
		final DatedSignal confirmation = signal(LocalDate.of(2015, 2, 20));
		setUpConfirmBy(3, 2);

		final boolean confirmed = confirmBy.isConfirmedBy(anchor, confirmation);

		verifyConfirmation(confirmed);
	}

	@Test
	public void latestConfirmationDate() {

		final DatedSignal anchor = signal(LocalDate.of(2015, 2, 18));
		final DatedSignal confirmation = signal(LocalDate.of(2015, 2, 23));
		setUpConfirmBy(3, 2);

		final boolean confirmed = confirmBy.isConfirmedBy(anchor, confirmation);

		verifyConfirmation(confirmed);
	}

	private DatedSignal signal( final LocalDate date ) {

		return new DatedSignal(date, SignalType.BULLISH);
	}

	private int priceDataPoints() {

		return confirmBy.requiredTradingPrices();
	}

	private void setUpConfirmBy( final int confirmationDayRange, final int delayUntilConfirmationRange ) {

		confirmBy = new TradingStrategyConfirmedBy(confirmationDayRange, delayUntilConfirmationRange);
	}

	private void verifyNoConfirmation( final boolean actual ) {

		assertEquals(false, actual);
	}

	private void verifyConfirmation( final boolean actual ) {

		assertEquals(true, actual);
	}
}
