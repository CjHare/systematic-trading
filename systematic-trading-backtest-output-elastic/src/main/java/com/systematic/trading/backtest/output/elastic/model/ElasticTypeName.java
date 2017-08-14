/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest.output.elastic.model;

/**
 * Name of a field on a type in Elastic Seearch.
 * 
 * @author CJ Hare
 */
public interface ElasticTypeName {

	String AMOUNT = "amount";
	String CASH_BALANCE = "cash_balance";
	String DIRECTION_TYPE = "direction_type";
	String END_EQUITY_BALANCE = "end_equity_balance";
	String EQUITY_BALANCE = "equity_balance";
	String EQUITY_BALANCE_VALUE = "equity_balance_value";
	String EQUITY_AMOUNT = "equity_amount";
	String EVENT = "event";
	String EVENT_DATE = "event_date";
	String EXCLUSIVE_END_DATE = "exclusive_end_date";
	String FUNDS_AFTER = "funds_after";
	String FUNDS_BEFORE = "funds_before";
	String IDENTITY = "identity";
	String INCLUSIVE_START_DATE = "inclusive_start_date";
	String NETWORTH = "networth";
	String PERCENTAGE_CHANGE = "percentage_change";
	String SIGNAL_DATE = "signal_date";
	String SIGNAL_TYPE = "signal_type";
	String STARTING_EQUITY_BALANCE = "starting_equity_balance";
	String TRANSACTION_DATE = "transaction_date";
	String TRANSACTION_FEE = "transaction_fee";
	String TOTAL_COST = "total_cost";
}