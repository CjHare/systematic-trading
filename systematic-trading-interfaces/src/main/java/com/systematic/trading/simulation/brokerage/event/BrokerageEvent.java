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
package com.systematic.trading.simulation.brokerage.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.event.Event;

/**
 * A brokerage event that warrants being recorded.
 * 
 * @author CJ Hare
 */
public interface BrokerageEvent extends Event {

	public enum BrokerageAccountEventType {
		BUY("Buy"),
		SELL("Sell");

		private final String display;

		BrokerageAccountEventType( final String display ) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}
	}

	/**
	 * Retrieves the classification for the brokerage event.
	 * 
	 * @return general category the event falls within.
	 */
	BrokerageAccountEventType getType();

	/**
	 * Brokers fee for performing the trade.
	 * 
	 * @return amount paid to the broker to facilitate the trade.
	 */
	BigDecimal getTransactionFee();

	/**
	 * Number of equities prior to the brokerage event.
	 * 
	 * @return quantities of equities prior to the brokerage event.
	 */
	BigDecimal getStartingEquityBalance();

	/**
	 * Number of equities after the brokerage event.
	 * 
	 * @return quantities of equities after the brokerage event.
	 */
	BigDecimal getEndEquityBalance();

	/**
	 * Date of brokerage event.
	 * 
	 * @return when the brokerage event occurred.
	 */
	LocalDate getTransactionDate();

	/**
	 * The number of equities involved in the brokerage transaction.
	 * 
	 * @return number of equities being brokered.
	 */
	BigDecimal getEquityAmount();
}