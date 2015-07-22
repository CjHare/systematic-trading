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
package com.systematic.trading.backtest.order.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.BrokerageTransaction;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.exception.OrderException;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.price.OpeningPrice;

/**
 * Buy tomorrow at the opening price.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class BuyTomorrowAtOpeningPriceOrderTest {

	@Mock
	private DataPoint todaysTrading;

	@Test
	public void isValid() {
		final BuyTomorrowAtOpeningPriceOrder buy = new BuyTomorrowAtOpeningPriceOrder(
				EquityOrderVolume.valueOf( BigDecimal.ONE ) );

		final boolean isValid = buy.isValid( todaysTrading );

		assertEquals( true, isValid );
	}

	@Test
	public void areExecutionConditionsMet() {
		final BuyTomorrowAtOpeningPriceOrder buy = new BuyTomorrowAtOpeningPriceOrder(
				EquityOrderVolume.valueOf( BigDecimal.ONE ) );

		final boolean areConditionMet = buy.areExecutionConditionsMet( todaysTrading );

		assertEquals( true, areConditionMet );
	}

	@Test
	public void execute() throws OrderException {
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.ONE );
		final BrokerageTransaction broker = mock( BrokerageTransaction.class );
		final CashAccount cashAccount = mock( CashAccount.class );
		final BuyTomorrowAtOpeningPriceOrder buy = new BuyTomorrowAtOpeningPriceOrder( volume );

		final OpeningPrice price = mock( OpeningPrice.class );
		when( todaysTrading.getOpeningPrice() ).thenReturn( price );

		final LocalDate date = LocalDate.now();
		when( todaysTrading.getDate() ).thenReturn( date );

		buy.execute( broker, cashAccount, todaysTrading );

		verify( broker ).buy( price, volume, date );
		verify( todaysTrading ).getDate();
		verify( todaysTrading ).getOpeningPrice();
	}
}
