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
package com.systematic.trading.backtest.event.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

import com.systematic.trading.backtest.event.Event;

/**
 * Broker Account events, buying and selling equities.
 * 
 * @author CJ Hare
 */
public class BrokerageAccountEvent implements Event {

	private static final DecimalFormat TWO_DECIMAL_PLACES;

	static {
		TWO_DECIMAL_PLACES = new DecimalFormat();
		TWO_DECIMAL_PLACES.setMaximumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setMinimumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setGroupingUsed( false );
	}

	public enum BrokerageAccountEventType {
		BUY( "Buy" ),
		SELL( "Sell" );

		private final String display;

		private BrokerageAccountEventType( final String display ) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}

	private final String amount;
	private final String balanceBefore;
	private final String balanceAfter;
	private final LocalDate transactionDate;
	private final BrokerageAccountEventType type;

	public BrokerageAccountEvent( final BigDecimal balanceBefore, final BigDecimal balanceAfter,
			final BigDecimal amount, final BrokerageAccountEventType type, final LocalDate transactionDate ) {
		this.balanceBefore = TWO_DECIMAL_PLACES.format( balanceBefore );
		this.balanceAfter = TWO_DECIMAL_PLACES.format( balanceAfter );
		this.amount = TWO_DECIMAL_PLACES.format( amount );
		this.transactionDate = transactionDate;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format( "Brokerage Account - %s: %s - balance %s -> %s on %s", type.getDisplay(), amount,
				balanceBefore, balanceAfter, transactionDate );
	}

	public String getAmount() {
		return amount;
	}

	public String getFundsBefore() {
		return balanceBefore;
	}

	public String getFundsAfter() {
		return balanceAfter;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public BrokerageAccountEventType getType() {
		return type;
	}
}
