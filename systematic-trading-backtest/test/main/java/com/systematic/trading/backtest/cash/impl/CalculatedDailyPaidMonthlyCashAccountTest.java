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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.event.Event;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.exception.InsufficientFundsException;

/**
 * Testing the calculate daily paid monthly cash account.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedDailyPaidMonthlyCashAccountTest {

	@Mock
	private InterestRate rate;

	@Mock
	private EventRecorder event;

	@Test
	public void credit() {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event );

		final BigDecimal credit = BigDecimal.valueOf( 1.23456789 );

		assertEquals( BigDecimal.ZERO, account.getBalance() );

		account.credit( credit, LocalDate.now() );

		assertEquals( credit, account.getBalance() );
		verify( event ).record( any( Event.class ) );
	}

	@Test
	public void debit() throws InsufficientFundsException {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, LocalDate.now(), event );

		final BigDecimal debit = BigDecimal.valueOf( 1.23456789 );

		assertEquals( openingFunds, account.getBalance() );

		account.debit( debit, LocalDate.now() );

		assertEquals( openingFunds.subtract( debit ), account.getBalance() );
		verify( event ).record( any( Event.class ) );
	}

	@Test(expected = InsufficientFundsException.class)
	public void debitInsufficientFunds() throws InsufficientFundsException {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event );

		final BigDecimal debit = BigDecimal.valueOf( 1.23456789 );

		account.debit( debit, LocalDate.now() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateDateTooEarly() {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event );

		account.update( LocalDate.now().minus( Period.ofDays( 1 ) ) );
	}

	@Test
	public void oneInterestPayment() {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final LocalDate openingDate = LocalDate.of( 2015, 3, 31 );
		final LocalDate tradingDate = LocalDate.of( 2015, 4, 1 );
		final BigDecimal interest = BigDecimal.valueOf( 1.000045 );
		when( rate.interest( any( BigDecimal.class ), anyInt(), anyBoolean() ) ).thenReturn( interest );

		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, openingDate, event );

		account.update( tradingDate );

		assertEquals( openingFunds.add( interest ), account.getBalance() );
	}

	@Test
	public void oneEscrowNoPayment() {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final LocalDate openingDate = LocalDate.of( 2015, 3, 30 );
		final BigDecimal interest = BigDecimal.valueOf( 1.000045 );
		final LocalDate tradingDate = LocalDate.of( 2015, 3, 31 );
		when( rate.interest( any( BigDecimal.class ), anyInt(), anyBoolean() ) ).thenReturn( interest );

		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, openingDate, event );

		account.update( tradingDate );

		assertEquals( openingFunds, account.getBalance() );
	}

	@Test
	public void twoInterestPayments() {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final LocalDate openingDate = LocalDate.of( 2015, 3, 31 );
		final LocalDate tradingDate = LocalDate.of( 2015, 5, 1 );
		final BigDecimal firstInterest = BigDecimal.valueOf( 1.000045 );
		final BigDecimal secondInterest = BigDecimal.valueOf( 2.00000999 );
		when( rate.interest( any( BigDecimal.class ), anyInt(), anyBoolean() ) ).thenReturn( firstInterest )
				.thenReturn( secondInterest );

		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, openingDate, event );

		account.update( tradingDate );

		assertEquals( openingFunds.add( firstInterest ).add( secondInterest ), account.getBalance() );
	}

	@Test
	public void oneEscrowOneInterestPayment() {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final LocalDate openingDate = LocalDate.of( 2015, 3, 30 );
		final BigDecimal firstInterest = BigDecimal.valueOf( 1.000045 );
		final BigDecimal secondInterest = BigDecimal.valueOf( 2.00000999 );
		when( rate.interest( any( BigDecimal.class ), anyInt(), anyBoolean() ) ).thenReturn( firstInterest )
				.thenReturn( secondInterest );

		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, openingDate, event );

		final LocalDate firstTradingDate = LocalDate.of( 2015, 3, 31 );
		account.update( firstTradingDate );

		final LocalDate secondTradingDate = LocalDate.of( 2015, 4, 1 );
		account.update( secondTradingDate );

		assertEquals( openingFunds.add( firstInterest ).add( secondInterest ), account.getBalance() );
	}

}
