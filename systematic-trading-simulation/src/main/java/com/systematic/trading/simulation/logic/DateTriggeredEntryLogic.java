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
package com.systematic.trading.simulation.logic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.brokerage.BrokerageFees;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;

/**
 * Frequent purchases of a fixed amount at regular intervals.
 * 
 * @author CJ Hare
 */
public class DateTriggeredEntryLogic implements EntryLogic {

	/** Time between creation of entry orders. */
	private final Period interval;

	/** Amount to buy in with. */
	private final BigDecimal amount;

	/** The last date purchase order was created. */
	private LocalDate lastOrder;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The type of equity being traded. */
	private final EquityClass type;

	/**
	 * @param amount total to spend on a single purchase order, including fees.
	 * @param firstOrder date to place the first order.
	 * @param interval time between creation of entry orders.
	 * @param mathContext scale and precision to apply to mathematical operations.
	 */
	public DateTriggeredEntryLogic( final BigDecimal amount, final EquityClass equityType, final LocalDate firstOrder,
			final Period interval, final MathContext mathContext ) {
		this.interval = interval;
		this.amount = amount;
		this.mathContext = mathContext;
		this.type = equityType;

		// The first order needs to be on that date, not interval after
		lastOrder = LocalDate.from( firstOrder ).minus( interval );
	}

	@Override
	public EquityOrder update( final BrokerageFees fees, final CashAccount cashAccount, final TradingDayPrices data ) {

		final LocalDate tradingDate = data.getDate();

		if (isOrderTime( tradingDate )) {

			final BigDecimal maximumTransactionCost = fees.calculateFee( amount, type, data.getDate() );
			final BigDecimal closingPrice = data.getClosingPrice().getPrice();
			final BigDecimal numberOfEquities = amount.subtract( maximumTransactionCost, mathContext ).divide(
					closingPrice, mathContext );

			if (numberOfEquities.compareTo( BigDecimal.ZERO ) > 0) {
				lastOrder = tradingDate.minus( Period.ofDays( 1 ) );
				return new BuyTotalCostTomorrowAtOpeningPriceOrder( amount, type, tradingDate, mathContext );
			}
		}

		return null;
	}

	private boolean isOrderTime( final LocalDate tradingDate ) {
		return tradingDate.isAfter( lastOrder.plus( interval ) );
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return EquityOrderInsufficientFundsAction.RESUMIT;
	}
}