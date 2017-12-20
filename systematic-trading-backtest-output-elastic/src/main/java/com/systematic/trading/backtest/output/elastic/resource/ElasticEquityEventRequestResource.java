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
package com.systematic.trading.backtest.output.elastic.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.systematic.trading.backtest.output.elastic.model.ElasticTypeName;
import com.systematic.trading.simulation.equity.event.EquityEvent;

/**
 * Resource for converting Equity Events to input for Elastic Search.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticEquityEventRequestResource extends TransactionDateEventResource {

	private final String identity;
	private final float startingEquityBalance;
	private final float endEquityBalance;
	private final float equityAmount;

	public ElasticEquityEventRequestResource( final EquityEvent event ) {
		super(event.type().name(), event.transactionDate());
		this.identity = event.identity().getTickerSymbol();
		this.startingEquityBalance = event.startingEquityBalance().floatValue();
		this.endEquityBalance = event.endEquityBalance().floatValue();
		this.equityAmount = event.equityAmount().floatValue();
	}

	@JsonProperty(ElasticTypeName.IDENTITY)
	public String getIdentity() {

		return identity;
	}

	@JsonProperty(ElasticTypeName.STARTING_EQUITY_BALANCE)
	public float getStartingEquityBalance() {

		return startingEquityBalance;
	}

	@JsonProperty(ElasticTypeName.END_EQUITY_BALANCE)
	public float getEndEquityBalance() {

		return endEquityBalance;
	}

	@JsonProperty(ElasticTypeName.EQUITY_AMOUNT)
	public float getEquityAmount() {

		return equityAmount;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder("ElasticEquityEventResource [");
		out.append(super.toString());
		out.append("identity=");
		out.append(identity);
		out.append(", startingEquityBalance=");
		out.append(startingEquityBalance);
		out.append(", endEquityBalance=");
		out.append(endEquityBalance);
		out.append(", equityAmount=");
		out.append(equityAmount);
		out.append("]");
		return out.toString();
	}
}