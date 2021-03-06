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
package com.systematic.trading.simulation.analysis.statistics.event;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;

/**
 * Statistics over the occurring brokerage events.
 * 
 * @author CJ Hare
 */
public interface BrokerageEventStatistics {

	/**
	 * Brokerage event has occurred and merits recording.
	 * 
	 * @param event
	 *            brokerage related event warranting attention of the statistics.
	 */
	void event( BrokerageEvent event );

	/**
	 * The amount to date paid in brokerage fees.
	 * 
	 * @return sum of the brokerage fees for the purchases and sales performed.
	 */
	BigDecimal brokerageFees();

	/**
	 * Number of purchase transactions performed.
	 * 
	 * @return number of brokerage purchase actions carried out.
	 */
	BigInteger buyEventCount();

	/**
	 * Number of sale transactions performed.
	 * 
	 * @return number of brokerage sale actions carried out.
	 */
	BigInteger sellEventCount();

	/**
	 * Total of the buy events that occurred.
	 * 
	 * @return the number equities brought and their frequency.
	 */
	Map<BigDecimal, BigInteger> buyEvents();

	/**
	 * Total of the sell events that occurred.
	 * 
	 * @return the number equities sold and their frequency.
	 */
	Map<BigDecimal, BigInteger> sellEvents();
}
