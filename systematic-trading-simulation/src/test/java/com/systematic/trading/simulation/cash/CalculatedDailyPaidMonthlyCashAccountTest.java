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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;
import com.systematic.trading.simulation.matcher.BigDecimalMatcher;

/**
 * Testing the calculate daily paid monthly cash account.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedDailyPaidMonthlyCashAccountTest {
	private static final LocalDate ACCOUNT_OPEN_DATE = LocalDate.of(2015, 3, 15);
	private static final LocalDate ACCOUNT_OPENING_MONTH = LocalDate.of(2015, 3, 31);
	private static final LocalDate MONTH_AFTER_OPENING = LocalDate.of(2015, 4, 1);
	private static final LocalDate TWO_MONTHS_AFTER_OPENING = LocalDate.of(2015, 5, 1);

	@Mock
	private InterestRate rate;

	private CalculatedDailyPaidMonthlyCashAccount account;

	@Test
	public void credit() {
		setUpCashAccount();

		credit(1.23456789);

		verifyBalance(1.23456789);
		verifyNoInterestRateCalculation();
	}

	@Test
	public void creditExistingFunds() {
		setUpCashAccount(55);

		credit(1.23456789);

		verifyBalance(56.23456789);
		verifyNoInterestRateCalculation();
	}

	@Test
	public void deposit() {
		setUpCashAccount();

		deposit(1.23456789);

		verifyBalance(1.23456789);
		verifyNoInterestRateCalculation();
	}

	@Test
	public void depositExistingFunds() {
		setUpCashAccount(45);

		deposit(1.23456789);

		verifyBalance(46.23456789);
		verifyNoInterestRateCalculation();
	}

	@Test
	public void debit() throws InsufficientFundsException {
		setUpCashAccount(100);

		debit(1.23456789);

		verifyBalance(98.76543211);
		verifyNoInterestRateCalculation();
	}

	@Test(expected = InsufficientFundsException.class)
	public void debitInsufficientFunds() throws InsufficientFundsException {
		setUpCashAccount();

		debit(1.23456789);
	}

	@Test
	public void updateTooSoonForInterestPayment() {
		setUpCashAccount(1);

		update(ACCOUNT_OPEN_DATE);

		verifyBalance(1);
		verifyNoInterestRateCalculation();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void oneInterestPayment() {
		setUpInterestCalculation(1.000045);
		setUpCashAccount(100);

		update(MONTH_AFTER_OPENING);

		verifyBalance(101.000045);
		verifyInterestCalculations(calculation(100, 17));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void oneEscrowNoPayment() {
		setUpInterestCalculation(1.000045);
		setUpCashAccount(100.56);

		update(ACCOUNT_OPENING_MONTH);

		verifyBalance(100.56);
		verifyInterestCalculations(calculation(100.56, 16));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void twoInterestPayments() {
		setUpInterestCalculation(1.23, 2.00089);
		setUpCashAccount(100);

		update(TWO_MONTHS_AFTER_OPENING);

		verifyBalance(103.23089);
		verifyInterestCalculations(calculation(100, 17), calculation(101.23, 30));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void oneEscrowOneInterestPayment() {
		setUpInterestCalculation(1.45, 2.0089);
		setUpCashAccount(100);

		update(ACCOUNT_OPENING_MONTH);
		update(MONTH_AFTER_OPENING);

		verifyBalance(103.4589);
		verifyInterestCalculations(calculation(100, 16), calculation(100, 1));
	}

	private Pair<Double, Integer> calculation( final double funds, final int daysOfInterest ) {
		return new ImmutablePair<Double, Integer>(funds, daysOfInterest);
	}

	@SuppressWarnings("unchecked")
	private void verifyInterestCalculations( final Pair<Double, Integer>... arguments ) {
		InOrder order = inOrder(rate);

		for (final Pair<Double, Integer> argument : arguments) {
			order.verify(rate).interest(BigDecimalMatcher.argumentMatches(argument.getLeft()), eq(argument.getRight()),
			        eq(false));
		}

		verifyNoMoreInteractions(rate);
	}

	private void setUpCashAccount() {
		setUpCashAccount(0);
	}

	private void setUpCashAccount( final double openingFunds ) {
		account = new CalculatedDailyPaidMonthlyCashAccount(rate, BigDecimal.valueOf(openingFunds), ACCOUNT_OPEN_DATE,
		        MathContext.DECIMAL64);

		assertEquals(String.format("Starting balance incorrect %s != %s", account.getBalance(), openingFunds), 0,
		        BigDecimal.valueOf(openingFunds).compareTo(account.getBalance()));
	}

	private void setUpInterestCalculation( final double... payments ) {
		OngoingStubbing<BigDecimal> interest = when(rate.interest(any(BigDecimal.class), anyInt(), anyBoolean()));

		for (final double payment : payments) {
			interest = interest.thenReturn(BigDecimal.valueOf(payment));
		}
	}

	private void verifyNoInterestRateCalculation() {
		verifyNoMoreInteractions(rate);
	}

	private void credit( final double amount ) {
		account.credit(BigDecimal.valueOf(amount), ACCOUNT_OPEN_DATE);
	}

	private void verifyBalance( final double expectedBalance ) {
		assertEquals(String.format("Balance expected %s != %s", expectedBalance, account.getBalance()), 0,
		        BigDecimal.valueOf(expectedBalance).compareTo(account.getBalance()));
	}

	private void deposit( final double amount ) {
		account.deposit(BigDecimal.valueOf(amount), ACCOUNT_OPEN_DATE);
	}

	private void debit( final double amount ) throws InsufficientFundsException {
		account.debit(BigDecimal.valueOf(amount), ACCOUNT_OPEN_DATE);
	}

	private void update( final LocalDate tradingDate ) {
		account.update(tradingDate);
	}
}