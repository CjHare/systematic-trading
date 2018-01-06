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
package com.systematic.trading.backtest.configuration.cash;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.InterestRate;
import com.systematic.trading.simulation.cash.RegularDepositCashAccountDecorator;

/**
 * Creates instances of the available fee structures.
 * 
 * @author CJ Hare
 */
public class CashAccountFactory {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(CashAccountFactory.class);

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	private InterestRate interestRate( final CashAccountConfiguration cashAccount ) {

		return new InterestRateFactory().create(InterestRateConfigurationType.FLAT_INTEREST_RATE,
		        cashAccount.interestRate(), MATH_CONTEXT);
	}

	public CashAccount create( final LocalDate startDate, final CashAccountConfiguration cashAccount ) {

		final InterestRate annualInterestRate = interestRate(cashAccount);
		final Optional<DepositConfiguration> deposit = cashAccount.deposit();

		if (deposit.isPresent()) {

			final BigDecimal depositAmount = deposit.get().amount();
			final Period depositFrequency = deposit.get().frequency().period();
			final CashAccount underlyingAccount = create(CashAccountConfigurationType.CALCULATED_DAILY_PAID_MONTHLY,
			        annualInterestRate, cashAccount.openingFunds(), startDate);

			return new RegularDepositCashAccountDecorator(depositAmount, underlyingAccount, startDate,
			        depositFrequency);
		}

		return create(CashAccountConfigurationType.CALCULATED_DAILY_PAID_MONTHLY, annualInterestRate,
		        cashAccount.openingFunds(), startDate);
	}

	/**
	 * Create an instance of the a cash account.
	 */
	private CashAccount create( final CashAccountConfigurationType configuration, final InterestRate annualInterestRate,
	        final BigDecimal openingFunds, final LocalDate openingDate ) {

		try {
			Constructor<?> cons = configuration.type().getConstructor(InterestRate.class, BigDecimal.class,
			        LocalDate.class, MathContext.class);

			return (CashAccount) cons.newInstance(annualInterestRate, openingFunds, openingDate, MATH_CONTEXT);
		} catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
		        | IllegalArgumentException | InvocationTargetException e) {
			LOG.error(e);
		}

		throw new IllegalArgumentException(
		        String.format("Could not create the desired cash accounte: %s", configuration));
	}
}