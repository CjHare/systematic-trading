/**
 * Copyright (c) 2015-2017-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.simulation.cash;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.simulation.cash.event.CashAccountEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.cash.event.CashEvent.CashEventType;
import com.systematic.trading.simulation.order.exception.InsufficientFundsException;

/**
 * Flat interest rates calculated daily, paid monthly.
 * 
 * @author CJ Hare
 */
public class CalculatedDailyPaidMonthlyCashAccount implements CashAccount {

	/** Rate applied to the funds on a daily basis. */
	private final InterestRate rate;

	/** last date that interest was calculated, or when to begin calculations. */
	private LocalDate lastInterestCalculation;

	/** The current available balance. */
	private BigDecimal funds;

	/** Interest awarded, yet to be realised (paid out). */
	private BigDecimal escrow;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Parties interested in the account events. */
	private final List<CashEventListener> listeners = new ArrayList<>();

	/**
	 * @param rate calculated daily to the funds and paid monthly, cannot be <code>null</code>.
	 * @param openingFunds starting balance for the account, cannot be <code>null</code>.
	 * @param openingDate date to start calculating interest from, cannot be <code>null</code>.
	 * @param mathContext math context defining the scale and precision to apply to operations.
	 */
	public CalculatedDailyPaidMonthlyCashAccount(final InterestRate rate, final BigDecimal openingFunds,
	        final LocalDate openingDate, final MathContext mathContext) {
		this.rate = rate;
		this.funds = openingFunds;
		this.lastInterestCalculation = openingDate;
		this.escrow = BigDecimal.ZERO;

		this.mathContext = mathContext;
	}

	@Override
	public void update( final LocalDate tradingDate ) {

		// Only calculate interest when the date is after the last calculation date
		if (tradingDate.isAfter(lastInterestCalculation)) {

			while (lastInterestCalculation.getMonth() != tradingDate.getMonth()) {
				lastInterestCalculation = applyFullMonthInterest(lastInterestCalculation);
			}

			// Remaining days of interest to escrow
			final boolean isLeapYear = tradingDate.isLeapYear();
			final int daysInterest = Period.between(lastInterestCalculation, tradingDate).getDays();

			escrow = escrow.add(rate.interest(funds, daysInterest, isLeapYear), mathContext);

			// Update the interest date marker
			lastInterestCalculation = tradingDate;
		}
	}

	private LocalDate applyFullMonthInterest( final LocalDate last ) {

		// Number of days interest this month
		final int daysInterest = last.getMonth().length(last.isLeapYear()) - last.getDayOfMonth() + 1;

		// Calculate and pay the interest
		final BigDecimal fundsBefore = funds;
		final boolean isLeapYear = last.isLeapYear();
		final BigDecimal interest = rate.interest(funds, daysInterest, isLeapYear).add(escrow, mathContext);
		funds = funds.add(interest, mathContext);
		escrow = BigDecimal.ZERO;

		// Next month begins on the first day
		LocalDate firstDayOfNextMonth = LocalDate.of(last.getYear(), last.getMonthValue(), 1);
		firstDayOfNextMonth = firstDayOfNextMonth.plus(Period.ofMonths(1));

		// Record the credit transaction
		notifyListeners(
		        new CashAccountEvent(fundsBefore, funds, interest, CashEventType.INTEREST, firstDayOfNextMonth));

		return firstDayOfNextMonth;
	}

	@Override
	public void debit( final BigDecimal debit, final LocalDate transactionDate ) throws InsufficientFundsException {

		if (funds.compareTo(debit) < 0) {
			throw new InsufficientFundsException(String.format("Attempting to debit %s from only %s", debit, funds));
		}

		final BigDecimal fundsBefore = funds;

		funds = funds.subtract(debit);

		// Record the debit transaction
		notifyListeners(new CashAccountEvent(fundsBefore, funds, debit, CashEventType.DEBIT, transactionDate));
	}

	@Override
	public void credit( final BigDecimal credit, final LocalDate transactionDate ) {
		final BigDecimal fundsBefore = funds;

		funds = funds.add(credit);

		// Record the credit transaction
		notifyListeners(new CashAccountEvent(fundsBefore, funds, credit, CashEventType.CREDIT, transactionDate));
	}

	@Override
	public BigDecimal getBalance() {
		// Only available funds count, not those in escrow
		return funds;
	}

	@Override
	public void deposit( final BigDecimal deposit, final LocalDate transactionDate ) {
		final BigDecimal fundsBefore = funds;

		funds = funds.add(deposit);

		// Record the credit transaction
		notifyListeners(new CashAccountEvent(fundsBefore, funds, deposit, CashEventType.DEPOSIT, transactionDate));
	}

	private void notifyListeners( final CashEvent event ) {
		for (final CashEventListener listener : listeners) {
			listener.event(event);
		}
	}

	@Override
	public void addListener( final CashEventListener listener ) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
}
