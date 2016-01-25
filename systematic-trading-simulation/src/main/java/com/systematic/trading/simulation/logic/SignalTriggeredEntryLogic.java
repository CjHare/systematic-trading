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
import java.util.List;

import com.systematic.trading.collection.LimitedSizeQueue;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;

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

	/** The number of decimal places the equity is traded in. */
	private final int scale;

	/** Generates buy signals. */
	private final AnalysisBuySignals buyLongAnalysis;

	/** The trading data as it rolled through the set. */
	private final LimitedSizeQueue<TradingDayPrices> tradingData;

	/** Signals that have already been submitted as orders. */
	private final LimitedSizeQueue<BuySignal> previousSignals;

	/** Minimum value of the trade excluding the fee amount */
	private final TradeValue tradeValue;

	/**
	 * @param interval time between creation of entry orders.
	 * @param analysis analyser of trading data to generate buy signals.
	 * @param mathContext scale and precision to apply to mathematical operations.
	 */
	public SignalTriggeredEntryLogic( final EquityClass equityType, final int equityScale, final TradeValue tradeValue,
			final AnalysisBuySignals analysis, final MathContext mathContext ) {
		this.buyLongAnalysis = analysis;
		this.mathContext = mathContext;
		this.tradeValue = tradeValue;
		this.scale = equityScale;
		this.type = equityType;

		this.tradingData = new LimitedSizeQueue<TradingDayPrices>( TradingDayPrices.class,
				analysis.getMaximumNumberOfTradingDaysRequired() );

		// There can only ever be as many signals as trading days stored
		this.previousSignals = new LimitedSizeQueue<BuySignal>( BuySignal.class,
				analysis.getMaximumNumberOfTradingDaysRequired() );
	}

	@Override
	public EquityOrder update( final BrokerageTransactionFee fees, final CashAccount cashAccount,
			final TradingDayPrices data ) {

		// Add the day's data to the rolling queue
		tradingData.add( data );

		// Create signals from the available trading data
		final List<BuySignal> signals = buyLongAnalysis.analyse( tradingData.toArray() );

		if (!signals.isEmpty()) {

			// Only one order at a day
			final BuySignal signal = signals.get( 0 );

			if (!previousSignals.contains( signal )) {

				// Order placed, put on the ignore list
				previousSignals.add( signal );

				final BigDecimal amount = getTradeAmount( cashAccount );
				return createOrder( fees, amount, data );
			}
		}

		return null;
	}

	private BigDecimal getTradeAmount( final CashAccount cashAccount ) {
		final BigDecimal availableFunds = cashAccount.getBalance();
		return tradeValue.getTradeValue( availableFunds );
	}

	private EquityOrder createOrder( final BrokerageTransactionFee fees, final BigDecimal amount,
			final TradingDayPrices data ) {

		final LocalDate tradingDate = data.getDate();
		final BigDecimal maximumTransactionCost = fees.calculateFee( amount, type, tradingDate );
		final BigDecimal closingPrice = data.getClosingPrice().getPrice();
		final BigDecimal numberOfEquities = amount.subtract( maximumTransactionCost, mathContext )
				.divide( closingPrice, mathContext ).setScale( scale, BigDecimal.ROUND_DOWN );

		if (numberOfEquities.compareTo( BigDecimal.ZERO ) > 0) {
			return new BuyTotalCostTomorrowAtOpeningPriceOrder( amount, type, scale, tradingDate, mathContext );
		}

		return null;
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return EquityOrderInsufficientFundsAction.DELETE;
	}

	@Override
	public void addListener( final SignalAnalysisListener listener ) {
		buyLongAnalysis.addListener( listener );
	}
}
