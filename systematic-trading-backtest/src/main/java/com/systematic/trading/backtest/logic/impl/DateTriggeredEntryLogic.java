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
package com.systematic.trading.backtest.logic.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.brokerage.BrokerageFees;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.order.BuyTomorrowAtAnyPriceOrder;
import com.systematic.trading.backtest.order.EquityOrder;
import com.systematic.trading.backtest.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.DataPoint;

/**
 * Frequent purchases of a fixed amount at regular intervals.
 * 
 * @author CJ Hare
 */
public class DateTriggeredEntryLogic implements EntryLogic {

	private final Period interval;
	private final BigDecimal amount;
	private final LocalDate lastOrder;

	public DateTriggeredEntryLogic( final LocalDate firstOrder, final Period interval, final BigDecimal amount ) {
		this.interval = interval;
		this.amount = amount;

		// The first order needs to be on that date, not interval after
		lastOrder = LocalDate.from( firstOrder ).minus( interval );
	}

	@Override
	public EquityOrder update( final BrokerageFees fees, final CashAccount cashAccount, final DataPoint data ) {

		if (data.getDate().isAfter( lastOrder.plus( interval ) )) {
			final EquityOrderVolume volume = EquityOrderVolume.valueOf( amount );
			return new BuyTomorrowAtAnyPriceOrder( volume );
		}

		return null;
	}

	@Override
	public EquityOrderInsufficientFundsAction actionOnInsufficentFunds( final EquityOrder order ) {
		return EquityOrderInsufficientFundsAction.RESUMIT;
	}
}
