/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.simulation.analysis.roi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.maths.formula.Networth;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventImpl;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEvent.CashEventType;

/**
 * Calculates and records the return on investment (ROI) at periodic intervals.
 * 
 * @author CJ Hare
 */
public class CulmativeReturnOnInvestment implements ReturnOnInvestmentListener {

	/** Parties interested in ROI events. */
	private final List<ReturnOnInvestmentEventListener> listeners = new ArrayList<>();

	/** Net Worth as recorded on previous update. */
	private Networth previousNetWorth;

	/** Date of the last update on recording of net worth. */
	private LocalDate previousDate;

	/** Running total of the amount deposited since the last net worth calculation. */
	private final Networth adjustment = new Networth();

	@Override
	public void update( final Brokerage broker, final CashAccount cashAccount, final TradingDayPrices tradingData ) {

		final BigDecimal percentageChange = percentageChangeInNetWorth(broker, cashAccount, tradingData);
		final ReturnOnInvestmentEvent event = event(percentageChange, tradingData.date());

		notifyListeners(event);
	}

	private void notifyListeners( final ReturnOnInvestmentEvent event ) {

		for (final ReturnOnInvestmentEventListener listener : listeners) {
			listener.event(event);
		}
	}

	@Override
	public void addListener( final ReturnOnInvestmentEventListener listener ) {

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void event( final CashEvent cashEvent ) {

		if (CashEventType.DEPOSIT == cashEvent.type()) {
			// Add the deposit to the running total
			adjustment.add(cashEvent.amount());
		}
	}

	private ReturnOnInvestmentEvent event( final BigDecimal percentageChange, final LocalDate latestDate ) {

		if (previousDate == null) {
			// No previous data, the change is ONE
			previousDate = latestDate.minus(Period.ofDays(1));
		}

		final ReturnOnInvestmentEvent event = new ReturnOnInvestmentEventImpl(
		        percentageChange,
		        previousDate,
		        latestDate);

		// Move the previous date to now
		previousDate = latestDate;

		return event;
	}

	private BigDecimal percentageChangeInNetWorth(
	        final Brokerage broker,
	        final CashAccount cashAccount,
	        final TradingDayPrices tradingData ) {

		final Networth netWorth = new Networth();
		netWorth.addEquity(broker.equityBalance(), tradingData.closingPrice().price());
		netWorth.add(cashAccount.balance());

		final BigDecimal percentageChange;

		// If there's no previous data, there's no change
		if (previousNetWorth == null) {
			percentageChange = BigDecimal.ZERO;
		} else {
			percentageChange = previousNetWorth.percentageChange(netWorth, adjustment);
		}

		// Reset the counters
		previousNetWorth = netWorth;
		adjustment.reset();

		return percentageChange;
	}
}
