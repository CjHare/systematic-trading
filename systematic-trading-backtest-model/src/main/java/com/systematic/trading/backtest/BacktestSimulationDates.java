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
package com.systematic.trading.backtest;

import java.time.LocalDate;

import com.systematic.trading.backtest.exception.InvalidSimulationDatesException;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;

/**
 * Simulation dates and warm up period.
 * 
 * @author CJ Hare
 */
public class BacktestSimulationDates {

	private final LocalDate startDateInclusive;
	private final LocalDate endDateExclusive;

	/**
	 * @param startDateInclusive
	 *            inclusive date that must be before or on the end date.
	 * @param endDateExclusive
	 *            exclusive date that must be on or after the start date.
	 */
	public BacktestSimulationDates( final BacktestStartDate startDateInclusive, final BacktestEndDate endDateExclusive )
	        throws InvalidSimulationDatesException {

		validateDates(startDateInclusive.date(), endDateExclusive.date());
		this.startDateInclusive = startDateInclusive.date();
		this.endDateExclusive = endDateExclusive.date();
	}

	/**
	 * Inclusive state date of the simulation.
	 */
	public LocalDate startDateInclusive() {

		return startDateInclusive;
	}

	/**
	 * Exclusive end date for the simulation.
	 */
	public LocalDate endDateExclusive() {

		return endDateExclusive;
	}

	private void validateDates( final LocalDate startDate, final LocalDate endDate )
	        throws InvalidSimulationDatesException {

		if (startDate.isAfter(endDate)) {
			throw new InvalidSimulationDatesException(String.format("%s %s", startDate, endDate));
		}
	}
}
