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
package com.systematic.trading.backtest.configuration.cash;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import com.systematic.trading.simulation.cash.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.FlatInterestRate;
import com.systematic.trading.simulation.cash.InterestRate;
import com.systematic.trading.simulation.cash.RegularDepositCashAccountDecorator;

/**
 * Creates instances of the available fee structures.
 * 
 * @author CJ Hare
 */
public class CashAccountFactory {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	public CashAccount create( final LocalDate startDate, final CashAccountConfiguration cashAccount ) {

		final InterestRate annualInterestRate = interestRate(cashAccount);
		final Optional<DepositConfiguration> deposit = cashAccount.deposit();

		if (deposit.isPresent()) {

			final CashAccount underlyingAccount = cashAccount(annualInterestRate, cashAccount, startDate);
			final BigDecimal depositAmount = deposit.get().amount();
			final Period depositFrequency = deposit.get().frequency().period();

			return new RegularDepositCashAccountDecorator(
			        depositAmount,
			        underlyingAccount,
			        startDate,
			        depositFrequency);
		}

		return cashAccount(annualInterestRate, cashAccount, startDate);
	}

	/**
	 * Create an instance of the a cash account.
	 */
	private CashAccount cashAccount(
	        final InterestRate rate,
	        final CashAccountConfiguration cashAccount,
	        final LocalDate openingDate ) {

		return new CalculatedDailyPaidMonthlyCashAccount(rate, cashAccount.openingFunds(), openingDate, MATH_CONTEXT);
	}

	private InterestRate interestRate( final CashAccountConfiguration cashAccount ) {

		return new FlatInterestRate(cashAccount.interestRate(), MATH_CONTEXT);
	}

}
