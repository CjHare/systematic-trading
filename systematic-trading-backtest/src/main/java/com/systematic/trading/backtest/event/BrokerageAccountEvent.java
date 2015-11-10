/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Broker Account events, buying and selling equities.
 * 
 * @author CJ Hare
 */
public class BrokerageAccountEvent implements BrokerageEvent {

	private final BigDecimal equityAmount;
	private final BigDecimal startingEquityBalance;
	private final BigDecimal endEquityBalance;
	private final LocalDate transactionDate;
	private final BrokerageAccountEventType type;
	private final BigDecimal transactionFee;
	private final BigDecimal transactionValue;

	public BrokerageAccountEvent( final BigDecimal startingEquityBalance, final BigDecimal endEquityBalance,
			final BigDecimal amount, final BrokerageAccountEventType type, final LocalDate transactionDate,
			final BigDecimal transactionValue, final BigDecimal transactionFee ) {
		this.startingEquityBalance = startingEquityBalance;
		this.endEquityBalance = endEquityBalance;
		this.equityAmount = amount;
		this.transactionDate = transactionDate;
		this.type = type;
		this.transactionValue = transactionValue;
		this.transactionFee = transactionFee;
	}

	@Override
	public BigDecimal getEquityAmount() {
		return equityAmount;
	}

	@Override
	public BigDecimal getStartingEquityBalance() {
		return startingEquityBalance;
	}

	@Override
	public BigDecimal getEndEquityBalance() {
		return endEquityBalance;
	}

	@Override
	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	@Override
	public BrokerageAccountEventType getType() {
		return type;
	}

	@Override
	public BigDecimal getTransactionFee() {
		return transactionFee;
	}

	public BigDecimal getTransactionValue() {
		return transactionValue;
	}
}
