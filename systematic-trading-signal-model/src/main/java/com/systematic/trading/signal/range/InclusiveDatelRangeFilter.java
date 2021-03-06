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
package com.systematic.trading.signal.range;

import java.time.LocalDate;

/**
 * Filter used for determining whether a candidate date is within a date given range.
 * 
 * @author CJ Hare
 */
public class InclusiveDatelRangeFilter {

	/**
	 * Determines whether the candidate date falls within the given date range.
	 * 
	 * @param earliestInclusiveDate
	 *            inclusive date for the earliest signal, not <code>null</code>.
	 * @param latestInclusiveDate
	 *            inclusive date for the latest signal, not <code>null</code>.
	 * @param candidate
	 *            date being evaluated, not <code>null</code>.
	 * @return <code>true</code> when date with within the range, <code>false</code> otherwise.
	 */
	public boolean isWithinSignalRange(
	        final LocalDate earliestInclusiveDate,
	        final LocalDate latestInclusiveDate,
	        final LocalDate candidate ) {

		return isWithinEarliestSignalRange(earliestInclusiveDate, candidate)
		        && isWithinLatestSignalRange(latestInclusiveDate, candidate);
	}

	private boolean isWithinEarliestSignalRange( final LocalDate earliestInclusiveDate, final LocalDate candidate ) {

		return !candidate.isBefore(earliestInclusiveDate);
	}

	private boolean isWithinLatestSignalRange( final LocalDate latestInclusiveDate, final LocalDate candidate ) {

		return !candidate.isAfter(latestInclusiveDate);
	}
}
