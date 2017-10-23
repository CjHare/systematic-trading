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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;
import com.systematic.trading.simulation.matcher.BigDecimalMatcher;

/**
 * Ensures the decorator does act in accordance to expectations.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RegularDepositCashAccountDecoratorTest {
	private static final BigDecimal DEPOSIT_AMOUNT = BigDecimal.valueOf(101);
	private static final Period INTERVAL = Period.ofWeeks(1);
	private static final LocalDate FIRST_DEPOSIT_DATE = LocalDate.of(2010, Month.MARCH, 1);
	private static final LocalDate BEFORE_FIRST_DEPOSIT_DATE = FIRST_DEPOSIT_DATE.minus(Period.ofDays(1));
	private static final LocalDate SECOND_DEPOSIT_DATE = FIRST_DEPOSIT_DATE.plus(INTERVAL);

	@Mock
	private CashAccount account;

	private RegularDepositCashAccountDecorator regularDeposits;

	@Before
	public void setUp() {
		regularDeposits = new RegularDepositCashAccountDecorator(DEPOSIT_AMOUNT, account, FIRST_DEPOSIT_DATE, INTERVAL);
	}

	@Test
	public void credit() {
		final LocalDate transactionDate = LocalDate.of(2010, Month.APRIL, 2);

		credit(76.5432, transactionDate);

		verifyCredit(76.5432, transactionDate);
	}

	@Test
	public void debit() throws InsufficientFundsException {
		final LocalDate transactionDate = LocalDate.of(2010, Month.APRIL, 8);

		debit(54.321, transactionDate);

		veriftDebit(54.321, transactionDate);
	}

	@Test
	public void deposit() {
		final LocalDate transactionDate = LocalDate.of(2010, Month.APRIL, 8);

		deposit(32.123, transactionDate);

		verifyDeposit(32.123, transactionDate);
	}

	@Test
	public void getBalance() {
		setUpBalance(345);

		verifyBalance(345);
	}

	@Test
	public void updateNoDeposit() {
		update(BEFORE_FIRST_DEPOSIT_DATE);

		verifyUpdate(BEFORE_FIRST_DEPOSIT_DATE);
	}

	@Test
	public void updateOneDeposit() {
		update(FIRST_DEPOSIT_DATE);

		verifyDeposits(FIRST_DEPOSIT_DATE, 1);
	}

	@Test
	public void updateTwoDeposits() {
		update(SECOND_DEPOSIT_DATE);

		verifyDeposits(SECOND_DEPOSIT_DATE, 2);
	}

	private void verifyBalance( final double expectedBalance ) {
		final BigDecimal actualBalance = regularDeposits.getBalance();
		assertEquals(String.format("Expected %s != %s", expectedBalance, actualBalance), 0,
		        BigDecimal.valueOf(345).compareTo(actualBalance));
		verify(account).getBalance();
		verifyNoMoreInteractions(account);
	}

	private void verifyDeposits( final LocalDate tradingDate, final int times ) {
		final InOrder order = inOrder(account);
		order.verify(account, times(times)).deposit(DEPOSIT_AMOUNT, tradingDate);
		order.verify(account).update(tradingDate);
		verifyNoMoreInteractions(account);
	}

	private void verifyUpdate( final LocalDate tradingDate ) {
		verify(account).update(tradingDate);
		verifyNoMoreInteractions(account);
	}

	private void update( final LocalDate tradingDate ) {
		regularDeposits.update(tradingDate);
	}

	private void setUpBalance( final double expectedBalance ) {
		when(account.getBalance()).thenReturn(BigDecimal.valueOf(expectedBalance));
	}

	private void credit( final double amount, final LocalDate transactionDate ) {
		regularDeposits.credit(BigDecimal.valueOf(amount), transactionDate);
	}

	private void verifyCredit( final double amount, final LocalDate transactionDate ) {
		verify(account).credit(BigDecimalMatcher.argumentMatches(amount), eq(transactionDate));
		verifyNoMoreInteractions(account);
	}

	private void debit( final double amount, final LocalDate transactionDate ) throws InsufficientFundsException {
		regularDeposits.debit(BigDecimal.valueOf(amount), transactionDate);
	}

	private void veriftDebit( final double amount, final LocalDate transactionDate ) throws InsufficientFundsException {
		verify(account).debit(BigDecimalMatcher.argumentMatches(amount), eq(transactionDate));
		verifyNoMoreInteractions(account);
	}

	private void deposit( final double amount, final LocalDate transactionDate ) {
		regularDeposits.deposit(BigDecimal.valueOf(amount), transactionDate);
	}

	private void verifyDeposit( final double amount, final LocalDate transactionDate ) {
		verify(account).deposit(BigDecimalMatcher.argumentMatches(amount), eq(transactionDate));
		verifyNoMoreInteractions(account);
	}
}