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

import com.systematic.trading.simulation.analysis.statistics.event.OrderEventStatistics;
import com.systematic.trading.simulation.order.event.OrderEvent;

/**
 * Cumulative recording of the equity events for statistical purposes.
 * 
 * @author CJ Hare
 */
public class CumulativeOrderEventStatistics implements OrderEventStatistics {

	private int entryEventCount = 0;
	private int deleteEntryEventCount = 0;
	private int exitEventCount = 0;
	private int deleteExitEventCount = 0;

	@Override
	public void event( final OrderEvent event ) {

		switch (event.type()) {
			case ENTRY:
				entryEventCount++;
				break;
			case DELETE_ENTRY:
				deleteEntryEventCount++;
				break;
			case EXIT:
				exitEventCount++;
				break;
			case DELETE_EXIT:
				deleteExitEventCount++;
				break;
			default:
				throw new IllegalArgumentException(String.format("Order event type %s is unexpected", event.type()));
		}
	}

	@Override
	public int entryEventCount() {

		return entryEventCount;
	}

	@Override
	public int deleteEntryEventCount() {

		return deleteEntryEventCount;
	}

	@Override
	public int exitEventCount() {

		return exitEventCount;
	}

	@Override
	public int deleteExitEventCount() {

		return deleteExitEventCount;
	}
}
