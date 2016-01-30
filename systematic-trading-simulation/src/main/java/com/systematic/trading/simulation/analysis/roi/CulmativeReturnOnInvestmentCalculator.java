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
package com.systematic.trading.simulation.analysis.roi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
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
public class CulmativeReturnOnInvestmentCalculator implements ReturnOnInvestmentCalculator {

	/** Used for the conversion to percentage. */
	private static BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );

	/** Context for BigDecimal operations. */
	private final MathContext mathContext;

	/** Parties interested in ROI events. */
	private final List<ReturnOnInvestmentEventListener> listeners = new ArrayList<ReturnOnInvestmentEventListener>();

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
		final ReturnOnInvestmentEvent event = createEvent( percentageChange, tradingData.getDate() );

		notifyListeners( event );

		// Move the previous date to now
		previousDate = tradingData.getDate();
	}

	private void notifyListeners( final ReturnOnInvestmentEvent event ) {
		for (final ReturnOnInvestmentEventListener listener : listeners) {
			listener.event( event );
		}
	}

	@Override
	public void addListener( final ReturnOnInvestmentEventListener listener ) {
		if (!listeners.contains( listener )) {
			listeners.add( listener );
		}
	}

	private ReturnOnInvestmentEvent createEvent( final BigDecimal percentageChange, final LocalDate latestDate ) {

		if (previousDate == null) {
			// No previous data, the change is ONE
			previousDate = latestDate.minus( Period.ofDays( 1 ) );
		}

		final ReturnOnInvestmentEvent event = new ReturnOnInvestmentEventImpl( percentageChange, previousDate,
				latestDate );

		// Move the previous date to now
		previousDate = latestDate;

		return event;
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
			final BigDecimal absoluteChange = netWorth.subtract( previousNetWorth, mathContext )
					.subtract( depositedSincePreviousNetWorth, mathContext );

			if (BigDecimal.ZERO.compareTo( absoluteChange ) == 0) {
				percentageChange = BigDecimal.ZERO;
			} else {
				percentageChange = absoluteChange.divide( previousNetWorth, mathContext ).multiply( ONE_HUNDRED,
						mathContext );
			}
		}

		// Reset the counters
		previousNetWorth = netWorth;
		depositedSincePreviousNetWorth = BigDecimal.ZERO;

		return percentageChange;
	}

	@Override
	public void event( final CashEvent cashEvent ) {

		if (CashEventType.DEPOSIT.equals( cashEvent.getType() )) {
			// Add the deposit to the running total
			depositedSincePreviousNetWorth = depositedSincePreviousNetWorth.add( cashEvent.getAmount(), mathContext );
		}
	}
}
