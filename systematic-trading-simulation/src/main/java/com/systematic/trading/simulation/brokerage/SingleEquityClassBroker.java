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
package com.systematic.trading.simulation.brokerage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.model.equity.EquityClass;
import com.systematic.trading.model.equity.EquityIdentity;
import com.systematic.trading.model.price.Price;
import com.systematic.trading.model.price.TradingDayPrices;
import com.systematic.trading.simulation.brokerage.event.BrokerageAccountEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent.BrokerageAccountEventType;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.brokerage.exception.InsufficientEquitiesException;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent.EquityEventType;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.equity.event.SingleEquityEvent;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeStructure;
import com.systematic.trading.simulation.order.EquityOrderVolume;

/**
 * Handles execution of trades and maintains the balance of equities.
 * 
 * @author CJ Hare
 */
public class SingleEquityClassBroker implements Brokerage {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Fee structure to apply to equity transactions. */
	private final BrokerageTransactionFeeStructure transactionFee;

	/** Fee structure to apply to funds under management. */
	private final EquityManagementFeeStructure equityManagementFee;

	/** Date of the last non-zero management fee. */
	private LocalDate lastManagementFee;

	/** Single type of equity class traded in. */
	private final EquityIdentity equity;

	/** Number of equities held. */
	private BigDecimal equityBalance;

	/** Deals with keeping track of the number of trades on a rolling basis. */
	private final MonthlyRollingCounter monthlyTradeCounter;

	/** Parties interested in listening to brokerage events. */
	private final List<BrokerageEventListener> brokerageListeners = new ArrayList<>();

	/** Parties interested in listening to equity events. */
	private final List<EquityEventListener> equityListeners = new ArrayList<>();

	/** Identifier for the broker. */
	private final String brokerName;

	public SingleEquityClassBroker(
	        final String brokerName,
	        final BrokerageTransactionFeeStructure fees,
	        final EquityManagementFeeStructure managementFees,
	        final EquityIdentity equity,
	        final LocalDate startDate ) {

		this.brokerName = brokerName;
		this.monthlyTradeCounter = new MonthlyRollingCounter();
		this.lastManagementFee = managementFees.lastManagementFeeDate(startDate);
		this.equityManagementFee = managementFees;
		this.transactionFee = fees;
		this.equityBalance = BigDecimal.ZERO;
		this.equity = equity;
	}

	@Override
	public void buy( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate ) {

		final BigDecimal tradeValue = price.price().multiply(volume.volume(), MATH_CONTEXT);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.cost(tradeValue, equity.type(), tradesThisMonth);

		// Adding the equity purchases to the balance
		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.add(volume.volume(), MATH_CONTEXT);

		// Record of the buy transaction
		notifyListeners(
		        new BrokerageAccountEvent(
		                startingEquityBalance,
		                equityBalance,
		                volume.volume(),
		                BrokerageAccountEventType.BUY,
		                tradeDate,
		                tradeValue,
		                tradeFee));
	}

	@Override
	public BigDecimal sell( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate )
	        throws InsufficientEquitiesException {

		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.subtract(volume.volume(), MATH_CONTEXT);

		if (equityBalance.compareTo(BigDecimal.ZERO) < 0) { throw new InsufficientEquitiesException(); }

		final BigDecimal tradeValue = price.price().multiply(volume.volume(), MATH_CONTEXT);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.cost(tradeValue, equity.type(), tradesThisMonth);

		// Record of the sell transaction
		notifyListeners(
		        new BrokerageAccountEvent(
		                startingEquityBalance,
		                equityBalance,
		                volume.volume(),
		                BrokerageAccountEventType.SELL,
		                tradeDate,
		                tradeValue,
		                tradeFee));

		return tradeValue.subtract(tradeFee, MATH_CONTEXT);
	}

	@Override
	public BigDecimal equityBalance() {

		return equityBalance;
	}

	@Override
	public BigDecimal cost( final BigDecimal tradeValue, final EquityClass type, final LocalDate tradeDate ) {

		return transactionFee.cost(tradeValue, type, monthlyTradeCounter.get(tradeDate));
	}

	@Override
	public BigDecimal cost( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate ) {

		final BigDecimal tradeValue = price.price().multiply(volume.volume(), MATH_CONTEXT);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.cost(tradeValue, equity.type(), tradesThisMonth);

		return tradeValue.add(tradeFee, MATH_CONTEXT);
	}

	@Override
	public void addListener( final BrokerageEventListener listener ) {

		if (!brokerageListeners.contains(listener)) {
			brokerageListeners.add(listener);
		}
	}

	@Override
	public void addListener( final EquityEventListener listener ) {

		if (!equityListeners.contains(listener)) {
			equityListeners.add(listener);
		}
	}

	@Override
	public void update( final TradingDayPrices tradingData ) {

		final LocalDate tradingDate = tradingData.date();

		final BigDecimal feeInEquities = equityManagementFee.update(equityBalance, lastManagementFee, tradingData);

		// Only when there's a fee apply & record an event
		if (BigDecimal.ZERO.compareTo(feeInEquities) != 0) {

			final BigDecimal startingEquityBalance = equityBalance;
			final BigDecimal transactionValue = tradingData.closingPrice().price()
			        .multiply(feeInEquities, MATH_CONTEXT);

			// Erode the original equity balance with the management fee
			equityBalance = equityBalance.subtract(feeInEquities, MATH_CONTEXT);

			lastManagementFee = equityManagementFee.lastManagementFeeDate(tradingDate);

			// TODO maybe a separate equity object to manage the events: fees / dividends / splits
			notifyListeners(
			        new SingleEquityEvent(
			                equity,
			                startingEquityBalance,
			                equityBalance,
			                feeInEquities,
			                EquityEventType.MANAGEMENT_FEE,
			                tradingDate,
			                transactionValue));
		}
	}

	@Override
	public String name() {

		return brokerName;
	}

	private void notifyListeners( final BrokerageEvent event ) {

		for (final BrokerageEventListener listener : brokerageListeners) {
			listener.event(event);
		}
	}

	private void notifyListeners( final EquityEvent event ) {

		for (final EquityEventListener listener : equityListeners) {
			listener.event(event);
		}
	}
}
