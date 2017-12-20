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
package com.systematic.trading.simulation.analysis.statistics;

import com.systematic.trading.simulation.analysis.statistics.event.BrokerageEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.CashEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.EquityEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.OrderEventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;

/**
 * Statistics recorded cumulatively, being updated when the events are received.
 * 
 * @author CJ Hare
 */
public class CumulativeEventStatistics implements EventStatistics {

	private final BrokerageEventStatistics brokerageStatistics = new CumulativeBrokerageEventStatistics();
	private final CashEventStatistics cashStatistics = new CumulativeCashEventStatistics();
	private final OrderEventStatistics orderStatistics = new CumulativeOrderEventStatistics();
	private final EquityEventStatistics equityStatistics = new CumulativeEquityEventStatistics();

	@Override
	public OrderEventStatistics orderEventStatistics() {

		return orderStatistics;
	}

	@Override
	public BrokerageEventStatistics brokerageEventStatistics() {

		return brokerageStatistics;
	}

	@Override
	public CashEventStatistics cashEventStatistics() {

		return cashStatistics;
	}

	@Override
	public void event( final CashEvent event ) {

		cashStatistics.event(event);
	}

	@Override
	public void event( final BrokerageEvent event ) {

		brokerageStatistics.event(event);
	}

	@Override
	public void event( final OrderEvent event ) {

		orderStatistics.event(event);
	}

	@Override
	public EquityEventStatistics equityEventStatistics() {

		return equityStatistics;
	}

	@Override
	public void event( final EquityEvent event ) {

		equityStatistics.event(event);
	}
}
