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
package com.systematic.trading.backtest.output.elastic.resource;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.systematic.trading.backtest.output.elastic.model.ElasticTypeName;
import com.systematic.trading.simulation.cash.event.CashEvent;

/**
 * Resource to send to Elastic Search for a CashEvent.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticCashEventRequestResource extends TransactionDateEventResource {

	private final float amount;
	private final float fundsBefore;
	private final float fundsAfter;

	public ElasticCashEventRequestResource( final CashEvent event ) {

		super(event.type().name(), event.transactionDate());
		this.amount = event.amount().floatValue();
		this.fundsBefore = event.fundsBefore().floatValue();
		this.fundsAfter = event.fundsAfter().floatValue();
	}

	@JsonGetter(ElasticTypeName.AMOUNT)
	public float amount() {

		return amount;
	}

	@JsonGetter(ElasticTypeName.FUNDS_BEFORE)
	public float fundsBefore() {

		return fundsBefore;
	}

	@JsonGetter(ElasticTypeName.FUNDS_AFTER)
	public float fundsAfter() {

		return fundsAfter;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder("ElasticCashEventResource [");
		out.append(super.toString());
		out.append(", amount=");
		out.append(amount);
		out.append(", fundsBefore=");
		out.append(fundsBefore);
		out.append(", fundsAfter=");
		out.append(fundsAfter);
		out.append("]");
		return out.toString();
	}
}
