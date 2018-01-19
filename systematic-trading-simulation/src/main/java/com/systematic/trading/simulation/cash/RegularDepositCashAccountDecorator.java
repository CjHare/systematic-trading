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
package com.systematic.trading.simulation.cash;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;

/**
 * Adds frequent deposits to an underlying Cash Account.
 * 
 * @author CJ Hare
 */
public class RegularDepositCashAccountDecorator implements CashAccount {

	/** Cash account being decorated with a regular deposit. */
	private final CashAccount account;

	/** Amount to deposit into the cash account each time. */
	private final BigDecimal depositAmount;

	/** Last time a deposit was made, determining when the next will occur. */
	private LocalDate lastDeposit;

	/** Time between deposit events. */
	private final Period interval;

	/**
	 * @param depositAmount
	 *            size of each deposit.
	 * @param account
	 *            Cash account that receives the deposits.
	 * @param firstDeposit
	 *            date for the first deposit.
	 * @param interval
	 *            time between deposit events.
	 */
	public RegularDepositCashAccountDecorator( final BigDecimal depositAmount, final CashAccount account,
	        final LocalDate firstDeposit, final Period interval ) {

		this.account = account;
		this.depositAmount = depositAmount;
		this.interval = interval;

		// The first order needs to be on that date, not interval after
		lastDeposit = LocalDate.from(firstDeposit).minus(interval).minus(Period.ofDays(1));
	}

	@Override
	public void update( final LocalDate tradingDate ) {

		if (isDepositTime(tradingDate)) {

			// Multiple intervals may have elapsed
			final int numberOfDeposits = numberOfDeposits(tradingDate);
			for (int i = 0; i < numberOfDeposits; i++) {
				deposit(depositAmount, tradingDate);
			}

			lastDeposit = lastDeposit.plus(interval.multipliedBy(numberOfDeposits));
		}

		account.update(tradingDate);
	}

	@Override
	public void debit( final BigDecimal debitAmount, final LocalDate transactionDate )
	        throws InsufficientFundsException {

		account.debit(debitAmount, transactionDate);
	}

	@Override
	public void credit( final BigDecimal creditAmount, final LocalDate transactionDate ) {

		account.credit(creditAmount, transactionDate);
	}

	@Override
	public BigDecimal balance() {

		return account.balance();
	}

	@Override
	public void deposit( final BigDecimal depositAmount, final LocalDate transactionDate ) {

		account.deposit(depositAmount, transactionDate);
	}

	@Override
	public void addListener( final CashEventListener listener ) {

		account.addListener(listener);
	}

	private boolean isDepositTime( final LocalDate tradingDate ) {

		return tradingDate.isAfter(lastDeposit.plus(interval));
	}

	private int numberOfDeposits( final LocalDate tradingDate ) {

		return Period.between(lastDeposit, tradingDate).getDays() / interval.getDays();
	}

	// TODO shift the interval and deposit amount into a tuple object
	public BigDecimal depositAmount() {

		return depositAmount;
	}

	public Period interval() {

		return interval;
	}
}