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
package com.systematic.trading.simulation.analysis.roi.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concrete implementation of the ROI event.
 * 
 * @author CJ Hare
 */
public class ReturnOnInvestmentEventImpl implements ReturnOnInvestmentEvent {

	private final BigDecimal percentageChange;
	private final LocalDate exclusiveStartDate;
	private final LocalDate inclusiveEndDate;

	/**
	 * Records a change in the state of the return on investment
	 * 
	 * @param percentageChange
	 *            amount the net worth has relatively changed by in the given time.
	 * @param exclusiveStartDate
	 *            the beginning of the elapsed time the percentage change occurred.
	 * @param inclusiveEndDate
	 *            the last day of the elapsed time where the percentage change occurred.
	 */
	public ReturnOnInvestmentEventImpl(
	        final BigDecimal percentageChange,
	        final LocalDate exclusiveStartDate,
	        final LocalDate inclusiveEndDate ) {

		this.percentageChange = percentageChange;
		this.exclusiveStartDate = exclusiveStartDate;
		this.inclusiveEndDate = inclusiveEndDate;
	}

	@Override
	public BigDecimal percentageChange() {

		return percentageChange;
	}

	@Override
	public LocalDate inclusiveStartDate() {

		return exclusiveStartDate;
	}

	@Override
	public LocalDate exclusiveEndDate() {

		return inclusiveEndDate;
	}

	@Override
	public String toString() {

		return String.format(
		        "Percentage change: %s, Exclusive start date: %s, Inclusive end date: %s",
		        percentageChange,
		        exclusiveStartDate,
		        inclusiveEndDate);
	}
}
