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
package com.systematic.trading.simulation.brokerage;

import java.time.LocalDate;
import java.time.Month;

/**
 * Keeps a count on a monthly rolling basis.
 * <p/>
 * The counter resets when a trade is received in a different month to the last addition.
 * 
 * @author CJ Hare
 */
public class MonthlyRollingCounter {

	/** Year of the last addition to the counter. */
	private int yearOfLastAddition;

	/** Month of the last addition to the counter. */
	private Month monthOfLastAddition;

	/** Counter for number of additions. */
	private int additionsThisMonth = 0;

	/**
	 * Increments the counter for the given month.
	 * 
	 * @param date date of the counter increment.
	 * @return count for the month of the given date.
	 */
	public int add( final LocalDate date ) {

		if (isSameMonthAsLastAddition(date)) {
			additionsThisMonth++;
		} else {
			resetCounter(date);
		}

		return additionsThisMonth;
	}

	/**
	 * The number of transactions.
	 * 
	 * @return number of transaction in the given month.
	 */
	public int get( final LocalDate date ) {

		if (isSameMonthAsLastAddition(date)) {
			return additionsThisMonth;
		}

		return 0;
	}

	private boolean isSameMonthAsLastAddition( final LocalDate tradeDate ) {

		return yearOfLastAddition == tradeDate.getYear() && monthOfLastAddition == tradeDate.getMonth();
	}

	private void resetCounter( final LocalDate tradeDate ) {

		yearOfLastAddition = tradeDate.getYear();
		monthOfLastAddition = tradeDate.getMonth();
		additionsThisMonth = 1;
	}
}
