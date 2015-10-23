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
package com.systematic.trading.backtest.logic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
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
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.OrderEvent.EquityOrderType;
import com.systematic.trading.backtest.event.impl.PlaceOrderTotalCostEvent;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.impl.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.event.recorder.EventRecorder;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.model.BuySignal;

/**
 * Testing the signal generator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SignalTriggeredEntryLogicTest {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	private static final EquityClass EQUITY_STOCK = EquityClass.STOCK;

	@Mock
	private EventRecorder event;

	@Mock
	private BrokerageFees fees;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private TradingDayPrices data;

	@Mock
	private AnalysisBuySignals buyLongAnalysis;

	@Test
	public void actionOnInsufficientFunds() {
		final BigDecimal minimumTradeValue = BigDecimal.ONE;
		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, minimumTradeValue,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds( mock( EquityOrder.class ) );

		assertEquals( EquityOrderInsufficientFundsAction.DELETE, action );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verifyNoMoreInteractions( buyLongAnalysis );
		verifyZeroInteractions( event );
		verifyZeroInteractions( fees );
		verifyZeroInteractions( cashAccount );
	}

	@Test
	public void updateNoOrder() throws TooFewDataPoints {
		final BigDecimal minimumTradeValue = BigDecimal.ONE;
		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, minimumTradeValue,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
		verify( buyLongAnalysis ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verifyNoMoreInteractions( buyLongAnalysis );
		verifyZeroInteractions( event );
		verifyZeroInteractions( fees );
		verifyZeroInteractions( cashAccount );
	}

	@Test
	public void updateTooFewDataPoints() throws TooFewDataPoints {
		when( buyLongAnalysis.analyse( any( TradingDayPrices[].class ) ) ).thenThrow(
				new TooFewDataPoints( "expected exception" ) );

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, BigDecimal.ZERO,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
		verify( buyLongAnalysis ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verifyNoMoreInteractions( buyLongAnalysis );
		verifyZeroInteractions( event );
		verifyZeroInteractions( fees );
		verifyZeroInteractions( cashAccount );
	}

	@Test
	public void updateOrderNotCreatedTooFewFundsToBuyStock() throws TooFewDataPoints {
		when( buyLongAnalysis.getMaximumNumberOfTradingDays() ).thenReturn( 10 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( BigDecimal.valueOf( 101 ) ) );

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf( 1 );
		final BigDecimal minimumTradeValue = BigDecimal.ZERO;
		when( cashAccount.getBalance() ).thenReturn( accountBalance );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( BigDecimal.valueOf( 2.25 ) );
		when( data.getDate() ).thenReturn( now );
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add( new BuySignal( now ) );
		when( buyLongAnalysis.analyse( any( TradingDayPrices[].class ) ) ).thenReturn( expected );

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, minimumTradeValue,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
		verify( buyLongAnalysis ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verify( fees ).calculateFee( accountBalance, EquityClass.STOCK, now );
		verifyNoMoreInteractions( buyLongAnalysis );
		verifyNoMoreInteractions( fees );
		verifyZeroInteractions( event );
		verify( cashAccount, atLeastOnce() ).getBalance();
		verifyNoMoreInteractions( cashAccount );
	}

	@Test
	public void updateOrderNotCreatedFundsBelowMinimumOrderThreshold() throws TooFewDataPoints {
		when( buyLongAnalysis.getMaximumNumberOfTradingDays() ).thenReturn( 10 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( BigDecimal.valueOf( 101 ) ) );

		when( cashAccount.getBalance() ).thenReturn( BigDecimal.ZERO );
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add( new BuySignal( LocalDate.now() ) );
		when( buyLongAnalysis.analyse( any( TradingDayPrices[].class ) ) ).thenReturn( expected );

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, BigDecimal.ONE,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
		verify( buyLongAnalysis ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verifyNoMoreInteractions( buyLongAnalysis );
		verifyZeroInteractions( fees );
		verifyZeroInteractions( event );
		verify( cashAccount, atLeastOnce() ).getBalance();
		verifyNoMoreInteractions( cashAccount );
	}

	@Test
	public void updateOrderCreated() throws TooFewDataPoints {

		when( buyLongAnalysis.getMaximumNumberOfTradingDays() ).thenReturn( 10 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( BigDecimal.valueOf( 101 ) ) );

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf( 50 );
		final BigDecimal minimumTradeValue = BigDecimal.ZERO;
		when( cashAccount.getBalance() ).thenReturn( accountBalance );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( BigDecimal.valueOf( 2.25 ) );
		when( data.getDate() ).thenReturn( now );
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add( new BuySignal( now ) );
		when( buyLongAnalysis.analyse( any( TradingDayPrices[].class ) ) ).thenReturn( expected );

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, minimumTradeValue,
				buyLongAnalysis, MATH_CONTEXT );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertEquals( true, order instanceof BuyTotalCostTomorrowAtOpeningPriceOrder );
		verify( buyLongAnalysis ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verify( fees ).calculateFee( accountBalance, EquityClass.STOCK, now );
		verifyNoMoreInteractions( buyLongAnalysis );
		verify( fees ).calculateFee( accountBalance, EquityClass.STOCK, now );
		verifyNoMoreInteractions( fees );
		verify( event ).record(
				argThat( new PlaceOrderTotalCostEventMatcher( accountBalance, now, EquityOrderType.ENTRY ) ) );
		verifyNoMoreInteractions( event );
		verify( cashAccount, atLeastOnce() ).getBalance();
		verifyNoMoreInteractions( cashAccount );
	}

	@Test
	public void updateOrderNotCreatedPreviouslyTriggered() throws TooFewDataPoints {

		when( buyLongAnalysis.getMaximumNumberOfTradingDays() ).thenReturn( 10 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( BigDecimal.valueOf( 101 ) ) );

		final LocalDate now = LocalDate.now();
		final BigDecimal accountBalance = BigDecimal.valueOf( 50 );
		final BigDecimal minimumTradeValue = BigDecimal.ZERO;
		when( cashAccount.getBalance() ).thenReturn( accountBalance );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( BigDecimal.valueOf( 2.25 ) );
		when( data.getDate() ).thenReturn( now );
		final List<BuySignal> expected = new ArrayList<BuySignal>();
		expected.add( new BuySignal( now ) );
		when( buyLongAnalysis.analyse( any( TradingDayPrices[].class ) ) ).thenReturn( expected );

		final SignalTriggeredEntryLogic logic = new SignalTriggeredEntryLogic( event, EQUITY_STOCK, minimumTradeValue,
				buyLongAnalysis, MATH_CONTEXT );

		logic.update( fees, cashAccount, data );
		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
		verify( buyLongAnalysis, times(2) ).analyse( any( TradingDayPrices[].class ) );
		verify( buyLongAnalysis, atLeastOnce() ).getMaximumNumberOfTradingDays();
		verify( fees ).calculateFee( accountBalance, EquityClass.STOCK, now );
		verifyNoMoreInteractions( buyLongAnalysis );
		verify( fees ).calculateFee( accountBalance, EquityClass.STOCK, now );
		verifyNoMoreInteractions( fees );
		verify( event ).record(
				argThat( new PlaceOrderTotalCostEventMatcher( accountBalance, now, EquityOrderType.ENTRY ) ) );
		verifyNoMoreInteractions( event );
		verify( cashAccount, atLeastOnce() ).getBalance();
		verifyNoMoreInteractions( cashAccount );
	}

	class PlaceOrderTotalCostEventMatcher extends ArgumentMatcher<PlaceOrderTotalCostEvent> {
		final BigDecimal totalCost;
		final LocalDate date;
		final EquityOrderType type;

		public PlaceOrderTotalCostEventMatcher( final BigDecimal totalCost, final LocalDate date,
				final EquityOrderType type ) {
			this.totalCost = totalCost;
			this.date = date;
			this.type = type;
		}

		@Override
		public boolean matches( final Object o ) {

			if (o instanceof PlaceOrderTotalCostEvent) {
				final PlaceOrderTotalCostEvent order = (PlaceOrderTotalCostEvent) o;
				return (totalCost.compareTo( order.getTotalCost() ) == 0) && (date.isEqual( order.getDate() ))
						&& (type.equals( order.getType() ));
			}

			return false;
		}
	}
}
