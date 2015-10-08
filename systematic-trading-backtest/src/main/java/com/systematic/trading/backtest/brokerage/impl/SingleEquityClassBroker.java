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
package com.systematic.trading.backtest.brokerage.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.event.BrokerageEvent.BrokerageAccountEventType;
import com.systematic.trading.backtest.event.impl.BrokerageAccountEvent;
import com.systematic.trading.backtest.exception.InsufficientEquitiesException;
import com.systematic.trading.backtest.exception.UnsupportedEquityClass;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.price.Price;
import com.systematic.trading.event.recorder.EventRecorder;

/**
 * Handles execution of trades and maintains the balance of equities.
 * 
 * @author CJ Hare
 */
public class SingleEquityClassBroker implements Brokerage {

	/** Fee structure to apply to equity transactions. */
	private final BrokerageFeeStructure fees;

	/** Single type of equity class traded in. */
	private final EquityClass type;

	/** Number of equities held. */
	private BigDecimal equityBalance;

	/** Deals with keeping track of the number of trades on a rolling basis. */
	private final MonthlyRollingCounter monthlyTradeCounter;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Record keeper for transactions from the Cash Account. */
	private final EventRecorder event;

	public SingleEquityClassBroker( final BrokerageFeeStructure fees, final EquityClass type, EventRecorder recorder,
			final MathContext mathContext ) {
		this.fees = fees;
		this.type = type;
		this.monthlyTradeCounter = new MonthlyRollingCounter();
		this.equityBalance = BigDecimal.ZERO;
		this.mathContext = mathContext;
		this.event = recorder;
	}

	@Override
	public BigDecimal buy( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate ) {

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), mathContext );
		final int tradesThisMonth = monthlyTradeCounter.add( tradeDate );
		final BigDecimal tradeFee = fees.calculateFee( tradeValue, type, tradesThisMonth );

		// Adding the equity purchases to the balance
		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.add( volume.getVolume(), mathContext );

		// Record of the buy transaction
		event.record( new BrokerageAccountEvent( startingEquityBalance, equityBalance, volume.getVolume(),
				BrokerageAccountEventType.BUY, tradeDate, tradeValue, tradeFee ) );

		return tradeValue.add( tradeFee, mathContext );
	}

	@Override
	public BigDecimal sell( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate )
			throws InsufficientEquitiesException {

		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.subtract( volume.getVolume(), mathContext );

		if (equityBalance.compareTo( BigDecimal.ZERO ) < 0) {
			throw new InsufficientEquitiesException();
		}

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), mathContext );
		final int tradesThisMonth = monthlyTradeCounter.add( tradeDate );
		final BigDecimal tradeFee = fees.calculateFee( tradeValue, type, tradesThisMonth );

		// Record of the sell transaction
		event.record( new BrokerageAccountEvent( startingEquityBalance, equityBalance, volume.getVolume(),
				BrokerageAccountEventType.SELL, tradeDate, tradeValue, tradeFee ) );

		return tradeValue.subtract( tradeFee, mathContext );
	}

	@Override
	public BigDecimal getEquityBalance() {
		return equityBalance;
	}

	@Override
	public BigDecimal calculateFee( final BigDecimal tradeValue, final EquityClass type, final LocalDate tradeDate )
			throws UnsupportedEquityClass {
		return fees.calculateFee( tradeValue, type, monthlyTradeCounter.get( tradeDate ) );
	}

	public BigDecimal getBalance() {
		return equityBalance;
	}
}
