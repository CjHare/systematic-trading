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
package com.systematic.trading.analysis.event;

import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEvent.EquityOrderType;
import com.systematic.trading.strategy.signal.SignalAnalysisEvent;

/**
 * Log output for only the buy orders i.e. 'when' and 'how much' to buy.
 * 
 * @author CJ Hare
 */
public class LogEntryOrderEventListner implements BacktestEventListener {

	/** Classes' logger. */
	private static final Logger LOG = LogManager.getLogger(LogEntryOrderEventListner.class);

	/** Format for the oder amounts */
	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");

	@Override
	public void event( final CashEvent event ) {

		// Only displaying the entry order events
	}

	@Override
	public void event( final OrderEvent event ) {

		if (event.type() == EquityOrderType.ENTRY) {
			LOG.info(String.format("Buy event on %s. Place a buy order for the total value of %s",
			        event.transactionDate(), TWO_DECIMAL_PLACES.format(event.totalCost())));

		}
	}

	@Override
	public void event( final BrokerageEvent event ) {

		// Only displaying the entry order events
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {

		// Only displaying the entry order events
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		// Only displaying the entry order events
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only displaying the entry order events
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {

		// Only displaying the entry order events
	}

	@Override
	public void event( final EquityEvent event ) {

		// Only displaying the entry order events
	}
}