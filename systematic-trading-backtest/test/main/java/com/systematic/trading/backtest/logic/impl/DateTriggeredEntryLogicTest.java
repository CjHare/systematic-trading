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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.impl.PlaceOrderEvent;
import com.systematic.trading.backtest.event.impl.PlaceOrderEvent.OrderType;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.backtest.order.impl.BuyTomorrowAtOpeningPriceOrder;
import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.price.ClosingPrice;

/**
 * Entry logic triggered by date.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class DateTriggeredEntryLogicTest {

	@Mock
	private DataPoint data;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private BrokerageFees fees;

	@Mock
	private EventRecorder recorder;

	private static final MathContext context = MathContext.DECIMAL64;

	@Test
	public void actionOnInsufficentFunds() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );

		final EquityOrder order = mock( EquityOrder.class );

		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds( order );

		assertEquals( EquityOrderInsufficientFundsAction.RESUMIT, action );
	}

	@Test
	public void updateNoOrder() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		when( data.getDate() ).thenReturn( firstOrder );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
	}

	@Test
	public void updateOrder() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.valueOf( 100 );
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		final LocalDate date = LocalDate.now();
		when( data.getDate() ).thenReturn( date );

		final BigDecimal closingPrice = BigDecimal.valueOf( 20 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( closingPrice ) );

		final BigDecimal transactionCost = BigDecimal.valueOf( 5 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( transactionCost );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		// Ensure correct volume of orders
		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		final BigDecimal volume = amount.subtract( transactionCost, context ).divide( closingPrice, context );
		assertEquals( volume, buyTomorrowOrder.getVolume().getVolume() );
		verify( recorder ).record( isPlaceOrder( volume, date ) );
	}

	@Test
	/**
	 * When the fees are higher then the order amount, there should be no order
	 */
	public void updateOrderMinimumOfZero() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.valueOf( 4 );
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final BigDecimal closingPrice = BigDecimal.valueOf( 1 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( closingPrice ) );

		final BigDecimal transactionCost = BigDecimal.valueOf( 5 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( transactionCost );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
	}

	@Test
	public void updateBuyTwoDaysNoOrder() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
	}

	@Test
	public void updateBuyTwoDaysWithOrder() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.valueOf( 100 );
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		final LocalDate date = LocalDate.now();
		when( data.getDate() ).thenReturn( date );

		final BigDecimal closingPrice = BigDecimal.valueOf( 20 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( closingPrice ) );

		final BigDecimal transactionCost = BigDecimal.valueOf( 5 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( transactionCost );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		// Ensure correct volume of orders
		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		final BigDecimal volume = amount.subtract( transactionCost, context ).divide( closingPrice, context );
		assertEquals( volume, buyTomorrowOrder.getVolume().getVolume() );
		verify( recorder ).record( isPlaceOrder( volume, date ) );
	}

	@Test
	public void updateBuyTwoDaysRolling() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.valueOf( 100 );
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( amount, recorder, EquityClass.STOCK,
				firstOrder, interval, context );
		final LocalDate date = LocalDate.now();
		when( data.getDate() ).thenReturn( date );

		final BigDecimal closingPrice = BigDecimal.valueOf( 20 );
		when( data.getClosingPrice() ).thenReturn( ClosingPrice.valueOf( closingPrice ) );

		final BigDecimal transactionCost = BigDecimal.valueOf( 5 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), any( LocalDate.class ) ) )
				.thenReturn( transactionCost );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		// Ensure correct volume of orders
		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		final BigDecimal volume = amount.subtract( transactionCost, context ).divide( closingPrice, context );
		assertEquals( volume, buyTomorrowOrder.getVolume().getVolume() );
		verify( recorder ).record( isPlaceOrder( volume, date ) );

		// Change the trading day to tomorrow - should not have an order
		when( data.getDate() ).thenReturn( LocalDate.now().plus( Period.ofDays( 1 ) ) );

		final EquityOrder secondOrder = logic.update( fees, cashAccount, data );

		assertNull( secondOrder );
	}

	private PlaceOrderEvent isPlaceOrder( final BigDecimal volume, final LocalDate date ) {
		return argThat( new IsPlaceOrderArgument( EquityOrderVolume.valueOf( volume ), date, OrderType.ENTRY ) );
	}

	class IsPlaceOrderArgument extends ArgumentMatcher<PlaceOrderEvent> {

		private final OrderType type;
		private final EquityOrderVolume volume;
		private final LocalDate date;

		public IsPlaceOrderArgument( final EquityOrderVolume volume, final LocalDate date, final OrderType type ) {
			this.volume = volume;
			this.date = date;
			this.type = type;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof PlaceOrderEvent) {
				final PlaceOrderEvent event = (PlaceOrderEvent) argument;
				return type == event.getType() && date.equals( event.getDate() )
						&& volume.getVolume().compareTo( event.getVolume().getVolume() ) == 0;
			}

			return false;
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( String.format( "Type: %s, Volume: %s, Date: %s", type, volume, date ) );
		}
	}
}
