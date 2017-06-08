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
package com.systematic.trading.backtest.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BigDecimalMatcher;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.logic.SignalTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.trade.AbsoluteTradeValueCalculator;
import com.systematic.trading.simulation.logic.trade.BoundedTradeValue;
import com.systematic.trading.simulation.logic.trade.RelativeTradeValueCalculator;
import com.systematic.trading.simulation.order.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;

/**
 * Testing the signal generator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SignalTriggeredEntryLogicTest {
	private static final int EQUITY_SCALE = 4;
	private static final LocalDate TODAY = LocalDate.now();

	@Mock
	private BrokerageTransactionFee fees;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private TradingDayPrices data;

	@Mock
	private AnalysisBuySignals buyLongAnalysis;

	/** Entry logic instance being tested.*/
	private SignalTriggeredEntryLogic logic;

	/** The last order returned from the update.*/
	private EquityOrder order;

	@Before
	public void setUp() {
		// Store the previous ten signals
		when(buyLongAnalysis.getMaximumNumberOfTradingDaysRequired()).thenReturn(10);

		logic = new SignalTriggeredEntryLogic(EquityClass.STOCK, EQUITY_SCALE,
		        new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		                new RelativeTradeValueCalculator(BigDecimal.ONE, MathContext.DECIMAL64)),
		        buyLongAnalysis, MathContext.DECIMAL64);
	}

	@Test
	public void actionOnInsufficientFunds() {
		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds(mock(EquityOrder.class));

		assertEquals(EquityOrderInsufficientFundsAction.DELETE, action);
		verifyBuyAnalysis(0);
		verifyNoFeeCalcilations();
		verifyNoCashAccountInterfactions();
	}

	@Test
	public void updateNoOrder() {
		update();

		verifyNoOrder();
		verifyBuyAnalysis(1);
		verifyNoFeeCalcilations();
		verifyNoCashAccountInterfactions();
	}

	@Test
	public void updateOrderNotCreatedTooFewFundsToBuyStock() {
		setUpAnalysisBuySignals(TODAY);
		setUpFeeCalculation(2.25);
		setUpTradingData(101);
		setUpCashBalance(1);

		update();

		verifyNoOrder();
		verifyBuyAnalysis(1);
		verifyCashAccountGetBalance();
		feeCalculation(1);
	}

	@Test
	public void updateOrderCreated() {
		setUpAnalysisBuySignals(TODAY);
		setUpFeeCalculation(2.25);
		setUpTradingData(101);
		setUpCashBalance(50);

		update();

		verifyOrder();
		verifyBuyAnalysis(1);
		verifyCashAccountGetBalance();
		feeCalculation(50, 50);
	}

	@Test
	public void updateOrderNotCreatedPreviouslyTriggered() {
		setUpAnalysisBuySignals(TODAY);
		setUpFeeCalculation(2.25);
		setUpTradingData(101);
		setUpCashBalance(50);

		update();

		verifyOrder();

		update();

		verifyNoOrder();
		verifyBuyAnalysis(2);
		verifyCashAccountGetBalance();
		feeCalculation(50, 50);
	}

	private void feeCalculation( final double... transactionFees ) {
		for (final double transactionFee : transactionFees) {
			verify(fees).calculateFee(BigDecimalMatcher.argumentMatches(transactionFee), eq(EquityClass.STOCK),
			        eq(TODAY));
		}

		verifyNoMoreInteractions(fees);
	}

	private void verifyCashAccountGetBalance() {
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}

	private void setUpFeeCalculation( final double fee ) {
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(fee));
	}

	private void setUpAnalysisBuySignals( final LocalDate... buySignals ) {
		final List<BuySignal> expected = new ArrayList<BuySignal>();

		for (final LocalDate buySignal : buySignals) {
			expected.add(new BuySignal(buySignal));
		}

		when(buyLongAnalysis.analyse(any(TradingDayPrices[].class))).thenReturn(expected);
	}

	private void update() {
		order = logic.update(fees, cashAccount, data);
	}

	private void verifyNoOrder() {
		assertNull(order);
	}

	private void verifyOrder() {
		assertNotNull(order);
		assertEquals(true, order instanceof BuyTotalCostTomorrowAtOpeningPriceOrder);
	}

	private void setUpTradingData( final double price ) {
		when(data.getClosingPrice()).thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(price)));
		when(data.getDate()).thenReturn(TODAY);
	}

	private void setUpCashBalance( final double balance ) {
		when(cashAccount.getBalance()).thenReturn(BigDecimal.valueOf(balance));
	}

	private void verifyBuyAnalysis( final int expectedAnalysisCount ) {
		verify(buyLongAnalysis, times(expectedAnalysisCount)).analyse(any(TradingDayPrices[].class));
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verifyNoMoreInteractions(buyLongAnalysis);
	}

	private void verifyNoFeeCalcilations() {
		verifyZeroInteractions(fees);
	}

	private void verifyNoCashAccountInterfactions() {
		verifyZeroInteractions(cashAccount);
	}
}