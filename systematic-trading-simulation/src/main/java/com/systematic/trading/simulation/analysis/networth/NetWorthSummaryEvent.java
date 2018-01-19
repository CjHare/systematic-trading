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
package com.systematic.trading.simulation.analysis.networth;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;

/**
 * Data pertaining to the net worth.
 * 
 * @author CJ Hare
 */
public class NetWorthSummaryEvent implements NetWorthEvent {

	private final BigDecimal equityBalance;
	private final BigDecimal equityBalanceValue;
	private final BigDecimal cashBalance;
	private final BigDecimal networth;
	private final LocalDate eventDate;
	private final NetWorthEventType type;

	public NetWorthSummaryEvent( final BigDecimal equityBalance, final BigDecimal equityBalanceValue,
	        final BigDecimal cashBalance, final BigDecimal networth, final LocalDate eventDate,
	        final NetWorthEventType type ) {

		this.equityBalance = equityBalance;
		this.equityBalanceValue = equityBalanceValue;
		this.cashBalance = cashBalance;
		this.networth = networth;
		this.eventDate = eventDate;
		this.type = type;
	}

	@Override
	public BigDecimal equityBalance() {

		return equityBalance;
	}

	@Override
	public BigDecimal equityBalanceValue() {

		return equityBalanceValue;
	}

	@Override
	public BigDecimal cashBalance() {

		return cashBalance;
	}

	@Override
	public BigDecimal netWorth() {

		return networth;
	}

	@Override
	public LocalDate eventDate() {

		return eventDate;
	}

	@Override
	public NetWorthEventType type() {

		return type;
	}
}