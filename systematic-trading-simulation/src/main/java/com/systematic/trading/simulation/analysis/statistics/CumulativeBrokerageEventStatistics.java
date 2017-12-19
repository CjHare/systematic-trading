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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.systematic.trading.simulation.analysis.statistics.event.BrokerageEventStatistics;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;

/**
 * Cumulative recording of the brokerage events for statistical purposes.
 * 
 * @author CJ Hare
 */
public class CumulativeBrokerageEventStatistics implements BrokerageEventStatistics {

	/** Running total of all brokerage fees paid.*/
	private BigDecimal brokerageFees = BigDecimal.ZERO;

	/** Buy events are put into bins of their precise amount, aggregation left for display step.*/
	private Map<BigDecimal, BigInteger> buyEvents = new HashMap<>();

	/** Sell events are put into bins of their precise amount, aggregation left for display step.*/
	private Map<BigDecimal, BigInteger> sellEvents = new HashMap<>();

	@Override
	public void event( final BrokerageEvent event ) {

		brokerageFees = brokerageFees.add(event.transactionFee());

		switch (event.type()) {
			case BUY:
				buyEvents.put(event.equityAmount(), increment(buyEvents, event.equityAmount()));
			break;
			case SELL:
				sellEvents.put(event.equityAmount(), increment(sellEvents, event.equityAmount()));
			break;
			default:
				throw new IllegalArgumentException(
				        String.format("Brokerage event type %s is unexpected", event.type()));
		}
	}

	@Override
	public BigDecimal brokerageFees() {
		return brokerageFees;
	}

	@Override
	public BigInteger buyEventCount() {
		BigInteger buyEventCount = BigInteger.ZERO;

		for (final BigInteger event : buyEvents.values()) {
			buyEventCount = buyEventCount.add(event);
		}

		return buyEventCount;
	}

	@Override
	public BigInteger sellEventCount() {
		BigInteger sellEventCount = BigInteger.ZERO;

		for (final BigInteger event : sellEvents.values()) {
			sellEventCount = sellEventCount.add(event);
		}

		return sellEventCount;
	}

	@Override
	public Map<BigDecimal, BigInteger> buyEvents() {
		return buyEvents;
	}

	@Override
	public Map<BigDecimal, BigInteger> sellEvents() {
		return sellEvents;
	}

	private BigInteger increment( final Map<BigDecimal, BigInteger> count, final BigDecimal key ) {
		return count.get(key) == null ? BigInteger.ONE : count.get(key).add(BigInteger.ONE);
	}
}