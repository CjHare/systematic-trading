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
package com.systematic.trading.backtest.display.console;

import java.text.DecimalFormat;

import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class ConsoleEventDisplay implements CashEventListener, BrokerageEventListener, OrderEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	@Override
	public void event( final OrderEvent event ) {

		final String output = String.format("Place Order - %s total cost %s created after c.o.b on %s", event.getType(),
		        TWO_DECIMAL_PLACES.format(event.getTotalCost()), event.getTransactionDate());

		System.out.println(output);
	}

	@Override
	public void event( final BrokerageEvent event ) {

		final String output = String.format("Brokerage Account - %s: %s - equity balance %s -> %s on %s",
		        event.getType(), TWO_DECIMAL_PLACES.format(event.getEquityAmount()),
		        TWO_DECIMAL_PLACES.format(event.getStartingEquityBalance()),
		        TWO_DECIMAL_PLACES.format(event.getEndEquityBalance()), event.getTransactionDate());

		System.out.println(output);
	}

	@Override
	public void event( final CashEvent event ) {

		final String output = String.format("Cash Account - %s: %s - funds %s -> %s on %s", event.getType(),
		        TWO_DECIMAL_PLACES.format(event.getAmount()), TWO_DECIMAL_PLACES.format(event.getFundsBefore()),
		        TWO_DECIMAL_PLACES.format(event.getFundsAfter()), event.getTransactionDate());

		System.out.println(output);
	}
}
