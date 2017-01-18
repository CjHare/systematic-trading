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

import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.simulation.StandardEquityEvent;

/**
 * Equity event for a single equity.
 * 
 * @author CJ Hare
 */
public class SingleEquityEvent extends StandardEquityEvent implements EquityEvent {

	private final EquityEventType type;
	private final EquityIdentity id;

	public SingleEquityEvent(final EquityIdentity id, final BigDecimal startingEquityBalance,
	        final BigDecimal endEquityBalance, final BigDecimal amount, final EquityEventType type,
	        final LocalDate transactionDate, final BigDecimal transactionValue) {
		super(amount, startingEquityBalance, endEquityBalance, transactionDate, transactionValue);
		this.type = type;
		this.id = id;
	}

	@Override
	public EquityEventType getType() {
		return type;
	}

	@Override
	public EquityIdentity getIdentity() {
		return id;
	}
}
