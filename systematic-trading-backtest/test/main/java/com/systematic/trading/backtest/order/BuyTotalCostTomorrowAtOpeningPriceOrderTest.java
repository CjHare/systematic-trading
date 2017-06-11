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
package com.systematic.trading.backtest.order;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.matcher.EquityOrderVolumeMatcher;
import com.systematic.trading.backtest.matcher.PriceMatcher;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.OpeningPrice;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.brokerage.BrokerageTransaction;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.simulation.order.exception.OrderException;

/**
 * Buy tomorrow at the opening price.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class BuyTotalCostTomorrowAtOpeningPriceOrderTest {
	private static final LocalDate TODAY = LocalDate.now();

	@Mock
	private TradingDayPrices todaysTrading;

	@Mock
	private BrokerageTransaction broker;

	@Mock
	private BrokerageTransactionFee fees;

	@Mock
	private CashAccount cashAccount;

	private BuyTotalCostTomorrowAtOpeningPriceOrder order;

	@Before
	public void setUp() {
		final BigDecimal targetTotalCost = BigDecimal.valueOf(44);
		final int equityDecimalPlaces = 4;
		order = new BuyTotalCostTomorrowAtOpeningPriceOrder(targetTotalCost, EquityClass.STOCK, equityDecimalPlaces,
		        LocalDate.now(), MathContext.DECIMAL64);
	}

	@Test
	public void execute() throws OrderException {
		setUpTradingPrices(5);
		setUpFeeCalculation(3);

		executeOrder();

		verifyBuyOrderPlaced(5, 8.2);
	}

	@Test
	public void isValid() {
		final boolean isValid = order.isValid(todaysTrading);

		assertEquals(true, isValid);
	}

	@Test
	public void areExecutionConditionsMet() {
		final boolean areConditionMet = order.areExecutionConditionsMet(todaysTrading);

		assertEquals(true, areConditionMet);
	}

	private void executeOrder() throws OrderException {
		order.execute(fees, broker, cashAccount, todaysTrading);
	}

	private void setUpFeeCalculation( final double fee ) {
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(fee));
	}

	private void setUpTradingPrices( final double equityPrice ) {
		final OpeningPrice openingPrice = mock(OpeningPrice.class);
		when(openingPrice.getPrice()).thenReturn(BigDecimal.valueOf(equityPrice));
		when(todaysTrading.getOpeningPrice()).thenReturn(openingPrice);

		when(todaysTrading.getDate()).thenReturn(TODAY);
	}

	private void verifyBuyOrderPlaced( final double equityPrice, final double volume ) {
		verify(broker).buy(PriceMatcher.argumentMatches(equityPrice), EquityOrderVolumeMatcher.argumentMatches(volume),
		        eq(TODAY));
		verify(todaysTrading, atLeastOnce()).getDate();
		verify(todaysTrading, atLeastOnce()).getOpeningPrice();
	}
}