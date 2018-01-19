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
package com.systematic.trading.backtest.output.elastic.model;

import java.time.Period;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * Frequency for return on investment events, as used by Elastic Search return-on-investment index.
 * 
 * @author CJ Hare
 */
public class ElasticReturnOnInvestmentEventFrequency {

	private final String frequency;

	public ElasticReturnOnInvestmentEventFrequency( final ReturnOnInvestmentEvent event ) {

		this.frequency = frequency(Period.between(event.inclusiveStartDate(), event.exclusiveEndDate()));
	}

	private String frequency( final Period frequency ) {

		if (isYearly(frequency)) { return ElasticTypeValue.YEARLY; }

		if (isMonthly(frequency)) { return ElasticTypeValue.MONTHLY; }

		if (isWeekly(frequency)) { return ElasticTypeValue.WEEKLY; }

		return ElasticTypeValue.DAILY;
	}

	/**
	 * Accepts just either side of being yearly in addition to being spot on, catering for offset
	 * years.
	 */
	private boolean isYearly( final Period frequency ) {

		return frequency.getYears() > 0 || frequency.getMonths() >= 11;
	}

	/**
	 * Accepts just either side of being monthly in addition to being spot on, catering for offset
	 * months.
	 */
	private boolean isMonthly( final Period frequency ) {

		return frequency.getMonths() > 0 || frequency.getDays() >= 26;
	}

	/**
	 * Five or more days is considered weekly, everything below falls into daily.
	 */
	private boolean isWeekly( final Period frequency ) {

		return frequency.getDays() >= 5;
	}

	public String frequency() {

		return frequency;
	}
}