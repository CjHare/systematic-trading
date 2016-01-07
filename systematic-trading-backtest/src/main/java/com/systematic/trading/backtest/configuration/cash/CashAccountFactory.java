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
package com.systematic.trading.backtest.configuration.cash;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

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
	private static final Logger LOG = LogManager.getLogger( CashAccountFactory.class );

	public static CashAccount create( final LocalDate startDate, final BigDecimal depositAmount,
			final Period depositFrequency, final MathContext mathContext ) {

		// TODO all these into a configuration - interest rate, deposit & frequency
		final BigDecimal annualRate = BigDecimal.valueOf( 1.5 );
		final InterestRate annualInterestRate = InterestRateFactory
				.create( InterestRateConfiguration.FLAT_INTEREST_RATE, annualRate, mathContext );
		final CashAccount underlyingAccount = CashAccountFactory.create(
				CashAccountConfiguration.CALCULATED_DAILY_PAID_MONTHLY, annualInterestRate, BigDecimal.ZERO, startDate,
				mathContext );
		return new RegularDepositCashAccountDecorator( depositAmount, underlyingAccount, startDate, depositFrequency );
	}

	/**
	 * Create an instance of the a cash account.
	 */
	public static CashAccount create( final CashAccountConfiguration configuration,
			final InterestRate annualInterestRate, final BigDecimal openingFunds, final LocalDate openingDate,
			final MathContext mathContext ) {

		try {
			Constructor<?> cons = configuration.getType().getConstructor( CashAccountConfiguration.class,
					BigDecimal.class, LocalDate.class, MathContext.class );

			return (CashAccount) cons.newInstance( annualInterestRate, openingFunds, openingDate, mathContext );
		} catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error( e );
		}

		throw new IllegalArgumentException(
				String.format( "Could not create the desired cash accounte: %s", configuration ) );
	}
}
