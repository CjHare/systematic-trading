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
package com.systematic.trading.backtest.output.elastic.model.index.brokerage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;

/**
 * Resource for an brokerage event to send to Elastic search. 
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticBrokerageEventResource {

	private final String event;
	private final float equityAmount;
	private final float startingEquityBalance;
	private final float endEquityBalance;
	private final float transactionFee;
	private final LocalDate transactionDate;

	public ElasticBrokerageEventResource( final BrokerageEvent event ) {
		this.event = event.getType().getName();
		this.equityAmount = event.getEquityAmount().floatValue();
		this.startingEquityBalance = event.getStartingEquityBalance().floatValue();
		this.endEquityBalance = event.getEndEquityBalance().floatValue();
		this.transactionFee = event.getTransactionFee().floatValue();
		this.transactionDate = event.getTransactionDate();
	}

	public String getEvent() {
		return event;
	}

	public float getEquityAmount() {
		return equityAmount;
	}

	public float getStartingEquityBalance() {
		return startingEquityBalance;
	}

	public float getEndEquityBalance() {
		return endEquityBalance;
	}

	public float getTransactionFee() {
		return transactionFee;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder("ElasticCashEvent [");
		out.append("event=");
		out.append(event);
		out.append(", equityAmount=");
		out.append(equityAmount);
		out.append(", startingEquityBalance=");
		out.append(startingEquityBalance);
		out.append(", endEquityBalance=");
		out.append(endEquityBalance);
		out.append(", transactionFee=");
		out.append(transactionFee);
		out.append(", transactionDate=");
		out.append(transactionDate);
		out.append("]");
		return out.toString();
	}
}