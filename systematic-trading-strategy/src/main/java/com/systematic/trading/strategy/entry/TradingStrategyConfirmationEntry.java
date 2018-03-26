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
package com.systematic.trading.strategy.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.strategy.confirmation.Confirmation;

/**
 * Trading strategy entry that uses two indicators, the first an anchor, the second as confirmation.
 * 
 * @author CJ Hare
 */
public class TradingStrategyConfirmationEntry implements Entry {

	private final Entry anchorIndicator;
	private final Confirmation confirmation;
	private final Entry confirmationIndicator;

	public TradingStrategyConfirmationEntry(
	        final Entry anchorIndicator,
	        final Confirmation confirmation,
	        final Entry confirmationIndicator ) {

		this.anchorIndicator = anchorIndicator;
		this.confirmation = confirmation;
		this.confirmationIndicator = confirmationIndicator;
	}

	@Override
	public List<DatedSignal> analyse( final TradingDayPrices[] data ) {

		final List<DatedSignal> anchorSignals = anchorIndicator.analyse(data);
		final List<DatedSignal> signals = new ArrayList<>(anchorSignals.size());

		if (hasSignals(anchorSignals)) {
			final List<DatedSignal> confirmationSignals = confirmationIndicator.analyse(data);

			for (final DatedSignal anchorSignal : anchorSignals) {
				final Optional<
				        DatedSignal> latestConfirmation = latestConformationSignal(anchorSignal, confirmationSignals);

				if (latestConfirmation.isPresent()) {
					signals.add(latestConfirmation.get());
				}
			}
		}

		return signals;
	}

	private boolean hasSignals( final List<DatedSignal> signals ) {

		return !signals.isEmpty();
	}

	private Optional<DatedSignal> latestConformationSignal(
	        final DatedSignal anchorSignal,
	        final List<DatedSignal> confirmationSignals ) {

		DatedSignal match = null;

		for (final DatedSignal confirmationSignal : confirmationSignals) {
			if (confirmation.isConfirmedBy(anchorSignal, confirmationSignal)) {
				match = confirmationSignal;
			}
		}

		return match == null ? Optional.empty() : Optional.of(match);
	}

	@Override
	public int requiredTradingPrices() {

		return Math.max(anchorIndicator.requiredTradingPrices(), confirmationIndicator.requiredTradingPrices())
		        + confirmation.requiredTradingPrices();
	}
}
