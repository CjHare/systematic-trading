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

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.systematic.trading.backtest.output.elastic.model.ElasticFormat;
import com.systematic.trading.backtest.output.elastic.model.ElasticTypeName;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;

/**
 * Resource for converting Net worth Events to input for Elastic Search.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticNetWorthEventRequestResource {

	private final String event;
	private final float cashBalance;
	private final float equityBalance;
	private final float equityBalanceValue;
	private final float networth;
	private final LocalDate eventDate;

	public ElasticNetWorthEventRequestResource( final NetWorthEvent event ) {
		this.event = event.getType().getName();
		this.cashBalance = event.getCashBalance().floatValue();
		this.equityBalance = event.getEquityBalance().floatValue();
		this.equityBalanceValue = event.getEquityBalanceValue().floatValue();
		this.networth = event.getNetWorth().floatValue();
		this.eventDate = event.getEventDate();
	}

	@JsonProperty(ElasticTypeName.EVENT)
	public String getEvent() {
		return event;
	}

	@JsonProperty(ElasticTypeName.CASH_BALANCE)
	public float getCashBalance() {
		return cashBalance;
	}

	@JsonProperty(ElasticTypeName.EQUITY_BALANCE)
	public float getEquityBalance() {
		return equityBalance;
	}

	@JsonProperty(ElasticTypeName.EQUITY_BALANCE_VALUE)
	public float getEquityBalanceValue() {
		return equityBalanceValue;
	}

	@JsonProperty(ElasticTypeName.NETWORTH)
	public float getNetworth() {
		return networth;
	}

	@JsonProperty(ElasticTypeName.EVENT_DATE)
	@JsonFormat(pattern = ElasticFormat.LOCAL_DATE)
	public LocalDate getEventDate() {
		return eventDate;
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder("ElasticNetWorthEventResource [");
		out.append("event=");
		out.append(event);
		out.append("cashBalance=");
		out.append(cashBalance);
		out.append(", equityBalance=");
		out.append(equityBalance);
		out.append(", equityBalanceValue=");
		out.append(equityBalanceValue);
		out.append(", networth=");
		out.append(networth);
		out.append(", eventDate=");
		out.append(eventDate);
		out.append("]");
		return out.toString();
	}
}