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
package com.systematic.trading.strategy.entry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.brokerage.BrokerageTransaction;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.brokerage.exception.InsufficientEquitiesException;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderVolume;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEvent.EquityOrderType;
import com.systematic.trading.simulation.order.event.PlaceOrderTotalCostEvent;

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

	/** The number of decimal places the equity is trading in. */
	private final int scale;

	public BuyTotalCostTomorrowAtOpeningPriceOrder( final BigDecimal targetTotalCost, final EquityClass type,
	        final int equityScale, final LocalDate creationDate, final MathContext mathContext ) {
		this.targetTotalCost = targetTotalCost;
		this.creationDate = creationDate;
		this.mathContext = mathContext;
		this.scale = equityScale;
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
	public void execute( final BrokerageTransactionFee fees, final BrokerageTransaction broker,
	        final CashAccount cashAccount, final TradingDayPrices todaysPrice )
	        throws InsufficientEquitiesException, InsufficientFundsException {

		final EquityOrderVolume volume = getOrderVolume(fees, todaysPrice);
		debitEquityCost(cashAccount, todaysPrice, equityCost(broker, todaysPrice, volume));
		addEquities(broker, todaysPrice, volume);
	}

	@Override
	public OrderEvent orderEvent() {
		return new PlaceOrderTotalCostEvent(targetTotalCost, creationDate, EquityOrderType.ENTRY);
	}

	private BigDecimal equityCost( final BrokerageTransaction broker, final TradingDayPrices todaysPrice,
	        final EquityOrderVolume volume ) {
		return broker.calculateBuy(todaysPrice.openingPrice(), volume, todaysPrice.date());
	}

	private void debitEquityCost( final CashAccount cashAccount, final TradingDayPrices todaysPrice,
	        final BigDecimal actualTotalCost ) throws InsufficientFundsException {
		cashAccount.debit(actualTotalCost, todaysPrice.date());
	}

	private void addEquities( final BrokerageTransaction broker, final TradingDayPrices todaysPrice,
	        final EquityOrderVolume volume ) {
		broker.buy(todaysPrice.openingPrice(), volume, todaysPrice.date());
	}

	private EquityOrderVolume getOrderVolume( final BrokerageTransactionFee fees, final TradingDayPrices todaysPrice ) {
		final BigDecimal maximumTransactionCost = fees.calculateFee(targetTotalCost, type, todaysPrice.date());
		final BigDecimal openingPrice = todaysPrice.openingPrice().getPrice();
		final BigDecimal numberOfEquities = targetTotalCost.subtract(maximumTransactionCost, mathContext)
		        .divide(openingPrice, mathContext);
		return getOrderVolume(numberOfEquities);
	}

	private EquityOrderVolume getOrderVolume( final BigDecimal numberOfEquities ) {
		return EquityOrderVolume.valueOf(numberOfEquities.setScale(scale, BigDecimal.ROUND_DOWN));
	}
}