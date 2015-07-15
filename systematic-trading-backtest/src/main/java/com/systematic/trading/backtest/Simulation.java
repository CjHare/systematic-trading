/**
 * Copyright (c) 2015, CJ Hare
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
package com.systematic.trading.backtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.data.DataPoint;

/**
 * The application of the chosen trading logic over a given set of data is performed in the Simulation.
 * <p/>
 * Applies the trading logic over a single equity only.
 * 
 * @author CJ Hare
 */
public class Simulation {

	/** Data set to feed into the trading behaviour. */
	private final SortedSet<DataPoint> chronologicalData;

	/** Makes the decision on whether action is required. */
	private final TradingLogic logic;

	/** The manager dealing with cash and it's accounting. */
	private final CashAccount cashAccount;

	/** Dealer of equities. */
	private final Brokerage broker;

	public Simulation( final DataPoint[] unordered, final TradingLogic logic, final CashAccount cashAccount,
			final Brokerage broker ) {

		// Correctly order the data set with the oldest entry first
		this.chronologicalData = new TreeSet<DataPoint>( new Comparator<DataPoint>() {
			@Override
			public int compare( final DataPoint a, final DataPoint b ) {
				return a.getDate().compareTo( b.getDate() );
			}
		} );
		this.chronologicalData.addAll( Arrays.asList( unordered ) );

		this.logic = logic;
		this.cashAccount = cashAccount;
		this.broker = broker;
	}

	public void run() {

		//TODO restructure
		
		// Iterating through the chronologically ordered data point
		TradeSignal signal = null;
		TradingOrder order = null;
		List<TradingOrder> orders = new ArrayList<TradingOrder>();

		for (final DataPoint data : chronologicalData) {

			orders = processOutstandingOrders( orders, data );

			signal = logic.update( data );

			cashAccount.update( data );

			if (signal != null) {
				order = logic.createOrder( signal, cashAccount, broker );
			}

			if (order != null) {
				orders.add( order );
			}
		}
	}

	/**
	 * Attempts to process the outstanding orders against today's price action.
	 * 
	 * @return orders that were not executed as their conditions were not met.
	 */
	private List<TradingOrder> processOutstandingOrders( final List<TradingOrder> orders, final DataPoint data ) {
		final List<TradingOrder> remainingOrders = new ArrayList<TradingOrder>( orders.size() );

		for (final TradingOrder order : orders) {

			if (!order.hasExpired()) {
				if (order.areExecutionConditionsMet( data )) {

					// TODO execute if money, otherwise ??
					// TODO add calc on brokerage
					// TODO execute any outstanding order(s)
					// TODO decrement the cash account balance on trade

				} else {
					remainingOrders.add( order );
				}
			}
		}

		return remainingOrders;
	}

}
