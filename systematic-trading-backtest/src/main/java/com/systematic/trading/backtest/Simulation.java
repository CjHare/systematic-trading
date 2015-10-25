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
package com.systematic.trading.backtest;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.recorder.ReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.exception.InsufficientFundsException;
import com.systematic.trading.backtest.exception.OrderException;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.data.TradingDayPrices;

/**
 * The application of the chosen trading logic over a given set of data is performed in the
 * Simulation.
 * <p/>
 * Applies the trading logic over a single equity only.
 * 
 * @author CJ Hare
 */
public class Simulation {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( Simulation.class );

	/** Makes the decision on whether entry action is required. */
	private final EntryLogic entry;

	/** Decision maker on trade exit behaviour. */
	private final ExitLogic exit;

	/** The manager dealing with cash and it's accounting. */
	private final CashAccount funds;

	/** Dealer of equities, manages the equity balance. */
	private final Brokerage broker;

	/** Beginning date of the simulation. */
	private final LocalDate endDate;

	/** Last date of the simulation. */
	private final LocalDate startDate;

	/** The trading data to feed into the simulation. */
	private final Map<LocalDate, TradingDayPrices> tradingData;

	/** Time between deposit events. */
	private final Period interval = Period.ofDays( 1 );

	/** Return on investment calculator. */
	private final ReturnOnInvestmentCalculator roi;

	public Simulation( final LocalDate startDate, final LocalDate endDate, final TradingDayPrices[] unordered,
			final Brokerage broker, final CashAccount funds, final ReturnOnInvestmentCalculator roi,
			final EntryLogic entry, final ExitLogic exit ) {

		this.startDate = startDate;
		this.endDate = endDate;
		this.entry = entry;
		this.exit = exit;
		this.funds = funds;
		this.broker = broker;
		this.roi = roi;

		this.tradingData = new HashMap<LocalDate, TradingDayPrices>();

		for (final TradingDayPrices data : unordered) {
			this.tradingData.put( data.getDate(), data );
		}

		if (this.tradingData.size() != unordered.length) {
			throw new IllegalArgumentException( "Duplicate trading dates provided" );
		}
	}

	public void run() {

		List<EquityOrder> orders = new ArrayList<EquityOrder>();
		LocalDate currentDate = startDate;

		while (currentDate.isBefore( endDate )) {

			// Financial activity of deposits, withdrawal and interest
			funds.update( currentDate );

			final TradingDayPrices currentTradingData = tradingData.get( currentDate );

			// Only when there is trading data for today
			if (currentTradingData != null) {

				// Process orders and add those from the day's trading data
				orders = processTradingData( currentTradingData, orders );

				// Update the return on investment calculator
				roi.update( broker, funds, currentTradingData );
			}

			// Move date to tomorrow
			currentDate = currentDate.plus( interval );
		}
	}

	/**
	 * Processes any outstanding orders when their execution criteria and add any additional orders
	 * based on the day's trading data.
	 * 
	 * @param tradingDataToday trading data for today.
	 * @param orders orders carried over from yesterday.
	 * @return the outstanding orders to carry over to tomorrow.
	 */
	private List<EquityOrder> processTradingData( final TradingDayPrices tradingDataToday, List<EquityOrder> orders ) {

		// Attempt to execute the queued orders
		orders = processOutstandingOrders( orders, tradingDataToday );

		// Apply analysis to generate more orders
		orders = addExitOrderForToday( tradingDataToday, orders );
		orders = addEntryOrderForToday( tradingDataToday, orders );

		return orders;
	}

	/**
	 * Update the exit logic analysis, adding any orders triggered by the day's trading data.
	 * 
	 * @param data todays trading data.
	 * @param openOrders the existing trading orders unfulfilled.
	 * @return the given list of open orders, plus any order added by the exit logic.
	 */
	private List<EquityOrder> addExitOrderForToday( final TradingDayPrices data, final List<EquityOrder> openOrders ) {
		final EquityOrder order = exit.update( broker, data );

		if (order != null) {
			openOrders.add( order );
		}

		return openOrders;
	}

	/**
	 * Update the entry logic analysis, adding any orders triggered by the day's trading data.
	 * 
	 * @param data todays trading data.
	 * @param openOrders the existing trading orders unfulfilled.
	 * @return the given list of open orders, plus any order added by the entry logic.
	 */
	private List<EquityOrder> addEntryOrderForToday( final TradingDayPrices data, final List<EquityOrder> openOrders ) {
		final EquityOrder order = entry.update( broker, funds, data );

		if (order != null) {
			openOrders.add( order );
		}

		return openOrders;
	}

	/**
	 * Attempts to process the outstanding orders against today's price action.
	 * 
	 * @return orders that were not executed as their conditions were not met.
	 */
	private List<EquityOrder> processOutstandingOrders( final List<EquityOrder> orders, final TradingDayPrices data ) {
		final List<EquityOrder> remainingOrders = new ArrayList<EquityOrder>( orders.size() );

		for (final EquityOrder order : orders) {

			if (order.isValid( data )) {
				final EquityOrder processedOrder = processOutstandingValidOrder( order, data );

				// Add the original / altered order back to try again
				if (processedOrder != null) {
					remainingOrders.add( processedOrder );
				}
			}
		}

		return remainingOrders;
	}

	/**
	 * Processes the order based on whether the execution criteria are met.
	 * 
	 * @return the order that may or may not have been executed.
	 */
	private EquityOrder processOutstandingValidOrder( final EquityOrder order, final TradingDayPrices data ) {

		if (order.areExecutionConditionsMet( data )) {
			return executeOrder( order, data );
		}

		return order;
	}

	/**
	 * Attempt to execute the order.
	 * 
	 * @param order trade to execute.
	 * @return <code>null</code> on success, or an order to re-attempt next cycle.
	 * @throws OrderException
	 */
	private EquityOrder executeOrder( final EquityOrder order, final TradingDayPrices data ) {
		try {
			order.execute( broker, broker, funds, data );

			// Discard the order, as it's processed
			return null;

		} catch (final InsufficientFundsException e) {
			LOG.warn( String.format( "Insufficient funds to execute order %s", order.toString() ) );

			final EquityOrderInsufficientFundsAction action = entry.actionOnInsufficentFunds( order );
			switch (action) {
				case DELETE:
					// Discard the order
					return null;
				case RESUMIT:
				default:
					throw new IllegalArgumentException( String.format( "Unsupported insufficient funds action: %s",
							action ) );
			}
		} catch (final OrderException e) {
			LOG.error( e );
			// TODO handle?
			throw new IllegalArgumentException( "Unhandled Order exception", e );
		}
	}
}
