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
package com.systematic.trading.backtest;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.backtest.exception.InvalidSimulationDatesException;

/**
 * End date must be on or after start date.
 * 
 * @author CJ Hare
 */
public class BacktestSimulationDatesTest {

	/** The back test simulation dates instance being tested. */
	private BacktestSimulationDates dates;

	@Test
	public void endDateAfterStartDate() throws InvalidSimulationDatesException {

		final LocalDate startDate = LocalDate.of(2001, 6, 16);
		final LocalDate endDate = LocalDate.of(2001, 6, 17);

		createSimulationDates(startDate, endDate);

		verifySimulationStartDate(startDate);
		verifySimulationEndDate(endDate);
	}

	@Test
	public void endDateEqualsStartDate() throws InvalidSimulationDatesException {

		final LocalDate startDate = LocalDate.of(2001, 6, 16);
		final LocalDate endDate = LocalDate.of(2001, 6, 16);

		createSimulationDates(startDate, endDate);

		verifySimulationStartDate(startDate);
		verifySimulationEndDate(endDate);
	}

	@Test(expected = InvalidSimulationDatesException.class)
	public void endDateBeforeStartDate() throws InvalidSimulationDatesException {

		final LocalDate startDate = LocalDate.of(2001, 6, 16);
		final LocalDate endDate = LocalDate.of(2001, 6, 11);

		createSimulationDates(startDate, endDate);
	}

	private void createSimulationDates( final LocalDate startDate, final LocalDate endDate )
	        throws InvalidSimulationDatesException {

		dates = new BacktestSimulationDates(startDate, endDate);
	}

	private void verifySimulationStartDate( final LocalDate expected ) {

		assertEquals(expected, dates.startDate());
	}

	private void verifySimulationEndDate( final LocalDate expected ) {

		assertEquals(expected, dates.endDate());
	}
}