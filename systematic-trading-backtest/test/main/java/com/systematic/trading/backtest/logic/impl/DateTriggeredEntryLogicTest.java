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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.backtest.order.impl.BuyTomorrowAtOpeningPriceOrder;
import com.systematic.trading.data.DataPoint;

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

	@Test
	public void actionOnInsufficentFunds() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );

		final EquityOrder order = mock( EquityOrder.class );

		final EquityOrderInsufficientFundsAction action = logic.actionOnInsufficentFunds( order );

		assertEquals( EquityOrderInsufficientFundsAction.RESUMIT, action );
	}

	@Test
	public void updateNoOrder() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );
		when( data.getDate() ).thenReturn( firstOrder );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
	}

	@Test
	public void updateOrder() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 1 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		assertEquals( EquityOrderVolume.valueOf( amount ).getVolume(), buyTomorrowOrder.getVolume().getVolume() );
	}

	@Test
	public void updateBuyTwoDaysNoOrder() {
		final LocalDate firstOrder = LocalDate.now();
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNull( order );
	}

	@Test
	public void updateBuyTwoDaysWithOrder() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		assertEquals( EquityOrderVolume.valueOf( amount ).getVolume(), buyTomorrowOrder.getVolume().getVolume() );
	}

	@Test
	public void updateBuyTwoDaysRolling() {
		final LocalDate firstOrder = LocalDate.now().minus( Period.ofDays( 1 ) );
		final Period interval = Period.ofDays( 2 );
		final BigDecimal amount = BigDecimal.ONE;
		final DateTriggeredEntryLogic logic = new DateTriggeredEntryLogic( firstOrder, interval, amount, recorder );
		when( data.getDate() ).thenReturn( LocalDate.now() );

		final EquityOrder order = logic.update( fees, cashAccount, data );

		assertNotNull( order );
		assertTrue( order instanceof BuyTomorrowAtOpeningPriceOrder );

		final BuyTomorrowAtOpeningPriceOrder buyTomorrowOrder = (BuyTomorrowAtOpeningPriceOrder) order;
		assertEquals( EquityOrderVolume.valueOf( amount ).getVolume(), buyTomorrowOrder.getVolume().getVolume() );

		// Change the trading day to tomorrow - should not have an order
		when( data.getDate() ).thenReturn( LocalDate.now().plus( Period.ofDays( 1 ) ) );

		final EquityOrder secondOrder = logic.update( fees, cashAccount, data );

		assertNull( secondOrder );
	}
}