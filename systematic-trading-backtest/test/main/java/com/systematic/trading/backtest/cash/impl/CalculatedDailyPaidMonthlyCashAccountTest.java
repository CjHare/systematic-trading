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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.event.CashEvent.CashEventType;
import com.systematic.trading.backtest.event.impl.CashAccountEvent;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.exception.InsufficientFundsException;

/**
 * Testing the calculate daily paid monthly cash account.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedDailyPaidMonthlyCashAccountTest {
	private final MathContext mc = MathContext.DECIMAL64;
	private static final DecimalFormat TWO_DECIMAL_PLACES;

	static {
		TWO_DECIMAL_PLACES = new DecimalFormat();
		TWO_DECIMAL_PLACES.setMaximumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setMinimumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setGroupingUsed( false );
	}

	@Mock
	private InterestRate rate;

	@Mock
	private EventRecorder event;

	@Test
	public void credit() {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event, mc );

		final BigDecimal credit = BigDecimal.valueOf( 1.23456789 );

		assertEquals( BigDecimal.ZERO, account.getBalance() );

		final LocalDate date = LocalDate.now();
		account.credit( credit, date );

		assertEquals( credit, account.getBalance() );
		verify( event )
				.record( isCashAccountEvent( BigDecimal.ZERO, credit, credit, CashEventType.CREDIT, date ) );
	}

	@Test
	public void deposit() {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event, mc );

		final BigDecimal deposit = BigDecimal.valueOf( 1.23456789 );

		assertEquals( BigDecimal.ZERO, account.getBalance() );

		final LocalDate date = LocalDate.now();
		account.deposit( deposit, date );

		assertEquals( deposit, account.getBalance() );
		verify( event ).record(
				isCashAccountEvent( BigDecimal.ZERO, deposit, deposit, CashEventType.DEPOSIT, date ) );
	}

	@Test
	public void debit() throws InsufficientFundsException {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, LocalDate.now(), event, mc );

		final BigDecimal debit = BigDecimal.valueOf( 1.23456789 );

		assertEquals( openingFunds, account.getBalance() );

		final LocalDate date = LocalDate.now();
		account.debit( debit, date );

		assertEquals( openingFunds.subtract( debit ), account.getBalance() );

		verify( event ).record(
				isCashAccountEvent( openingFunds, openingFunds.subtract( debit, mc ), debit,
						CashEventType.DEBIT, date ) );
	}

	@Test(expected = InsufficientFundsException.class)
	public void debitInsufficientFunds() throws InsufficientFundsException {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event, mc );

		final BigDecimal debit = BigDecimal.valueOf( 1.23456789 );

		account.debit( debit, LocalDate.now() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateDateTooEarly() {
		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				BigDecimal.ZERO, LocalDate.now(), event, mc );

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
				openingFunds, openingDate, event, mc );

		account.update( tradingDate );

		assertEquals( openingFunds.add( interest ), account.getBalance() );

		verify( event ).record(
				isCashAccountEvent( openingFunds, BigDecimal.valueOf( 101 ), BigDecimal.valueOf( 1 ),
						CashEventType.INTEREST, tradingDate ) );
	}

	@Test
	public void oneEscrowNoPayment() {
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final LocalDate openingDate = LocalDate.of( 2015, 3, 30 );
		final BigDecimal interest = BigDecimal.valueOf( 1.000045 );
		final LocalDate tradingDate = LocalDate.of( 2015, 3, 31 );
		when( rate.interest( any( BigDecimal.class ), anyInt(), anyBoolean() ) ).thenReturn( interest );

		final CalculatedDailyPaidMonthlyCashAccount account = new CalculatedDailyPaidMonthlyCashAccount( rate,
				openingFunds, openingDate, event, mc );

		account.update( tradingDate );

		assertEquals( openingFunds, account.getBalance() );
		verify( rate ).interest( openingFunds, 1, false );
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
				openingFunds, openingDate, event, mc );

		account.update( tradingDate );

		assertEquals( openingFunds.add( firstInterest ).add( secondInterest ), account.getBalance() );
		verify( rate ).interest( openingFunds, 1, false );
		verify( rate ).interest( openingFunds.add( firstInterest ), 30, false );
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
				openingFunds, openingDate, event, mc );

		final LocalDate firstTradingDate = LocalDate.of( 2015, 3, 31 );
		account.update( firstTradingDate );

		final LocalDate secondTradingDate = LocalDate.of( 2015, 4, 1 );
		account.update( secondTradingDate );

		assertEquals( openingFunds.add( firstInterest ).add( secondInterest ), account.getBalance() );
		verify( rate, times( 2 ) ).interest( openingFunds, 1, false );
	}

	private CashAccountEvent isCashAccountEvent( final BigDecimal fundsBefore, final BigDecimal fundsAfter,
			final BigDecimal interest, final CashEventType type, final LocalDate transactionDate ) {
		return argThat( new IsCashAccounEventArgument( fundsBefore, fundsAfter, interest, type, transactionDate ) );
	}

	class IsCashAccounEventArgument extends ArgumentMatcher<CashAccountEvent> {

		private final String amount;
		private final String fundsBefore;
		private final String fundsAfter;
		private final LocalDate transactionDate;
		private final CashEventType type;

		public IsCashAccounEventArgument( final BigDecimal fundsBefore, final BigDecimal fundsAfter,
				final BigDecimal amount, final CashEventType type, final LocalDate transactionDate ) {
			this.fundsBefore = TWO_DECIMAL_PLACES.format( fundsBefore );
			this.fundsAfter = TWO_DECIMAL_PLACES.format( fundsAfter );
			this.amount = TWO_DECIMAL_PLACES.format( amount );
			this.transactionDate = transactionDate;
			this.type = type;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof CashAccountEvent) {
				final CashAccountEvent event = (CashAccountEvent) argument;
				return amount.equals( event.getAmount() ) && fundsBefore.equals( event.getFundsBefore() )
						&& fundsAfter.equals( event.getFundsAfter() )
						&& transactionDate.equals( event.getTransactionDate() ) && type == event.getType();
			}

			return false;
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( String.format( "%s, %s, %s, %s, %s", fundsBefore, fundsAfter, amount, type,
					transactionDate ) );
		}
	}
}
