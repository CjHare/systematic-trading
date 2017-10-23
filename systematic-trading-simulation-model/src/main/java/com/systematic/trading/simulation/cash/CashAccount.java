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
package com.systematic.trading.simulation.cash;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;

/**
 * Cash flow and interest management.
 * 
 * @author CJ Hare
 */
public interface CashAccount {

	/**
	 * Applies relevant interest calculations and payments based on the passage of time.
	 * 
	 * @param tradingDate the next day of trading data to add.
	 */
	void update( final LocalDate tradingDate );

	/**
	 * Removes funds from an account.
	 * 
	 * @param debitAmount sum to be removed from the account.
	 * @param transactionDate date of the debit.
	 * @throws InsufficientFundsException encountered when the funds cannot be debited.
	 */
	void debit( BigDecimal debitAmount, LocalDate transactionDate ) throws InsufficientFundsException;

	/**
	 * Adds funds to an account.
	 * 
	 * @param creditAmount sum to be added to the account.
	 * @param transactionDate date of the credit.
	 */
	void credit( BigDecimal creditAmount, LocalDate transactionDate );

	/**
	 * Adds funds to an account that is considered a deposit, where the funds come from an outside
	 * source rather then from a trading activity.
	 * 
	 * @param depositAmount sum to be added to the account.
	 * @param transactionDate date of the deposit.
	 */
	void deposit( BigDecimal depositAmount, LocalDate transactionDate );

	/**
	 * Retrieves the current balance of the account.
	 * 
	 * @return positive number when the account is credit, negative otherwise.
	 */
	BigDecimal getBalance();

	/**
	 * Adds a listener that is interested in CashEvents.
	 * 
	 * @param listener to receive cash event notifications.
	 */
	void addListener( CashEventListener listener );
}