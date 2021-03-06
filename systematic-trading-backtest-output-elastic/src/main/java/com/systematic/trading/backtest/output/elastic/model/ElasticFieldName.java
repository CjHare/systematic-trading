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
 * The set of fields that Systematic Trading use with Elastic Search.
 * 
 * @author CJ Hare
 */
public enum ElasticFieldName {

	AMOUNT(ElasticTypeName.AMOUNT),
	CASH_BALANCE(ElasticTypeName.CASH_BALANCE),
	DIRECTION_TYPE(ElasticTypeName.DIRECTION_TYPE),
	END_EQUITY_BALANCE(ElasticTypeName.END_EQUITY_BALANCE),
	EQUITY_BALANCE(ElasticTypeName.EQUITY_BALANCE),
	EQUITY_BALANCE_VALUE(ElasticTypeName.EQUITY_BALANCE_VALUE),
	EQUITY_AMOUNT(ElasticTypeName.EQUITY_AMOUNT),
	EQUITY_VALUE(ElasticTypeName.EQUITY_VALUE),
	EVENT(ElasticTypeName.EVENT),
	EVENT_DATE(ElasticTypeName.EVENT_DATE),
	FUNDS_AFTER(ElasticTypeName.FUNDS_AFTER),
	FUNDS_BEFORE(ElasticTypeName.FUNDS_BEFORE),
	FREQUENCY(ElasticTypeName.FREQUENCY),
	IDENTITY(ElasticTypeName.IDENTITY),
	INCLUSIVE_START_DATE(ElasticTypeName.START_DATE_INCLUSIVE),
	EXCLUSIVE_END_DATE(ElasticTypeName.END_DATE_EXCLUSIVE),
	NETWORTH(ElasticTypeName.NETWORTH),
	PERCENTAGE_CHANGE(ElasticTypeName.PERCENTAGE_CHANGE),
	SIGNAL_DATE(ElasticTypeName.SIGNAL_DATE),
	SIGNAL_TYPE(ElasticTypeName.SIGNAL_TYPE),
	STARTING_EQUITY_BALANCE(ElasticTypeName.STARTING_EQUITY_BALANCE),
	TRANSACTION_DATE(ElasticTypeName.TRANSACTION_DATE),
	TRANSACTION_FEE(ElasticTypeName.TRANSACTION_FEE),
	TOTAL_COST(ElasticTypeName.TOTAL_COST);

	private final String fieldName;

	ElasticFieldName( final String name ) {

		this.fieldName = name;
	}

	public String fieldName() {

		return fieldName;
	}
}
