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
package com.systematic.trading.backtest.event;

import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.strategy.signal.SignalAnalysisEvent;

/**
 * Single entry point to output for a simulation run without actually outputting any of the events.
 * <p/>
 * Produces no IO operations.
 * 
 * @author CJ Hare
 */
public class SilentBacktestEventLisener implements BacktestEventListener {

	@Override
	public void event( final CashEvent event ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void event( final OrderEvent event ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void event( final BrokerageEvent event ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		// Recording of this state change is not required for no display
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {

		// Recording of this event is not required for no display
	}

	@Override
	public void event( final EquityEvent event ) {

		// Recording of this event is not required for no display
	}
}
