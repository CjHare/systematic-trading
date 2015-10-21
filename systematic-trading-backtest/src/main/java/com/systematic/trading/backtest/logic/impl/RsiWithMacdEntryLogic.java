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
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.collections.LimitedQueue;
import com.systematic.trading.backtest.event.OrderEvent.EquityOrderType;
import com.systematic.trading.backtest.event.impl.PlaceOrderTotalCostEvent;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.impl.BuyTotalCostTomorrowAtOpeningPriceOrder;
import com.systematic.trading.backtest.util.LimitedLinkedList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.recorder.EventRecorder;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.StochasticOscillatorSignals;
import com.systematic.trading.signals.model.AnalysisLongBuySignals;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.BuySignalDateComparator;
import com.systematic.trading.signals.model.configuration.AllSignalsConfiguration;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.RsiMacdOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;

/**
 * Entry logic using a combination of the stochastic, relative strength index and moving average,
 * convergence, divergence indicators.
 * 
 * @author CJ Hare
 */
public class RsiWithMacdEntryLogic implements EntryLogic {

	/** Default ordering of signals. */
	private static final BuySignalDateComparator ORDER_BY_DATE = new BuySignalDateComparator();

	/** Record keeper for transactions from the Cash Account. */
	private final EventRecorder event;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The type of equity being traded. */
	private final EquityClass type;

	/** Generates buy signals. */
	private final AnalysisLongBuySignals buyLongAnalysis;

	/** The trading data as it rolled through the set. */
	private final Queue<TradingDayPrices> tradingData;

	/** Signals that have already been submitted as orders. */
	private final Queue<BuySignal> previousSignals;

	/** Minimum value of the trade excluding the fee amount */
	private final BigDecimal minimumTradeValue;

	/**
	 * @param event recorder of the order creation.
	 * @param interval time between creation of entry orders.
	 * @param mathContext scale and precision to apply to mathematical operations.
	 */
	public RsiWithMacdEntryLogic( final EventRecorder event, final EquityClass equityType,
			final BigDecimal minimumTradeValue, final MathContext mathContext ) {
		this.event = event;
		this.mathContext = mathContext;
		this.type = equityType;
		this.minimumTradeValue = minimumTradeValue;

		// TODO remove magic numbers
		this.previousSignals = new LimitedLinkedList<BuySignal>( 20 );

		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 70, 30 );
		final MovingAveragingConvergeDivergenceSignals macd = new MovingAveragingConvergeDivergenceSignals( 10, 20, 7 );
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 10, 3, 3 );
		final LongBuySignalConfiguration configuration = new AllSignalsConfiguration( rsi, macd, stochastic );
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();

		// Only signals from the last two days are of interest
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new RsiMacdOnSameDaySignalFilter(),
				Period.ofDays( 5 ) );
		filters.add( filter );

		this.buyLongAnalysis = new AnalysisLongBuySignals( configuration, filters );

		// TODO 200 value should be the largest data needed for the indicators :. calculate
		this.tradingData = new LimitedQueue<TradingDayPrices>( 200 );
	}

	@Override
	public EquityOrder update( final BrokerageFees fees, final CashAccount cashAccount, final TradingDayPrices data ) {

		// Add the day's data to the rolling queue
		tradingData.add( data );

		// Create signals from the available trading data
		final List<BuySignal> signals;
		try {
			signals = buyLongAnalysis.analyse( tradingData.toArray( new TradingDayPrices[0] ), ORDER_BY_DATE );
		} catch (final TooFewDataPoints e) {
			// Until there are enough data points, no signals can be generated
			return null;
		}

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
			event.record( new PlaceOrderTotalCostEvent( amount, tradingDate, EquityOrderType.ENTRY ) );
		}

		return new BuyTotalCostTomorrowAtOpeningPriceOrder( amount, type, mathContext );
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return EquityOrderInsufficientFundsAction.DELETE;
	}
}
