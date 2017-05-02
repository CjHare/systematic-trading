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
package com.systematic.trading.backtest.display.file;

import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;

/**
 * Template providing state management for the various file display implementations.
 * 
 * @author CJ Hare
 */
public abstract class FileDisplay implements BacktestOutput {

	protected abstract EventStatisticsDisplay getEventStatisticsDisplay();

	protected abstract NetWorthSummaryDisplay getNetWorthSummaryDisplay();

	protected abstract NetWorthEventListener getNetWorthEventListener();

	@Override
	public void stateChanged( final SimulationState transitionedState ) {

		if (SimulationState.COMPLETE == transitionedState) {
			simulationCompleted();
		}
	}

	private void simulationCompleted() {
		getEventStatisticsDisplay().displayEventStatistics();
		getNetWorthSummaryDisplay().displayNetWorth();
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {
		getNetWorthEventListener().event(event, state);
		getNetWorthSummaryDisplay().event(event, state);
	}
}
