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
package com.systematic.trading.simulation.analysis.networth.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data pertaining to the net worth.
 * 
 * @author CJ Hare
 */
public interface NetWorthEvent {

	public enum NetWorthEventType {
		COMPLETED;
	}

	/**
	 * Retrieve the brokerage's balance of equities.
	 * 
	 * @return number of equities held.
	 */
	BigDecimal equityBalance();

	/**
	 * Retrieve the value of the balance of equities.
	 * 
	 * @return how much the equities held are worth.
	 */
	BigDecimal equityBalanceValue();

	/**
	 * Balance held in the cash account.
	 * 
	 * @return funds not invested in equities.
	 */
	BigDecimal cashBalance();

	/**
	 * Retrieve the total net worth.
	 * 
	 * @return sum of the cash balance(s) and equities value, exclusive of any transaction fee, or
	 *         capital gains tax for liquidation.
	 */
	BigDecimal netWorth();

	/**
	 * Date when the event occurred
	 * 
	 * @return the date when the event occurred.
	 */
	LocalDate eventDate();

	/**
	 * Trigger for the Net worth event.
	 * 
	 * @return type of Net worth event.
	 */
	NetWorthEventType type();
}
