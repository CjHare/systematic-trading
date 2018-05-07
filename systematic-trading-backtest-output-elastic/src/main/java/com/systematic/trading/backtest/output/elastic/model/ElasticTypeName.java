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
package com.systematic.trading.backtest.output.elastic.model;

/**
 * Name of a field on a type in Elastic Seearch.
 * 
 * @author CJ Hare
 */
public final class ElasticTypeName {

	public static final String AMOUNT = "amount";
	public static final String CASH_BALANCE = "cash_balance";
	public static final String DIRECTION_TYPE = "direction_type";
	public static final String END_EQUITY_BALANCE = "end_equity_balance";
	public static final String EQUITY_BALANCE = "equity_balance";
	public static final String EQUITY_BALANCE_VALUE = "equity_balance_value";
	public static final String EQUITY_AMOUNT = "equity_amount";
	public static final String EQUITY_VALUE = "equity_value";
	public static final String EVENT = "event";
	public static final String EVENT_DATE = "event_date";
	public static final String END_DATE_EXCLUSIVE = "end_date_exclusive";
	public static final String FUNDS_AFTER = "funds_after";
	public static final String FUNDS_BEFORE = "funds_before";
	public static final String FREQUENCY = "frequency";
	public static final String IDENTITY = "identity";
	public static final String START_DATE_INCLUSIVE = "start_date_inclusive";
	public static final String NETWORTH = "networth";
	public static final String PERCENTAGE_CHANGE = "percentage_change";
	public static final String SIGNAL_DATE = "signal_date";
	public static final String SIGNAL_TYPE = "signal_type";
	public static final String STARTING_EQUITY_BALANCE = "starting_equity_balance";
	public static final String TRANSACTION_DATE = "transaction_date";
	public static final String TRANSACTION_FEE = "transaction_fee";
	public static final String TOTAL_COST = "total_cost";

	private ElasticTypeName() {}
}
