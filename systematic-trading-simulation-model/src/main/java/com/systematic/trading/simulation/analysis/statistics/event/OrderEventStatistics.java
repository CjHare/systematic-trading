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
package com.systematic.trading.simulation.analysis.statistics.event;

import com.systematic.trading.simulation.order.event.OrderEvent;

/**
 * Statistics over the occurring order events.
 * 
 * @author CJ Hare
 */
public interface OrderEventStatistics {

	/**
	 * Order event has occurred and merits recording.
	 * 
	 * @param event order related event warranting attention of the statistics.
	 */
	void event( OrderEvent event );

	/**
	 * Number of entry events recorded.
	 * 
	 * @return number of entry events events recorded.
	 */
	int entryEventCount();

	/**
	 * Number of delete entry events recorded.
	 * 
	 * @return number of delete entry events events recorded.
	 */
	int deleteEntryEventCount();

	/**
	 * Number of exit events recorded.
	 * 
	 * @return number of exit events events recorded.
	 */
	int exitEventCount();

	/**
	 * Number of delete exit events recorded.
	 * 
	 * @return number of delete exit events events recorded.
	 */
	int deleteExitEventCount();
}