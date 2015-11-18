/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.analysis.roi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.event.ReturnOnInvestmentEvent;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEventImpl;
import com.systematic.trading.backtest.event.ReturnOnInvestmentEventListener;

/**
 * Creates periodic cumulative ROI events.
 * 
 * @author CJ Hare
 */
public class PeriodicCulmativeReturnOnInvestmentCalculator implements ReturnOnInvestmentEventListener {

	/** Context for BigDecimal operations. */
	private final MathContext mathContext;

	/** Parties interested in ROI events. */
	private final List<ReturnOnInvestmentEventListener> listeners = new ArrayList<ReturnOnInvestmentEventListener>();

	/** Aggregates the cumulative ROI for every summary period. */
	private final Period summaryPeriod;

	/** Date of the last summary event. */
	private LocalDate lastSummaryDate, nextSummaryDate;

	/** The running date for calculating when the summary period has passed. */
	private LocalDate date;

	/** Running total of the ROI for the period so far. */
	private BigDecimal cumulativeROI = BigDecimal.ZERO;

	public PeriodicCulmativeReturnOnInvestmentCalculator( final LocalDate startingDate, final Period summaryPeriod,
			final MathContext mathContext ) {
		this.date = startingDate;
		this.mathContext = mathContext;
		this.summaryPeriod = summaryPeriod;

		lastSummaryDate = startingDate;
		nextSummaryDate = startingDate.plus( summaryPeriod );
	}

	private void notifyListeners( final BigDecimal percentageChange, final LocalDate exclusiveStartDate,
			final LocalDate inclusiveEndDate ) {
		for (final ReturnOnInvestmentEventListener listener : listeners) {
			listener.event( new ReturnOnInvestmentEventImpl( percentageChange, exclusiveStartDate, inclusiveEndDate ) );
		}
	}

	public void addListener( final ReturnOnInvestmentEventListener listener ) {
		if (!listeners.contains( listener )) {
			listeners.add( listener );
		}
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {
		final BigDecimal percentageChange = event.getPercentageChange();
		date = event.getInclusiveEndDate();
		cumulativeROI = cumulativeROI.add( percentageChange, mathContext );

		if (nextSummaryDate.isBefore( date )) {
			notifyListeners( cumulativeROI, lastSummaryDate, date );
			cumulativeROI = BigDecimal.ZERO;
			lastSummaryDate = date;
			nextSummaryDate = nextSummaryDate.plus( summaryPeriod );
		}
	}
}
