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

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.event.BrokerageEvent;
import com.systematic.trading.backtest.event.CashEvent;
import com.systematic.trading.backtest.event.OrderEvent;
import com.systematic.trading.event.Event;
import com.systematic.trading.event.recorder.EventRecorder;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class ConsoleEventRecorder implements EventRecorder {

	private final List<BrokerageEvent> brokerage;
	private final List<CashEvent> cash;
	private final List<OrderEvent> orders;

	public ConsoleEventRecorder() {
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

	public void summary() {
		System.out.println( "###############" );
		System.out.println( "### Summary ###" );
		System.out.println( "###############" );

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

	}
}
