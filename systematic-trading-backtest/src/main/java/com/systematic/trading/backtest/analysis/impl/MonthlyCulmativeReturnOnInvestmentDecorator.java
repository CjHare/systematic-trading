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
package com.systematic.trading.backtest.analysis.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.event.listener.ReturnOnInvestmentListener;

/**
 * Creates monthly cumulative ROI events.
 * 
 * @author CJ Hare
 */
public class MonthlyCulmativeReturnOnInvestmentDecorator implements ReturnOnInvestmentListener {

	/** Context for BigDecimal operations. */
	private final MathContext mathContext;

	/** Deals with the recording the changes in the ROI. */
	private final ReturnOnInvestmentListener eventRecorer;

	/** The running date for calculating when the months have passed. */
	private LocalDate date;

	/** Running total of the ROI for the month so far. */
	private BigDecimal cumulativeROI = BigDecimal.ZERO;

	public MonthlyCulmativeReturnOnInvestmentDecorator( final LocalDate startingDate,
			final ReturnOnInvestmentListener eventRecorder, final MathContext mathContext ) {
		this.eventRecorer = eventRecorder;
		this.date = startingDate;
		this.mathContext = mathContext;
	}

	@Override
	public void record( final BigDecimal percentageChange, final Period elapsed ) {

		eventRecorer.record( percentageChange, elapsed );

		date = date.plus( elapsed );
		cumulativeROI = cumulativeROI.add( percentageChange, mathContext );

		System.err.println( date + "  " + cumulativeROI );

		// TODO output somewhere, but where? another listener?
	}
}
