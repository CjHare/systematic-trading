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
package com.systematic.trading.simulation.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.matcher.BigDecimalMatcher;
import com.systematic.trading.simulation.order.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;

/**
 * Entry logic triggered by date.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class DateTriggeredEntryLogicTest {
	private static final int EQUITY_SCALE = 4;
	private static final Period ORDER_EVERY_DAY = Period.ofDays(1);
	private static final Period ORDER_EVERY_OTHER_DAY = Period.ofDays(2);
	private static final LocalDate YESTERDAY = LocalDate.now().minus(Period.ofDays(1));
	private static final LocalDate TODAY = LocalDate.now();
	private static final LocalDate TOMORROW = LocalDate.now().plus(Period.ofDays(1));

	@Mock
	private TradingDayPrices data;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private BrokerageTransactionFee fees;

	/** Entry logic instance created in the setUpEntryLogic.*/
	private DateTriggeredEntryLogic logic;

	/* The most recent update response.*/
	private EquityOrder order;

	@Before
	public void setUp() {
		logic = null;
		order = null;
	}

	@Test
	public void updateNoOrder() {
		setUpEntryLogic();
		setUpData(20, TODAY);

		update();

		verifyNoOrder();
		verifyNoCashAccountActions();
		verifyNoFeeCalculations();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateOrder() {
		setUpEntryLogic(YESTERDAY, ORDER_EVERY_DAY);
		setUpData(20, TODAY);
		setUpFeeCalculation(5);
		setUpCashAccount(100);

		update();

		verifyOrder();
		verifyCashAccountBalanceCheck();
		verifyFee(calculation(100, TODAY));
	}

	@SuppressWarnings("unchecked")
	@Test
	/**
	 * When the fees are higher then the order amount, there should be no order
	 */
	public void updateOrderMinimumOfZero() {
		setUpEntryLogic(YESTERDAY, ORDER_EVERY_DAY);
		setUpData(1, TODAY);
		setUpFeeCalculation(5);
		setUpCashAccount(4);

		update();

		verifyNoOrder();
		verifyCashAccountBalanceCheck();
		verifyFee(calculation(4, TODAY));
	}

	@Test
	public void updateBuyTwoDaysNoOrder() {
		setUpEntryLogic(TODAY, ORDER_EVERY_OTHER_DAY);
		setUpData(1, TODAY);

		update();

		verifyNoOrder();
		verifyNoCashAccountActions();
		verifyNoFeeCalculations();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateBuyTwoDaysWithOrder() {
		setUpEntryLogic(YESTERDAY, ORDER_EVERY_OTHER_DAY);
		setUpData(20, TODAY);
		setUpFeeCalculation(5);
		setUpCashAccount(100);

		update();

		verifyOrder();
		verifyCashAccountBalanceCheck();
		verifyFee(calculation(100, TODAY));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateBuyTwoDaysRolling() {
		setUpEntryLogic(YESTERDAY, ORDER_EVERY_OTHER_DAY);
		setUpData(20, TODAY);
		setUpFeeCalculation(5);
		setUpCashAccount(100);

		update();

		verifyOrder();

		// Change the trading day to tomorrow - should not have an order
		setUpData(18, TOMORROW);

		update();

		verifyNoOrder();
		verifyCashAccountBalanceCheck();
		verifyFee(calculation(100, TODAY));
	}

	@SuppressWarnings("unchecked")
	@Test
	/**
	 * Make sure there is no drift in the order date.
	 */
	public void updateEnsureNoDrift() {
		setUpEntryLogic(YESTERDAY, ORDER_EVERY_DAY);
		setUpData(20, TODAY);
		setUpFeeCalculation(5);
		setUpCashAccount(100);

		update();

		verifyOrder();

		// Change the trading day to tomorrow - should not have an order
		setUpData(18, TOMORROW);

		update();

		verifyOrder();
		verifyCashAccountBalanceCheck();
		verifyFee(calculation(100, TODAY), calculation(100, TOMORROW));
	}

	@Test
	public void actionOnInsufficentFunds() {
		setUpEntryLogic();

		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds(mock(EquityOrder.class));

		assertEquals(EquityOrderInsufficientFundsAction.RESUMIT, action);
	}

	private void verifyCashAccountBalanceCheck() {
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}

	private Pair<Double, LocalDate> calculation( final double tradeValue, final LocalDate tradeDate ) {
		return new ImmutablePair<Double, LocalDate>(tradeValue, tradeDate);
	}

	@SuppressWarnings("unchecked")
	private void verifyFee( final Pair<Double, LocalDate>... tradeValuesAndDates ) {
		InOrder order = inOrder(fees);

		for (final Pair<Double, LocalDate> tradeValueAndDate : tradeValuesAndDates) {
			order.verify(fees).calculateFee(BigDecimalMatcher.argumentMatches(tradeValueAndDate.getLeft()),
			        eq(EquityClass.STOCK), eq(tradeValueAndDate.getRight()));
		}

		verifyNoMoreInteractions(fees);
	}

	private void setUpCashAccount( final double balance ) {
		when(cashAccount.getBalance()).thenReturn(BigDecimal.valueOf(balance));
	}

	private void setUpFeeCalculation( final double transactionCost ) {
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(transactionCost));
	}

	private void setUpData( final double closingPrice, final LocalDate date ) {
		when(data.getDate()).thenReturn(date);
		when(data.getClosingPrice()).thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(closingPrice)));
	}

	private void verifyOrder() {
		assertNotNull(order);
		assertTrue(order instanceof BuyTotalCostTomorrowAtOpeningPriceOrder);
	}

	private void verifyNoOrder() {
		assertNull(order);
	}

	private void update() {
		order = logic.update(fees, cashAccount, data);
	}

	private void setUpEntryLogic() {
		setUpEntryLogic(LocalDate.now(), Period.ofDays(1));
	}

	private void setUpEntryLogic( final LocalDate firstOrder, final Period interval ) {
		logic = new DateTriggeredEntryLogic(EquityClass.STOCK, EQUITY_SCALE, firstOrder, interval);
	}

	private void verifyNoCashAccountActions() {
		verifyZeroInteractions(cashAccount);
	}

	private void verifyNoFeeCalculations() {
		verifyZeroInteractions(fees);
	}
}