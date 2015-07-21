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
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.event.impl.CashAccounEvent;
import com.systematic.trading.backtest.event.impl.CashAccounEvent.CashAccountEventType;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.exception.InsufficientFundsException;

/**
 * Flat interest rates calculated daily, paid monthly.
 * 
 * @author CJ Hare
 */
public class CalculatedDailyPaidMonthlyCashAccount implements CashAccount {

	/** Rate applied to the funds on a daily basis. */
	private final InterestRate rate;

	/** last date that interest was calculated. */
	private LocalDate lastInterestCalculation;

	/** The current available balance. */
	private BigDecimal funds;

	/** Interest awarded, yet to be realised (paid out). */
	private BigDecimal escrow;

	/** Record keeper for transactions from the Cash Account. */
	private final EventRecorder event;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext context;

	/**
	 * @param rate calculated daily to the funds and paid monthly, cannot be <code>null</code>.
	 * @param openingFunds starting balance for the account, cannot be <code>null</code>.
	 * @param openingDate date to start calculating interest from, cannot be <code>null</code>.
	 * @param event record keeper for deposits, withdrawals and interest from the Cash Account.
	 * @param context math context defining the scale and precision to apply to operations.
	 */
	public CalculatedDailyPaidMonthlyCashAccount( final InterestRate rate, final BigDecimal openingFunds,
			final LocalDate openingDate, final EventRecorder event, final MathContext context ) {
		this.rate = rate;
		this.funds = openingFunds;
		this.lastInterestCalculation = openingDate;
		this.escrow = BigDecimal.ZERO;
		this.event = event;
		this.context = context;
	}

	@Override
	public void update( final LocalDate tradingDate ) {

		// Any trading date earlier in time then our last interest is a mistake
		if (tradingDate.isBefore( lastInterestCalculation )) {
			throw new IllegalArgumentException( String.format( "Given date %s has already been passed by %s",
					tradingDate, lastInterestCalculation ) );
		}

		while (lastInterestCalculation.getMonth() != tradingDate.getMonth()) {
			lastInterestCalculation = applyFullMonthInterest( lastInterestCalculation );
		}

		// Remaining days of interest to escrow
		final boolean isLeapYear = tradingDate.isLeapYear();
		final int daysInterest = Period.between( lastInterestCalculation, tradingDate ).getDays();

		escrow = escrow.add( rate.interest( funds, daysInterest, isLeapYear ), context );

		// Update the interest date marker
		lastInterestCalculation = tradingDate;
	}

	private LocalDate applyFullMonthInterest( final LocalDate last ) {

		// Number of days interest this month
		final int daysInterest = last.getMonth().length( last.isLeapYear() ) - last.getDayOfMonth() + 1;

		// Calculate and pay the interest
		final BigDecimal fundsBefore = funds;
		final boolean isLeapYear = last.isLeapYear();
		final BigDecimal interest = rate.interest( funds, daysInterest, isLeapYear ).add( escrow, context );
		funds = funds.add( interest, context );
		escrow = BigDecimal.ZERO;

		// Next month begins on the first day
		LocalDate firstDayOfNextMonth = LocalDate.of( last.getYear(), last.getMonthValue(), 1 );
		firstDayOfNextMonth = firstDayOfNextMonth.plus( Period.ofMonths( 1 ) );

		// Record the credit transaction
		event.record( new CashAccounEvent( fundsBefore, funds, interest, CashAccountEventType.INTEREST,
				firstDayOfNextMonth ) );

		return firstDayOfNextMonth;
	}

	@Override
	public void debit( final BigDecimal debit, final LocalDate transactionDate ) throws InsufficientFundsException {

		if (funds.compareTo( debit ) < 0) {
			throw new InsufficientFundsException( String.format( "Attempting to debit %s from only %s", debit, funds ) );
		}

		final BigDecimal fundsBefore = funds;

		funds = funds.subtract( debit );

		// Record the debit transaction
		event.record( new CashAccounEvent( fundsBefore, funds, debit, CashAccountEventType.DEBIT, transactionDate ) );
	}

	@Override
	public void credit( final BigDecimal credit, final LocalDate transactionDate ) {
		final BigDecimal fundsBefore = funds;

		funds = funds.add( credit );

		// Record the credit transaction
		event.record( new CashAccounEvent( fundsBefore, funds, credit, CashAccountEventType.CREDIT, transactionDate ) );
	}

	@Override
	public BigDecimal getBalance() {
		// Only available funds count, not those in escrow
		return funds;
	}
}
