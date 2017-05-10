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
package com.systematic.trading.backtest.output.elastic.model;

/**
 * The set of fields that Systematic Trading use with Elastic Search.
 * 
 * @author CJ Hare
 */
public enum ElasticFieldName {

	AMOUNT("amount"),
	CASH_BALANCE("cash_balance"),
	DIRECTION_TYPE("direction_type"),
	END_EQUITY_BALANCE("end_equity_balance"),
	EQUITY_BALANCE("equity_balance"),
	EQUITY_BALANCE_VALUE("equity_balance_value"),
	EQUITY_AMOUNT("equity_amount"),
	EVENT("event"),
	EVENT_DATE("event_date"),
	FUNDS_AFTER("funds_after"),
	FUNDS_BEFORE("funds_before"),
	IDENTITY("identity"),
	INCLUSIVE_START_DATE("inclusive_start_date"),
	INCLUSIVE_END_DATE("inclusive_end_date"),
	NETWORTH("networth"),
	PERCENTAGE_CHANGE("percentage_change"),
	SIGNAL_DATE("signal_date"),
	SIGNAL_TYPE("signal_type"),
	STARTING_EQUITY_BALANCE("starting_equity_balance"),
	TRANSACTION_DATE("transaction_date"),
	TRANSACTION_FEE("transaction_fee"),
	TOTAL_COST("total_cost");

	private final String name;

	ElasticFieldName( final String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}