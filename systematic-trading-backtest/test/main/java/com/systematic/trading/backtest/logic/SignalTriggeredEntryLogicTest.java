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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
import com.systematic.trading.simulation.logic.trade.TradeValueLogic;
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

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	private static final EquityClass EQUITY_STOCK = EquityClass.STOCK;
	private static final int EQUITY_SCALE = 4;

	@Mock
	private BrokerageTransactionFee fees;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private TradingDayPrices data;

	@Mock
	private AnalysisBuySignals buyLongAnalysis;

	@Test
	public void actionOnInsufficientFunds() {
		final TradeValueLogic tradeValue = new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		        new RelativeTradeValueCalculator(BigDecimal.ONE, MATH_CONTEXT));
		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic(EQUITY_STOCK, EQUITY_SCALE, tradeValue,
		        buyLongAnalysis, MATH_CONTEXT);

		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds(mock(EquityOrder.class));

		assertEquals(EquityOrderInsufficientFundsAction.DELETE, action);
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verifyNoMoreInteractions(buyLongAnalysis);
		verifyZeroInteractions(fees);
		verifyZeroInteractions(cashAccount);
	}

	@Test
	public void updateNoOrder() {
		final TradeValueLogic tradeValue = new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		        new RelativeTradeValueCalculator(BigDecimal.ONE, MATH_CONTEXT));
		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic(EQUITY_STOCK, EQUITY_SCALE, tradeValue,
		        buyLongAnalysis, MATH_CONTEXT);

		final EquityOrder order = logic.update(fees, cashAccount, data);

		assertNull(order);
		verify(buyLongAnalysis).analyse(any(TradingDayPrices[].class));
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verifyNoMoreInteractions(buyLongAnalysis);
		verifyZeroInteractions(fees);
		verifyZeroInteractions(cashAccount);
	}

	@Test
	public void updateOrderNotCreatedTooFewFundsToBuyStock() {
		when(buyLongAnalysis.getMaximumNumberOfTradingDaysRequired()).thenReturn(10);
		when(data.getClosingPrice()).thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(101)));

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf(1);
		final TradeValueLogic tradeValue = new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		        new RelativeTradeValueCalculator(BigDecimal.ONE, MATH_CONTEXT));

		when(cashAccount.getBalance()).thenReturn(accountBalance);
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(2.25));
		when(data.getDate()).thenReturn(now);
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add(new BuySignal(now));
		when(buyLongAnalysis.analyse(any(TradingDayPrices[].class))).thenReturn(expected);

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic(EQUITY_STOCK, EQUITY_SCALE, tradeValue,
		        buyLongAnalysis, MATH_CONTEXT);

		final EquityOrder order = logic.update(fees, cashAccount, data);

		assertNull(order);
		verify(buyLongAnalysis).analyse(any(TradingDayPrices[].class));
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verify(fees).calculateFee(accountBalance, EquityClass.STOCK, now);
		verifyNoMoreInteractions(buyLongAnalysis);
		verifyNoMoreInteractions(fees);
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}

	@Test
	public void updateOrderCreated() {

		when(buyLongAnalysis.getMaximumNumberOfTradingDaysRequired()).thenReturn(10);
		when(data.getClosingPrice()).thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(101)));

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf(50);
		final TradeValueLogic tradeValue = new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		        new RelativeTradeValueCalculator(BigDecimal.ONE, MATH_CONTEXT));

		when(cashAccount.getBalance()).thenReturn(accountBalance);
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(2.25));
		when(data.getDate()).thenReturn(now);
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add(new BuySignal(now));
		when(buyLongAnalysis.analyse(any(TradingDayPrices[].class))).thenReturn(expected);

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic(EQUITY_STOCK, EQUITY_SCALE, tradeValue,
		        buyLongAnalysis, MATH_CONTEXT);

		final EquityOrder order = logic.update(fees, cashAccount, data);

		assertNotNull(order);
		assertEquals(true, order instanceof BuyTotalCostTomorrowAtOpeningPriceOrder);
		verify(buyLongAnalysis).analyse(any(TradingDayPrices[].class));
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verify(fees).calculateFee(accountBalance, EquityClass.STOCK, now);
		verifyNoMoreInteractions(buyLongAnalysis);
		verify(fees).calculateFee(accountBalance, EquityClass.STOCK, now);
		verifyNoMoreInteractions(fees);
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}

	@Test
	public void updateOrderNotCreatedPreviouslyTriggered() {

		when(buyLongAnalysis.getMaximumNumberOfTradingDaysRequired()).thenReturn(10);
		when(data.getClosingPrice()).thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(101)));

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf(50);
		final TradeValueLogic tradeValue = new BoundedTradeValue(new AbsoluteTradeValueCalculator(BigDecimal.ONE),
		        new RelativeTradeValueCalculator(BigDecimal.ONE, MATH_CONTEXT));

		when(cashAccount.getBalance()).thenReturn(accountBalance);
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(2.25));
		when(data.getDate()).thenReturn(now);
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add(new BuySignal(now));
		when(buyLongAnalysis.analyse(any(TradingDayPrices[].class))).thenReturn(expected);

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic(EQUITY_STOCK, EQUITY_SCALE, tradeValue,
		        buyLongAnalysis, MATH_CONTEXT);

		logic.update(fees, cashAccount, data);
		final EquityOrder order = logic.update(fees, cashAccount, data);

		assertNull(order);
		verify(buyLongAnalysis, times(2)).analyse(any(TradingDayPrices[].class));
		verify(buyLongAnalysis, atLeastOnce()).getMaximumNumberOfTradingDaysRequired();
		verify(fees).calculateFee(accountBalance, EquityClass.STOCK, now);
		verifyNoMoreInteractions(buyLongAnalysis);
		verify(fees).calculateFee(accountBalance, EquityClass.STOCK, now);
		verifyNoMoreInteractions(fees);
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}
}
