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
package com.systematic.trading.signals.model.filter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;

/**
 * Signal generated when there are two indicators within a given time of each other, 
 * first signal followed by a confirmation signal within time frame.
 * <p/>
 * The signal date is on the confirmation signal date, rather then the anchor signal date.
 * 
 * @author CJ Hare
 */
public class ConfirmationIndicatorsSignalFilter implements SignalFilter {

	/** Signal that is used as the first, start of the acceptable date range.*/
	private final IndicatorSignalId anchor;

	/** Signal that is expected to occur after, follow the anchor signal.*/
	private final IndicatorSignalId confirmation;

	/** Inclusive maximum number of days after the anchor the confirmation of the follower must occur.*/
	private final int confirmationDayRange;

	/** The number of days after the anchor signal to begin range of acceptable days for the confirmation signal.*/
	private final int delayUntilConfirmationRange;

	/**
	 * @param anchor first signal that precedes the follower.
	 * @param follower after the anchor where a signal if generated only when within the inclusive days.
	 * @param delayUntilConfirmationRange number of days after the anchor signal to begin the acceptable range of days for the confirmation signal. 
	 * @param confirmationDayRange inclusive number of days from the start of the range the confirmation signal must be within.
	 */
	public ConfirmationIndicatorsSignalFilter( final IndicatorSignalId anchor, final IndicatorSignalId confirmation,
	        final int delayUntilConfirmationRange, final int confirmationDayRange ) {
		validate(anchor, "Expecting an anchor IndicatorSignalType");
		validate(confirmation, "Expecting an anchor IndicatorSignalType");
		validate(confirmationDayRange, "Expecting zero or a positive number of days for the confirmation signal range");
		validate(delayUntilConfirmationRange, "Expecting zero or positive number for the days");

		this.anchor = anchor;
		this.confirmation = confirmation;
		this.delayUntilConfirmationRange = delayUntilConfirmationRange;
		this.confirmationDayRange = confirmationDayRange;
	}

	@Override
	public SortedSet<BuySignal> apply( final Map<IndicatorSignalId, List<IndicatorSignal>> signals,
	        final Comparator<BuySignal> ordering, final LocalDate latestTradingDate ) {
		validateInput(signals);

		final SortedSet<BuySignal> passedSignals = new TreeSet<>(ordering);
		final List<IndicatorSignal> anchorSignals = signals.get(anchor);
		final List<IndicatorSignal> confirmationSignals = signals.get(confirmation);

		for (final IndicatorSignal anchorSignal : anchorSignals) {
			final LocalDate anchorDate = anchorSignal.getDate();
			final LocalDate confirmationSignalDate = hasConfirmationSignal(anchorDate, confirmationSignals);
			if (confirmationSignalDate != null) {
				passedSignals.add(new BuySignal(confirmationSignalDate));
			}
		}

		return passedSignals;
	}

	private LocalDate hasConfirmationSignal( final LocalDate anchorDate,
	        final List<IndicatorSignal> confirmationSignals ) {
		final LocalDate startConfirmationRange = anchorDate.plus(delayUntilConfirmationRange, ChronoUnit.DAYS);
		final LocalDate endConfirmationRange = startConfirmationRange.plus(confirmationDayRange, ChronoUnit.DAYS);

		for (final IndicatorSignal confirmationSignal : confirmationSignals) {
			final LocalDate confirmationSignalDate = confirmationSignal.getDate();
			if (isOnOrAfter(confirmationSignalDate, startConfirmationRange)
			        && isOnOrBefore(confirmationSignalDate, endConfirmationRange)) {
				return confirmationSignalDate;
			}
		}

		return null;
	}

	private boolean isOnOrAfter( final LocalDate first, final LocalDate second ) {
		return first.isEqual(second) || first.isAfter(second);
	}

	private boolean isOnOrBefore( final LocalDate first, final LocalDate second ) {
		return first.isEqual(second) || first.isBefore(second);
	}

	private void validateInput( final Map<IndicatorSignalId, List<IndicatorSignal>> signals ) {
		if (signals.get(anchor) == null || signals.get(confirmation) == null) {
			throw new IllegalArgumentException(
			        String.format("Expecting a non-null entries for types %s and %s", anchor, confirmation));
		}
	}

	private void validate( final Object toValidate, final String message ) {
		if (toValidate == null) {
			throw new IllegalArgumentException(message);
		}
	}

	private void validate( final int toValidate, final String message ) {
		if (toValidate < 0) {
			throw new IllegalArgumentException(message);
		}
	}
}