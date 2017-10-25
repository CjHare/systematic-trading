/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.strategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.simulation.brokerage.BrokerageTransaction;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.strategy.definition.Entry;
import com.systematic.trading.strategy.definition.EntrySize;
import com.systematic.trading.strategy.definition.Exit;
import com.systematic.trading.strategy.definition.ExitSize;
import com.systematic.trading.strategy.definition.Strategy;
import com.systematic.trading.strategy.entry.BuyTotalCostTomorrowAtOpeningPriceOrder;

/**
 * A trading strategy.
 * 
 * @author CJ Hare
 */
public class TradingStrategy implements Strategy {

	//TODO should remove this MC or encapsulate
	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** The type of equity being traded. */
	private final EquityClass type;

	/** The number of decimal places the equity is traded in. */
	private final int scale;

	/** Decides for when an entry trade occurs. */
	private final Entry entry;

	/** Decides when an exit trade occurs. */
	private final Exit exit;

	/** Decides the size of the entry trade. */
	private final EntrySize entryPositionSizing;

	/** Decides the size of the exit trade. */
	private final ExitSize exitPositionSizing;

	public TradingStrategy( final Entry entry, final EntrySize entryPositionSizing, final Exit exit,
	        final ExitSize exitPositionSizing, final EquityClass type, final int scale ) {
		this.entry = entry;
		this.exit = exit;
		this.entryPositionSizing = entryPositionSizing;
		this.exitPositionSizing = exitPositionSizing;
		this.type = type;
		this.scale = scale;
	}

	@Override
	public EquityOrder entryTick( final BrokerageTransactionFee fees, final CashAccount cashAccount,
	        final TradingDayPrices data ) {

		if (entry.entryTick(fees, cashAccount, data)) {

			//TODO do some better encapsulation / refactor
			final BigDecimal amount = entryPositionSizing.entryPositionSize(cashAccount);
			final LocalDate tradingDate = data.getDate();
			final BigDecimal maximumTransactionCost = fees.calculateFee(amount, type, tradingDate);
			final BigDecimal closingPrice = data.getClosingPrice().getPrice();
			final BigDecimal numberOfEquities = amount.subtract(maximumTransactionCost, MATH_CONTEXT)
			        .divide(closingPrice, MATH_CONTEXT).setScale(scale, BigDecimal.ROUND_DOWN);

			if (numberOfEquities.compareTo(BigDecimal.ZERO) > 0) {
				return new BuyTotalCostTomorrowAtOpeningPriceOrder(amount, type, scale, tradingDate, MATH_CONTEXT);
			}
		}

		// No order to place
		return null;
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return entry.actionOnInsufficentFunds(order);
	}

	@Override
	public EquityOrder exitTick( final BrokerageTransaction broker, final TradingDayPrices data ) {

		if (exit.exitTick(broker, data)) {
			throw new UnsupportedOperationException("Implement sell order logic");
		}

		// No order to place
		return null;
	}

	@Override
	public void addListener( final SignalAnalysisListener listener ) {

		//TODO move the listener into the indicator, as they are the ones that fire events
	}
}