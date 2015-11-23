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
package com.systematic.trading.event.order;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.event.Event;

/**
 * An order event that warrants being recorded.
 * 
 * @author CJ Hare
 */
public interface OrderEvent extends Event {
	/**
	 * All the different event types for equity orders.
	 * 
	 * @author CJ Hare
	 */
	public enum EquityOrderType {
		ENTRY,
		EXIT,
		DELETE_ENTRY,
		DELETE_EXIT
	}

	/**
	 * Retrieve the type of the order.
	 * 
	 * @return purpose of the order that triggered the event recording.
	 */
	EquityOrderType getType();

	/**
	 * Date of cash event.
	 * 
	 * @return when the cash event occurred.
	 */
	LocalDate getTransactionDate();

	/**
	 * Total cost of the order.
	 * 
	 * @return cost of the order including any fees.
	 */
	BigDecimal getTotalCost();
}