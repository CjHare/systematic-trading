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
package com.systematic.trading.backtest.matcher.analysis;

import static org.mockito.Matchers.argThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * ReturnOnInvestmentEvent argument matcher.
 * 
 * @author CJ Hare
 */
public class RoiEventMatcher extends ArgumentMatcher<ReturnOnInvestmentEvent> {
	private final BigDecimal percentageChange;
	private final LocalDate startDateExclusive;
	private final LocalDate endDateInclusive;

	public static ReturnOnInvestmentEvent argumentMatches( final BigDecimal percentageChange,
	        final LocalDate startDateExclusive, final LocalDate endDateInclusive ) {
		return argThat(new RoiEventMatcher(percentageChange, startDateExclusive, endDateInclusive));
	}

	RoiEventMatcher( final BigDecimal percentageChange, final LocalDate startDateExclusive,
	        final LocalDate endDateInclusive ) {
		this.percentageChange = percentageChange;
		this.startDateExclusive = startDateExclusive;
		this.endDateInclusive = endDateInclusive;
	}

	@Override
	public boolean matches( final Object argument ) {
		final ReturnOnInvestmentEvent event = (ReturnOnInvestmentEvent) argument;

		return percentageChange.compareTo(event.getPercentageChange()) == 0
		        && startDateExclusive.equals(event.getExclusiveStartDate())
		        && endDateInclusive.equals(event.getInclusiveEndDate());
	}

	@Override
	public void describeTo( Description description ) {
		description.appendText(String.format("Percentage change: %s, Exclusive start date: %s, Inclusive end date: %s",
		        percentageChange, startDateExclusive, endDateInclusive));
	}
}