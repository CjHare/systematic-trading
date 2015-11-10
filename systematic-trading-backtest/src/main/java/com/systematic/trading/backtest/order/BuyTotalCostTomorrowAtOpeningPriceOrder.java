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
package com.systematic.trading.backtest.order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.BrokerageTransaction;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.OrderEvent;
import com.systematic.trading.backtest.event.PlaceOrderTotalCostEvent;
import com.systematic.trading.backtest.event.OrderEvent.EquityOrderType;
import com.systematic.trading.backtest.exception.OrderException;
import com.systematic.trading.data.TradingDayPrices;

/**
 * Placing an order to purchase an equity.
 * 
 * @author CJ Hare
 */
public class BuyTotalCostTomorrowAtOpeningPriceOrder implements EquityOrder {

	/** Sum of the transaction cost, fees & maximum number of equities. */
	private final BigDecimal targetTotalCost;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The type of equity being traded. */
	private final EquityClass type;

	/** Date on which the order was created. */
	private final LocalDate creationDate;

	public BuyTotalCostTomorrowAtOpeningPriceOrder( final BigDecimal targetTotalCost, final EquityClass type,
			final LocalDate creationDate, final MathContext mathContext ) {
		this.targetTotalCost = targetTotalCost;
		this.creationDate = creationDate;
		this.mathContext = mathContext;
		this.type = type;
	}

	@Override
	public boolean isValid( final TradingDayPrices todaysTrading ) {
		// Never expire
		return true;
	}

	@Override
	public boolean areExecutionConditionsMet( final TradingDayPrices todaysTrading ) {
		// Buy irrespective of the date or price
		return true;
	}

	@Override
	public void execute( final BrokerageFees fees, final BrokerageTransaction broker, final CashAccount cashAccount,
			final TradingDayPrices todaysTrade ) throws OrderException {

		final BigDecimal maximumTransactionCost = fees.calculateFee( targetTotalCost, type, todaysTrade.getDate() );
		final BigDecimal openingPrice = todaysTrade.getOpeningPrice().getPrice();
		final BigDecimal numberOfEquities = targetTotalCost.subtract( maximumTransactionCost, mathContext ).divide(
				openingPrice, mathContext );

		final EquityOrderVolume volume = EquityOrderVolume.valueOf( numberOfEquities );

		final BigDecimal actualTotalCost = broker.calculateBuy( todaysTrade.getOpeningPrice(), volume,
				todaysTrade.getDate() );

		cashAccount.debit( actualTotalCost, todaysTrade.getDate() );

		broker.buy( todaysTrade.getOpeningPrice(), volume, todaysTrade.getDate() );
	}

	@Override
	public OrderEvent getOrderEvent() {
		return new PlaceOrderTotalCostEvent( targetTotalCost, creationDate, EquityOrderType.ENTRY );
	}
}
