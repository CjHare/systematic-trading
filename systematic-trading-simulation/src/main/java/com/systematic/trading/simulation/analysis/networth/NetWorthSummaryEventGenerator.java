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
package com.systematic.trading.simulation.analysis.networth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.simulation.SimulationStateListener;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent.NetWorthEventType;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;

/**
 * Displays the the net worth.
 * 
 * @author CJ Hare
 */
public class NetWorthSummaryEventGenerator implements SimulationStateListener {

	/** Parties interested in receiving net worth events. */
	private final List<NetWorthEventListener> listeners = new ArrayList<>();

	private final Brokerage broker;
	private final TradingDayPrices lastTradingDay;
	private final CashAccount cashAccount;

	public NetWorthSummaryEventGenerator( final Brokerage broker, final TradingDayPrices lastTradingDay,
	        final CashAccount cashAccount ) {
		this.broker = broker;
		this.lastTradingDay = lastTradingDay;
		this.cashAccount = cashAccount;
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		final BigDecimal equityBalance = broker.equityBalance();
		final BigDecimal lastClosingPrice = lastTradingDay.closingPrice().getPrice();
		final BigDecimal equityBalanceValue = equityBalance.multiply(lastClosingPrice);
		final BigDecimal cashBalance = cashAccount.balance();
		final BigDecimal networth = cashAccount.balance().add(equityBalanceValue);
		final LocalDate eventDate = lastTradingDay.date();
		final NetWorthEventType type = NetWorthEventType.COMPLETED;

		final NetWorthEvent event = new NetWorthSummaryEvent(equityBalance, equityBalanceValue, cashBalance, networth,
		        eventDate, type);

		for (final NetWorthEventListener listener : listeners) {
			listener.event(event, transitionedState);
		}
	}

	/**
	 * Adds a listener to those notified when a net worth event occurs.
	 * 
	 * @param listener
	 *            party interested in net worth events.
	 */
	public void addListener( final NetWorthEventListener listener ) {

		listeners.add(listener);
	}
}