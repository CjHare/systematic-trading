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
package com.systematic.trading.backtest.analysis.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.analysis.ReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.ReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.event.CashEvent;
import com.systematic.trading.backtest.event.CashEvent.CashEventType;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.Event;

/**
 * Calculates and records the return on investment (ROI) at periodic intervals.
 * 
 * @author CJ Hare
 */
public class CulmativeReturnOnInvestmentCalculator implements ReturnOnInvestmentCalculator {

	/** Used for the conversion to percentage. */
	private static BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );

	/** Context for BigDecimal operations. */
	private final MathContext mathContext;

	/** Parties interested in ROI events. */
	private final List<ReturnOnInvestmentCalculatorListener> listeners = new ArrayList<ReturnOnInvestmentCalculatorListener>();

	/** Net Worth as recorded on previous update. */
	private BigDecimal previousNetWorth;

	/** Date of the last update on recording of net worth. */
	private LocalDate previousDate;

	/** Running total of the amount deposited since the last net worth calculation. */
	private BigDecimal depositedSincePreviousNetWorth = BigDecimal.ZERO;

	public CulmativeReturnOnInvestmentCalculator( final MathContext mathContext ) {
		this.mathContext = mathContext;
	}

	@Override
	public void update( final Brokerage broker, final CashAccount cashAccount, final TradingDayPrices tradingData ) {

		final BigDecimal percentageChange = calculatePercentageChangeInNetWorth( broker, cashAccount, tradingData );
		final Period elapsed = calculateElapsedDuration( tradingData.getDate() );

		notifyListeners( percentageChange, elapsed );
	}

	private void notifyListeners( final BigDecimal percentageChange, final Period elapsed ) {
		for (final ReturnOnInvestmentCalculatorListener listener : listeners) {
			listener.record( percentageChange, elapsed );
		}
	}

	public void addListener( final ReturnOnInvestmentCalculatorListener listener ) {
		if (!listeners.contains( listener )) {
			listeners.add( listener );
		}
	}

	private Period calculateElapsedDuration( final LocalDate latestDate ) {

		final Period elapsed;

		if (previousDate == null) {
			// No previous data, the change is ONE
			elapsed = Period.ofDays( 1 );
		} else {
			elapsed = Period.between( previousDate, latestDate );
		}

		// Move the previous date to now
		previousDate = latestDate;

		return elapsed;
	}

	private BigDecimal calculatePercentageChangeInNetWorth( final Brokerage broker, final CashAccount cashAccount,
			final TradingDayPrices tradingData ) {

		final BigDecimal equityBalance = broker.getEquityBalance();
		final BigDecimal lastClosingPrice = tradingData.getClosingPrice().getPrice();
		final BigDecimal holdingsValue = equityBalance.multiply( lastClosingPrice, mathContext );
		final BigDecimal cashBalance = cashAccount.getBalance();
		final BigDecimal netWorth = cashBalance.add( holdingsValue, mathContext );

		final BigDecimal percentageChange;

		// If there's no previous data, there's no change
		if (previousNetWorth == null) {
			percentageChange = BigDecimal.ZERO;
		} else {
			// Difference / previous worth
			final BigDecimal absoluteChange = netWorth.subtract( previousNetWorth, mathContext ).subtract(
					depositedSincePreviousNetWorth, mathContext );
			percentageChange = absoluteChange.divide( previousNetWorth, mathContext ).multiply( ONE_HUNDRED,
					mathContext );
		}

		// Reset the counters
		previousNetWorth = netWorth;
		depositedSincePreviousNetWorth = BigDecimal.ZERO;

		return percentageChange;
	}

	@Override
	public void event( final Event event ) {

		if (event instanceof CashEvent) {
			final CashEvent cashEvent = (CashEvent) event;

			if (CashEventType.DEPOSIT.equals( cashEvent.getType() )) {
				// Add the deposit to the running total
				depositedSincePreviousNetWorth = depositedSincePreviousNetWorth
						.add( cashEvent.getAmount(), mathContext );
			}
		}

	}
}