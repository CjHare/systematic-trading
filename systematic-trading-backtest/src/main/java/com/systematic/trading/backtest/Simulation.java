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

import java.util.Arrays;
import java.util.Comparator;
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

    /** Makes the decision on whether action is required.*/
    private final TradingLogic logic;

    /** The manager dealing with cash and it's accounting.*/
    private final CashAccount cashAccount;

    /** Dealer of equities.*/
    private final Brokerage broker;

    public Simulation(final DataPoint[] unordered, final TradingLogic logic, final CashAccount cashAccount,
            final Brokerage broker) {

        // Correctly order the data set with the oldest entry first
        this.chronologicalData = new TreeSet<DataPoint>(new Comparator<DataPoint>() {
            @Override
            public int compare(final DataPoint a, final DataPoint b) {
                return a.getDate().compareTo(b.getDate());
            }
        });
        this.chronologicalData.addAll(Arrays.asList(unordered));

        this.logic = logic;
        this.cashAccount = cashAccount;
        this.broker = broker;
    }

    public void run() {

        // Iterating through the chronologically ordered data point
        TradeSignal signal;
        TradeEvent trade;

        for (final DataPoint data : chronologicalData) {

            signal = logic.update(data);

            cashAccount.update(data);

            trade = filterTradeSignal(signal);

            //TODO broker, buying logic i.e. how much

            //TODO need a trading balance
            //TODO decrement the cash account balance on trade

            //TODO queue for orders, choose to place order & execute tomorrow, at tomorrows price, or at a price
            //TODO action the trade (if there is one)
        }
    }

    /**
     * Determines whether there are sufficient funds to perform a trade based on the signal.
     */
    private TradeEvent filterTradeSignal(final TradeSignal signal) {
        //TODO action signal
        //TODO apply brokerage
        
        //TODO calculation
        return null;
    }

}
