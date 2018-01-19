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
package com.systematic.trading.simulation.analysis.statistics;

import com.systematic.trading.simulation.analysis.statistics.event.BrokerageEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.CashEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.EquityEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.OrderEventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Records the data produced during process, making the statistics easily accessible.
 * 
 * @author CJ Hare
 */
public interface EventStatistics
        extends CashEventListener, BrokerageEventListener, OrderEventListener, EquityEventListener {

	/**
	 * Retrieves the recorded order event statistics.
	 * 
	 * @return order events recorded to date.
	 */
	OrderEventStatistics orderEventStatistics();

	/**
	 * Retrieves the recorded brokerage event statistics.
	 * 
	 * @return brokerage events recorded to date.
	 */
	BrokerageEventStatistics brokerageEventStatistics();

	/**
	 * Retrieves the recorded cash event statistics.
	 * 
	 * @return cash events recorded to date.
	 */
	CashEventStatistics cashEventStatistics();

	/**
	 * Retrieves the recorded equity event statistics.
	 * 
	 * @return equity events recorded to date.
	 */
	EquityEventStatistics equityEventStatistics();
}