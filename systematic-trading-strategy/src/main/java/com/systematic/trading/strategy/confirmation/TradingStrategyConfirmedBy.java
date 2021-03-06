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

import java.time.LocalDate;

import com.systematic.trading.signal.model.DatedSignal;

/**
 * Anchor signal is confirmed by another signal, with a certain number of days.
 * 
 * @author CJ Hare
 */
public class TradingStrategyConfirmedBy implements Confirmation {

	/** Inclusive confirmation range. */
	private final int confirmationDayRange;

	/** Inclusive delay range. */
	private final int delayUntilConfirmationRange;

	/**
	 * @param confirmationDayRange
	 *            inclusive span of days for the confirmation signal to occur, after the delay.
	 * @param delayUntilConfirmationRange
	 *            inclusive number of days until confirmation signals are accepted.
	 */
	public TradingStrategyConfirmedBy( final int confirmationDayRange, final int delayUntilConfirmationRange ) {

		this.confirmationDayRange = confirmationDayRange;
		this.delayUntilConfirmationRange = delayUntilConfirmationRange;
	}

	@Override
	public boolean isConfirmedBy( final DatedSignal anchor, final DatedSignal confirmation ) {

		return isAfterConfirmationDelay(anchor, confirmation) && isBeforeConfirmationRangeEnd(anchor, confirmation);
	}

	@Override
	public int requiredTradingPrices() {

		return confirmationDayRange + delayUntilConfirmationRange;
	}

	/**
	 * Confirmation signal date is equal or after the earliest acceptable date.
	 */
	private boolean isAfterConfirmationDelay( final DatedSignal anchor, final DatedSignal confirmation ) {

		return !confirmation.date().isBefore(earliestConfirmationDate(anchor));
	}

	private boolean isBeforeConfirmationRangeEnd( final DatedSignal anchor, final DatedSignal confirmation ) {

		return !confirmation.date().isAfter(latestConfirmationDate(anchor));
	}

	private LocalDate earliestConfirmationDate( final DatedSignal anchor ) {

		return anchor.date().plusDays(delayUntilConfirmationRange);
	}

	private LocalDate latestConfirmationDate( final DatedSignal anchor ) {

		return anchor.date().plusDays((long) delayUntilConfirmationRange + confirmationDayRange);
	}
}
