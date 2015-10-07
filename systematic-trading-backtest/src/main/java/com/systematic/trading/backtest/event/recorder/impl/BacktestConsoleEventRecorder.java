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
package com.systematic.trading.backtest.event.recorder.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.event.BrokerageEvent;
import com.systematic.trading.backtest.event.CashEvent;
import com.systematic.trading.backtest.event.OrderEvent;
import com.systematic.trading.backtest.event.impl.CashAccountEvent;
import com.systematic.trading.event.Event;
import com.systematic.trading.event.recorder.EventRecorder;
import com.systematic.trading.event.recorder.data.TickerSymbolTradingRange;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class BacktestConsoleEventRecorder implements EventRecorder {

	private final List<BrokerageEvent> brokerage;
	private final List<CashEvent> cash;
	private final List<OrderEvent> orders;

	private static final DecimalFormat FORMAT_TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	public BacktestConsoleEventRecorder() {
		this.brokerage = new ArrayList<BrokerageEvent>();
		this.cash = new ArrayList<CashEvent>();
		this.orders = new ArrayList<OrderEvent>();
	}

	@Override
	public void record( final Event event ) {

		if (event instanceof BrokerageEvent) {
			brokerage.add( (BrokerageEvent) event );
		} else if (event instanceof CashEvent) {
			cash.add( (CashEvent) event );
		} else if (event instanceof OrderEvent) {
			orders.add( (OrderEvent) event );
		} else {
			throw new IllegalArgumentException( String.format( "Unsupported event class: %s", event.getClass() ) );
		}

		System.out.println( event );
	}

	@Override
	public void eventSummary() {
		System.out.println( "\n" );

		System.out.println( "#####################" );
		System.out.println( "### Event Summary ###" );
		System.out.println( "#####################" );

		System.out.println( "" );
		System.out.println( String.format( "# Brokerage events: %s", brokerage.size() ) );

		System.out.println( "" );
		System.out.println( String.format( "# Order events: %s", orders.size() ) );

		System.out.println( "" );
		System.out.println( "Cash Account events" );
		int creditCount = 0, debitCount = 0, depositCount = 0, interestCount = 0;
		for (final CashEvent event : cash) {
			switch (event.getType()) {
				case CREDIT:
					creditCount++;
					break;
				case DEBIT:
					debitCount++;
					break;
				case DEPOSIT:
					depositCount++;
					break;
				case INTEREST:
					interestCount++;
					break;
				default:
					throw new IllegalArgumentException( String.format( "Cash Account event type %s not catered for",
							event.getType() ) );
			}
		}

		System.out.println( String.format( "# Cash account credit events: %s", creditCount ) );
		System.out.println( String.format( "# Cash account debit events: %s", debitCount ) );
		System.out.println( String.format( "# Cash account interest events: %s", interestCount ) );
		System.out.println( String.format( "# Cash account deposit events: %s", depositCount ) );

		summariseCashAccount( cash );
	}

	private void summariseCashAccount( final List<CashEvent> cash ) {
		BigDecimal depositSum = BigDecimal.ZERO;

		for (final CashEvent event : cash) {
			switch (event.getType()) {
				case CREDIT:
					break;
				case DEBIT:
					break;
				case DEPOSIT:
					final BigDecimal amount = BigDecimal.valueOf( Double.valueOf( ((CashAccountEvent) event)
							.getAmount() ) );
					depositSum = depositSum.add( amount );
					break;
				case INTEREST:
					break;
				default:
					throw new IllegalArgumentException( String.format( "Cash Account event type %s not catered for",
							event.getType() ) );
			}
		}

		System.out.println( String.format( "Total Cash deposit amount: %s", depositSum ) );
	}

	@Override
	public void header() {
		System.out.println( "\n" );
		System.out.println( "#######################" );
		System.out.println( "### Backtest Events ###" );
		System.out.println( "#######################" );
		System.out.println( "" );
	}

	@Override
	public void header( final TickerSymbolTradingRange range ) {

		System.out.println( String.format( "Data set for %s from %s to %s", range.getTickerSymbol(),
				range.getStartDate(), range.getEndDate() ) );

		final long daysBetween = ChronoUnit.DAYS.between( range.getStartDate(), range.getEndDate() );
		final double percentageTradingDays = ((double) range.getNumberOfTradingDays() / daysBetween) * 100;

		System.out
				.println( String.format( "# trading days: %s over %s days (%s percentage trading days)",
						range.getNumberOfTradingDays(), daysBetween,
						FORMAT_TWO_DECIMAL_PLACES.format( percentageTradingDays ) ) );

		System.out.println( "\n" );
	}

}
