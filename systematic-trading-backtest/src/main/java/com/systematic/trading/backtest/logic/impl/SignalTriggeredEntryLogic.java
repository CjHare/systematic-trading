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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Queue;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.collections.LimitedQueue;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.impl.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.model.BuySignal;

/**
 * Entry logic using indicator signals to decide when to create orders.
 * 
 * @author CJ Hare
 */
public class SignalTriggeredEntryLogic implements EntryLogic {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The type of equity being traded. */
	private final EquityClass type;

	/** Generates buy signals. */
	private final AnalysisBuySignals buyLongAnalysis;

	/** The trading data as it rolled through the set. */
	private final Queue<TradingDayPrices> tradingData;

	/** Signals that have already been submitted as orders. */
	private final Queue<BuySignal> previousSignals;

	/** Minimum value of the trade excluding the fee amount */
	private final BigDecimal minimumTradeValue;

	/**
	 * @param interval time between creation of entry orders.
	 * @param buyLongAnalysis analyser of trading data to generate buy signals.
	 * @param mathContext scale and precision to apply to mathematical operations.
	 */
	public SignalTriggeredEntryLogic( final EquityClass equityType, final BigDecimal minimumTradeValue,
			final AnalysisBuySignals buyLongAnalysis, final MathContext mathContext ) {
		this.mathContext = mathContext;
		this.type = equityType;
		this.minimumTradeValue = minimumTradeValue;
		this.buyLongAnalysis = buyLongAnalysis;

		this.tradingData = new LimitedQueue<TradingDayPrices>( buyLongAnalysis.getMaximumNumberOfTradingDaysRequired() );

		// There can only ever be as many signals as trading days stored
		this.previousSignals = new LimitedQueue<BuySignal>( buyLongAnalysis.getMaximumNumberOfTradingDaysRequired() );
	}

	@Override
	public EquityOrder update( final BrokerageFees fees, final CashAccount cashAccount, final TradingDayPrices data ) {

		// Add the day's data to the rolling queue
		tradingData.add( data );

		// Create signals from the available trading data
		final List<BuySignal> signals = buyLongAnalysis.analyse( tradingData.toArray( new TradingDayPrices[0] ) );

		if (!signals.isEmpty()) {

			// Only one order at a day
			final BuySignal signal = signals.get( 0 );

			if (!previousSignals.contains( signal )) {

				if (cashAccount.getBalance().compareTo( minimumTradeValue ) > 0) {

					// Order placed, put on the ignore list
					previousSignals.add( signal );

					// Everything into the trade
					final BigDecimal amount = cashAccount.getBalance();

					return createOrder( fees, amount, data );
				}
			}
		}

		return null;
	}

	private EquityOrder createOrder( final BrokerageFees fees, final BigDecimal amount, final TradingDayPrices data ) {

		final LocalDate tradingDate = data.getDate();
		final BigDecimal maximumTransactionCost = fees.calculateFee( amount, type, tradingDate );
		final BigDecimal closingPrice = data.getClosingPrice().getPrice();
		final BigDecimal numberOfEquities = amount.subtract( maximumTransactionCost, mathContext ).divide(
				closingPrice, mathContext );

		if (numberOfEquities.compareTo( BigDecimal.ZERO ) > 0) {
			return new BuyTotalCostTomorrowAtOpeningPriceOrder( amount, type, tradingDate, mathContext );
		}

		return null;
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return EquityOrderInsufficientFundsAction.DELETE;
	}
}
