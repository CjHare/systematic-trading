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
package com.systematic.trading.strategy.entry.size;

import java.math.BigDecimal;
import java.util.Optional;

import com.systematic.trading.simulation.cash.CashAccount;

/**
 * The largest possible entry size that is limited by a maximum and minimum bounds.
 * 
 * There will only be an entry when a size above the minimum can be achieved, 
 * with the cap being the maximum.
 * 
 * @author CJ Hare
 */
public class LargestPossibleEntryPosition implements EntrySize {

	/** The lower cap on the opening position size . */
	private final EntryPositionBounds minimum;

	/** The upper cap on the opening position size . */
	private final EntryPositionBounds maximum;

	/**
	 * @param minimum lower cap on the opening position size.
	 * @param maximum upper cap on the opening position size.
	 */
	public LargestPossibleEntryPosition( final EntryPositionBounds minimum,
	        final EntryPositionBounds maximum ) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	/**
	 * @return when the bounds are not met, then zero is returned, indicating no entry should be made.
	 */
	public BigDecimal entryPositionSize( final CashAccount cashAccount ) {
		final Optional<BigDecimal> minimumPosition = minimumPosition(cashAccount);

		if (!minimumPosition.isPresent()) {
			return BigDecimal.ZERO;
		}

		return minimumPosition.get().max(maximumPosition(cashAccount));
	}

	/**
	 * @return minimum position size, a value from the positive space of numbers.
	 */
	private Optional<BigDecimal> minimumPosition( final CashAccount cashAccount ) {
		final BigDecimal minimumPosition = minimum.bounds(cashAccount.balance());

		return isBalanceBelow(cashAccount, minimumPosition) ? Optional.empty()
		        : Optional.of(BigDecimal.ZERO.max(minimumPosition));
	}

	private BigDecimal maximumPosition( final CashAccount cashAccount ) {
		final BigDecimal availableCapital = cashAccount.balance();
		final BigDecimal maximumPosition = maximum.bounds(cashAccount.balance());

		return availableCapital.min(maximumPosition);
	}

	private boolean isBalanceBelow( final CashAccount cashAccount, final BigDecimal bar ) {
		return bar.compareTo(cashAccount.balance()) > 0;
	}
}