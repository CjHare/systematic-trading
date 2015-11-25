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
package com.systematic.trading.backtest.cash;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.RegularDepositCashAccountDecorator;
import com.systematic.trading.simulation.order.exception.InsufficientFundsException;

/**
 * Ensures the decorator does act in accordance to expectations.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class RegularDepositCashAccountDecoratorTest {

	@Mock
	private CashAccount account;

	private final BigDecimal depositAmount = BigDecimal.valueOf( 101 );
	private final LocalDate firstDeposit = LocalDate.now();
	private final Period interval = Period.ofWeeks( 1 );

	@Test
	public void credit() {
		final BigDecimal creditAmount = BigDecimal.valueOf( 77 );
		final LocalDate transactionDate = LocalDate.of( 2010, Month.APRIL, 2 );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.credit( creditAmount, transactionDate );

		verify( account ).credit( creditAmount, transactionDate );
		verifyNoMoreInteractions( account );
	}

	@Test
	public void debit() throws InsufficientFundsException {
		final BigDecimal creditAmount = BigDecimal.valueOf( 55 );
		final LocalDate transactionDate = LocalDate.of( 2010, Month.APRIL, 8 );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.debit( creditAmount, transactionDate );

		verify( account ).debit( creditAmount, transactionDate );
		verifyNoMoreInteractions( account );
	}

	@Test
	public void deposit() {
		final BigDecimal creditAmount = BigDecimal.valueOf( 55 );
		final LocalDate transactionDate = LocalDate.of( 2010, Month.APRIL, 8 );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.deposit( creditAmount, transactionDate );

		verify( account ).deposit( creditAmount, transactionDate );
		verifyNoMoreInteractions( account );
	}

	@Test
	public void getBalance() {
		final BigDecimal expectedBalance = BigDecimal.valueOf( 345 );
		when( account.getBalance() ).thenReturn( expectedBalance );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		final BigDecimal actualBalance = regularDeposits.getBalance();

		assertEquals( expectedBalance, actualBalance );
		verify( account ).getBalance();
		verifyNoMoreInteractions( account );
	}

	@Test
	public void updateNoDeposit() {
		final LocalDate transactionDate = LocalDate.now();
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.update( transactionDate );

		verify( account ).update( transactionDate );
		verifyNoMoreInteractions( account );
	}

	@Test
	public void updateOneDeposit() {
		final LocalDate transactionDate = LocalDate.now().plus( Period.ofDays( 1 ) );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.update( transactionDate );

		verify( account ).deposit( depositAmount, transactionDate );
		verify( account ).update( transactionDate );
		verifyNoMoreInteractions( account );
	}

	@Test
	public void updateTwoDeposits() {
		final LocalDate transactionDate = LocalDate.now().plus( Period.ofDays( 1 ) ).plus( interval );
		final RegularDepositCashAccountDecorator regularDeposits = new RegularDepositCashAccountDecorator(
				depositAmount, account, firstDeposit, interval );

		regularDeposits.update( transactionDate );

		verify( account, times( 2 ) ).deposit( depositAmount, transactionDate );
		verify( account ).update( transactionDate );
		verifyNoMoreInteractions( account );
	}
}
