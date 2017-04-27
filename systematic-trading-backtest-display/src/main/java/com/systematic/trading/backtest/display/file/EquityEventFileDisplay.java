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

import java.text.DecimalFormat;

import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.equity.event.EquityEventListener;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class EquityEventFileDisplay implements EquityEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	/** Display responsible for handling the file output. */
	private final DisplayMultithreading display;

	public EquityEventFileDisplay( final DisplayMultithreading display ) {
		this.display = display;

		display.write("=== Equity Events ===\n");
	}

	@Override
	public void event( EquityEvent event ) {
		final String content = String.format("Equity Event - %s: %s - equity balance %s -> %s on %s%n", event.getType(),
		        TWO_DECIMAL_PLACES.format(event.getEquityAmount()),
		        TWO_DECIMAL_PLACES.format(event.getStartingEquityBalance()),
		        TWO_DECIMAL_PLACES.format(event.getEndEquityBalance()), event.getTransactionDate());

		display.write(content);
	}
}