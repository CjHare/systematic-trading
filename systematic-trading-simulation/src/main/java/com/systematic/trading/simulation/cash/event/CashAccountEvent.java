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
package com.systematic.trading.simulation.cash.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cash Account events, such as credit, debit and interest.
 * 
 * @author CJ Hare
 */
public class CashAccountEvent implements CashEvent {

	private final BigDecimal amount;
	private final BigDecimal fundsBefore;
	private final BigDecimal fundsAfter;
	private final LocalDate transactionDate;
	private final CashEventType type;

	public CashAccountEvent( final BigDecimal fundsBefore, final BigDecimal fundsAfter, final BigDecimal amount,
	        final CashEventType type, final LocalDate transactionDate ) {
		this.fundsBefore = fundsBefore;
		this.fundsAfter = fundsAfter;
		this.amount = amount;
		this.transactionDate = transactionDate;
		this.type = type;
	}

	@Override
	public BigDecimal amount() {

		return amount;
	}

	@Override
	public BigDecimal fundsBefore() {

		return fundsBefore;
	}

	@Override
	public BigDecimal fundsAfter() {

		return fundsAfter;
	}

	@Override
	public LocalDate transactionDate() {

		return transactionDate;
	}

	@Override
	public CashEventType type() {

		return type;
	}
}