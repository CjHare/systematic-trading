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
package com.systematic.trading.simulation.equity.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.event.Event;
import com.systematic.trading.model.EquityIdentity;

/**
 * An event derived from holding an equity that warrants recording.
 * 
 * @author CJ Hare
 */
public interface EquityEvent extends Event {

	public enum EquityEventType {
		MANAGEMENT_FEE("Management Fee");

		private final String name;

		EquityEventType( final String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Retrieves the classification for the equity event.
	 * 
	 * @return general category the event falls within.
	 */
	EquityEventType getType();

	/**
	 * Number of equities prior to the brokerage event.
	 * 
	 * @return quantities of equities prior to the brokerage event.
	 */
	BigDecimal getStartingEquityBalance();

	/**
	 * Number of equities after the equity event.
	 * 
	 * @return quantities of equities after the equity event.
	 */
	BigDecimal getEndEquityBalance();

	/**
	 * Date of equity event.
	 * 
	 * @return when the equity event occurred.
	 */
	LocalDate getTransactionDate();

	/**
	 * The number of equities involved in the event.
	 * 
	 * @return number of equities.
	 */
	BigDecimal getEquityAmount();

	/**
	 * Equity that the event has been applied onto.
	 * 
	 * @return identity of the equity that was subject to the event.
	 */
	EquityIdentity getIdentity();
}