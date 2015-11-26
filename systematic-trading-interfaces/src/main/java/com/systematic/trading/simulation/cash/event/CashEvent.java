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
package com.systematic.trading.simulation.cash.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.event.Event;

/**
 * A cash event that warrants being recorded.
 * 
 * @author CJ Hare
 */
public interface CashEvent extends Event {

	enum CashEventType {
		/** From the sale of equities. */
		CREDIT( "Credit" ),
		/** From the purchase of equities. */
		DEBIT( "Debit" ),
		/** Non-equity source of funds being credited. */
		DEPOSIT( "Deposit" ),
		/** Interest paid on cash held in transactional account. */
		INTEREST( "Interest" );

		private final String display;

		private CashEventType( final String display ) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}

	/**
	 * Retrieves the classification of cash event.
	 * 
	 * @return general category the cash event falls within.
	 */
	CashEventType getType();

	/**
	 * Value of the cash event.
	 * 
	 * @return amount of cash involved in the event.
	 */
	BigDecimal getAmount();

	/**
	 * Available fund prior to the cash event.
	 * 
	 * @return funds available before the cash event.
	 */
	BigDecimal getFundsBefore();

	/**
	 * Available funds after the cash event.
	 * 
	 * @return funds available after the cash event.
	 */
	BigDecimal getFundsAfter();

	/**
	 * Date of cash event.
	 * 
	 * @return when the cash event occurred.
	 */
	LocalDate getTransactionDate();
}
