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
package com.systematic.trading.backtest.output.elastic.model.index.equity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.systematic.trading.simulation.equity.event.EquityEvent;

/**
 * Resource for converting Equity Events to input for Elastic Search.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticEquityEventResource {

	private final String event;
	private final String identity;
	private final float startingEquityBalance;
	private final float endEquityBalance;
	private final float equityAmount;
	private final LocalDate transactionDate;

	public ElasticEquityEventResource( final EquityEvent event ) {
		this.event = event.getType().getName();
		this.identity = event.getIdentity().getTickerSymbol();
		this.startingEquityBalance = event.getStartingEquityBalance().floatValue();
		this.endEquityBalance = event.getEndEquityBalance().floatValue();
		this.equityAmount = event.getEquityAmount().floatValue();
		this.transactionDate = event.getTransactionDate();
	}

	public String getEvent() {
		return event;
	}

	public String getIdentity() {
		return identity;
	}

	public float getStartingEquityBalance() {
		return startingEquityBalance;
	}

	public float getEndEquityBalance() {
		return endEquityBalance;
	}

	public float getEquityAmount() {
		return equityAmount;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder("ElasticEquityEventResource [");
		out.append("event=");
		out.append(event);
		out.append("identity=");
		out.append(identity);
		out.append(", startingEquityBalance=");
		out.append(startingEquityBalance);
		out.append(", endEquityBalance=");
		out.append(endEquityBalance);
		out.append(", equityAmount=");
		out.append(equityAmount);
		out.append(", transactionDate=");
		out.append(transactionDate);
		out.append("]");
		return out.toString();
	}
}