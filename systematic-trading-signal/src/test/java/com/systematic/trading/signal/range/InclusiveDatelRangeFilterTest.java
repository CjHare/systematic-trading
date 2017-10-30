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
package com.systematic.trading.signal.range;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.signal.range.InclusiveDatelRangeFilter;

/**
 * Verifying InclusiveDatelRangeFilter.
 * 
 * @author CJ Hare
 */
public class InclusiveDatelRangeFilterTest {

	/** Filter instance being tested. */
	private InclusiveDatelRangeFilter filter;

	@Before
	public void setUp() {
		filter = new InclusiveDatelRangeFilter();
	}

	@Test
	public void tooEarly() {
		final LocalDate earliestInclusiveDate = LocalDate.ofEpochDay(6);
		final LocalDate latestInclusiveDate = LocalDate.ofEpochDay(100);
		final LocalDate candidate = LocalDate.ofEpochDay(5);

		final boolean result = isWithinSignalRange(earliestInclusiveDate, latestInclusiveDate, candidate);

		assertFalse("Date was a day too early, should have failed", result);
	}

	@Test
	public void tooLate() {
		final LocalDate earliestInclusiveDate = LocalDate.ofEpochDay(6);
		final LocalDate latestInclusiveDate = LocalDate.ofEpochDay(100);
		final LocalDate candidate = LocalDate.ofEpochDay(101);

		final boolean result = isWithinSignalRange(earliestInclusiveDate, latestInclusiveDate, candidate);

		assertFalse("Date was a day too late, should have failed", result);
	}

	@Test
	public void earliestAcceptable() {
		final LocalDate earliestInclusiveDate = LocalDate.ofEpochDay(6);
		final LocalDate latestInclusiveDate = LocalDate.ofEpochDay(100);
		final LocalDate candidate = earliestInclusiveDate;

		final boolean result = isWithinSignalRange(earliestInclusiveDate, latestInclusiveDate, candidate);

		assertTrue("Date was the earliest acceptable, should have passed", result);
	}

	@Test
	public void latestAcceptable() {
		final LocalDate earliestInclusiveDate = LocalDate.ofEpochDay(6);
		final LocalDate latestInclusiveDate = LocalDate.ofEpochDay(100);
		final LocalDate candidate = latestInclusiveDate;

		final boolean result = isWithinSignalRange(earliestInclusiveDate, latestInclusiveDate, candidate);

		assertTrue("Date was the latest acceptable, should have passed", result);
	}

	private boolean isWithinSignalRange( final LocalDate earliestInclusiveDate, final LocalDate latestInclusiveDate,
	        final LocalDate candidate ) {
		return filter.isWithinSignalRange(earliestInclusiveDate, latestInclusiveDate, candidate);
	}
}