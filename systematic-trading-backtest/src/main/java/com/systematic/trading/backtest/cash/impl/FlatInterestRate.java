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
package com.systematic.trading.backtest.cash.impl;

import java.math.BigDecimal;
import java.math.MathContext;

import com.systematic.trading.backtest.cash.InterestRate;

/**
 * The same interest rate is applied on all funds.
 * 
 * @author CJ Hare
 */
public class FlatInterestRate implements InterestRate {

	private static final BigDecimal DAYS_IN_LEAP_YEAR = BigDecimal.valueOf( 366 );
	private static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf( 365 );
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );

	/** Interest applied daily in a non-leap year. */
	private final BigDecimal dailyInterestRate;

	/** Interest applied daily in a leap year. */
	private final BigDecimal dailyInterestRateLeapYear;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/**
	 * @param annualInterestRate rate of interest applied over the course of a year, where 2% is
	 *            2.0.
	 * @param mathContext math context defining the scale and precision to apply to operations.
	 */
	public FlatInterestRate( final BigDecimal annualInterestRate, final MathContext mathContext ) {

		this.mathContext = mathContext;

		// Small numbers for the daily interest rate
		final BigDecimal scaledAnnualInterestRate = annualInterestRate.divide( ONE_HUNDRED, this.mathContext );

		this.dailyInterestRate = scaledAnnualInterestRate.divide( DAYS_IN_YEAR, this.mathContext );
		this.dailyInterestRateLeapYear = scaledAnnualInterestRate.divide( DAYS_IN_LEAP_YEAR, this.mathContext );
	}

	@Override
	public BigDecimal interest( final BigDecimal funds, final int days, final boolean isLeapYear ) {
		if (days == 0) {
			return BigDecimal.ZERO;
		}

		if (isLeapYear) {
			return dailyInterestRateLeapYear.multiply( funds, mathContext ).multiply( BigDecimal.valueOf( days ), mathContext );
		}

		return dailyInterestRate.multiply( funds, mathContext ).multiply( BigDecimal.valueOf( days ), mathContext );
	}
}
