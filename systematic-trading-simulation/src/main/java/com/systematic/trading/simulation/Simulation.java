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
package com.systematic.trading.simulation;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.roi.ReturnOnInvestmentListener;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.exception.InsufficientEquitiesException;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.exception.InsufficientFundsException;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.simulation.order.event.EquityOrderDeletedDueToInsufficentFundsEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEventListener;
import com.systematic.trading.strategy.Strategy;

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
	private static final Logger LOG = LogManager.getLogger(Simulation.class);

	/** Makes the decision on whether entry and exit actions. */
	private final Strategy strategy;

	/** The manager dealing with cash and it's accounting. */
	private final CashAccount funds;

	/** Dealer of equities, manages the equity balance. */
	private final Brokerage broker;

	/** Time between deposit events. */
	private final Period interval = Period.ofDays(1);

	/** Return on investment calculator. */
	private final ReturnOnInvestmentListener roi;

	/** Listeners interested in entry events. */
	private final List<OrderEventListener> orderEventListeners = new ArrayList<>();

	/** Listeners interested in state transition events. */
	private final List<SimulationStateListener> stateListeners = new ArrayList<>();

	/** Trading data to use for the simulation. */
	private final TickerSymbolTradingData tradingData;

	public Simulation( final TickerSymbolTradingData tradingData, final Brokerage broker, final CashAccount funds,
	        final ReturnOnInvestmentListener roi, final Strategy strategy ) {
		this.strategy = strategy;
		this.funds = funds;
		this.broker = broker;
		this.roi = roi;
		this.tradingData = tradingData;
	}

	public void run() {

		final Map<LocalDate, TradingDayPrices> tradingDayPrices = tradingData.tradingPrices();
		final LocalDate endDate = tradingData.latestDate();

		List<EquityOrder> orders = new ArrayList<>();
		LocalDate currentDate = tradingData.earliestDate();

		while (currentDate.isBefore(endDate)) {

			// Financial activity of deposits, withdrawal and interest
			funds.update(currentDate);

			final TradingDayPrices currentTradingData = tradingDayPrices.get(currentDate);

			// Only when there is trading data for today
			if (currentTradingData != null) {

				// Process orders and add those from the day's trading data
				orders = processTradingData(currentTradingData, orders);

				// Update the return on investment calculator
				roi.update(broker, funds, currentTradingData);

				// Broker activity of fees, clearing transactions at the end of the business day
				broker.update(currentTradingData);
			}

			// Move date to tomorrow
			currentDate = currentDate.plus(interval);
		}

		notifyListeners(SimulationState.COMPLETE);
	}

	/**
	 * Processes any outstanding orders when their execution criteria and add any additional orders
	 * based on the day's trading data.
	 * 
	 * @param tradingDataToday trading data for today.
	 * @param orders orders carried over from yesterday.
	 * @return the outstanding orders to carry over to tomorrow.
	 */
	private List<EquityOrder> processTradingData( final TradingDayPrices tradingDataToday,
	        final List<EquityOrder> orders ) {

		// Attempt to execute the queued orders
		final List<EquityOrder> withoutAnyOutstandingOrders = processOutstandingOrders(orders, tradingDataToday);

		// Apply analysis to generate more orders
		final List<EquityOrder> exitOrdersIncluded = addExitOrderForToday(tradingDataToday,
		        withoutAnyOutstandingOrders);

		return addEntryOrderForToday(tradingDataToday, exitOrdersIncluded);
	}

	/**
	 * Update the exit logic analysis, adding any orders triggered by the day's trading data.
	 * 
	 * @param data todays trading data.
	 * @param openOrders the existing trading orders unfulfilled.
	 * @return the given list of open orders, plus any order added by the exit logic.
	 */
	private List<EquityOrder> addExitOrderForToday( final TradingDayPrices data, final List<EquityOrder> openOrders ) {
		final Optional<EquityOrder> order = strategy.exitTick(broker, data);

		if (order.isPresent()) {
			notifyListeners(order.get().orderEvent());
			openOrders.add(order.get());
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
		final Optional<EquityOrder> order = strategy.entryTick(broker, funds, data);

		if (order.isPresent()) {
			notifyListeners(order.get().orderEvent());
			openOrders.add(order.get());
		}

		return openOrders;
	}

	/**
	 * Attempts to process the outstanding orders against today's price action.
	 * 
	 * @return orders that were not executed as their conditions were not met.
	 */
	private List<EquityOrder> processOutstandingOrders( final List<EquityOrder> orders, final TradingDayPrices data ) {
		final List<EquityOrder> remainingOrders = new ArrayList<>(orders.size());

		for (final EquityOrder order : orders) {

			if (order.isValid(data)) {
				final EquityOrder processedOrder = processOutstandingValidOrder(order, data);

				// Add the original / altered order back to try again
				if (processedOrder != null) {
					remainingOrders.add(processedOrder);
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

		if (order.areExecutionConditionsMet(data)) {
			return executeOrder(order, data);
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
			order.execute(broker, broker, funds, data);

			// Discard the order, as it's processed
			return null;

		} catch (final InsufficientFundsException e) {

			final EquityOrderInsufficientFundsAction action = strategy.actionOnInsufficentFunds(order);

			switch (action) {
				case DELETE:
					// Discard the order
					notifyListeners(new EquityOrderDeletedDueToInsufficentFundsEvent(order.orderEvent()));
					return null;
				case RESUMIT:
				default:
					throw new IllegalArgumentException(
					        String.format("Unsupported insufficient funds action: %s for order: %s using strategy: %s",
					                action, order, strategy),
					        e);
			}
		} catch (final InsufficientEquitiesException e) {
			LOG.error(e);
			throw new IllegalArgumentException("Unhandled Order exception", e);
		}
	}

	private void notifyListeners( final OrderEvent event ) {
		for (final OrderEventListener listener : orderEventListeners) {
			listener.event(event);
		}
	}

	private void notifyListeners( final SimulationState event ) {
		for (final SimulationStateListener listener : stateListeners) {
			listener.stateChanged(event);
		}
	}

	/**
	 * Adds the listener to the set of order event listeners.
	 * 
	 * @param listener will receive notification of order event occurrences.
	 */
	public void addListener( final OrderEventListener listener ) {
		if (!orderEventListeners.contains(listener)) {
			orderEventListeners.add(listener);
		}
	}

	/**
	 * Adds the listener to the set of state transition listeners.
	 * 
	 * @param listener will receive notification of simulation state change occurrences.
	 */
	public void addListener( final SimulationStateListener listener ) {
		if (!stateListeners.contains(listener)) {
			stateListeners.add(listener);
		}
	}
}
