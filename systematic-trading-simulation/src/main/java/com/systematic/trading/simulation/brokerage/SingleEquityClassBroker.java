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
package com.systematic.trading.simulation.brokerage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.Price;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.simulation.brokerage.event.BrokerageAccountEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent.BrokerageAccountEventType;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.simulation.equity.event.EquityEvent;
import com.systematic.trading.simulation.equity.event.EquityEventListener;
import com.systematic.trading.simulation.equity.event.SingleEquityEvent;
import com.systematic.trading.simulation.equity.event.EquityEvent.EquityEventType;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeStructure;
import com.systematic.trading.simulation.order.EquityOrderVolume;
import com.systematic.trading.simulation.order.exception.InsufficientEquitiesException;

/**
 * Handles execution of trades and maintains the balance of equities.
 * 
 * @author CJ Hare
 */
public class SingleEquityClassBroker implements Brokerage {

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

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Parties interested in listening to brokerage events. */
	private final List<BrokerageEventListener> brokerageListeners = new ArrayList<>();

	/** Parties interested in listening to equity events. */
	private final List<EquityEventListener> equityListeners = new ArrayList<>();

	/** Identifier for the broker.*/
	private final String brokerName;

	public SingleEquityClassBroker(final String brokerName, final BrokerageTransactionFeeStructure fees,
	        final EquityManagementFeeStructure managementFees, final EquityIdentity equity, final LocalDate startDate,
	        final MathContext mathContext) {
		this.brokerName = brokerName;
		this.monthlyTradeCounter = new MonthlyRollingCounter();
		this.lastManagementFee = managementFees.getLastManagementFeeDate(startDate);
		this.equityManagementFee = managementFees;
		this.transactionFee = fees;
		this.equityBalance = BigDecimal.ZERO;
		this.mathContext = mathContext;
		this.equity = equity;
	}

	@Override
	public void buy( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate ) {

		final BigDecimal tradeValue = price.getPrice().multiply(volume.getVolume(), mathContext);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.calculateFee(tradeValue, equity.getType(), tradesThisMonth);

		// Adding the equity purchases to the balance
		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.add(volume.getVolume(), mathContext);

		// Record of the buy transaction
		notifyListeners(new BrokerageAccountEvent(startingEquityBalance, equityBalance, volume.getVolume(),
		        BrokerageAccountEventType.BUY, tradeDate, tradeValue, tradeFee));
	}

	@Override
	public BigDecimal sell( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate )
	        throws InsufficientEquitiesException {

		final BigDecimal startingEquityBalance = equityBalance;
		equityBalance = equityBalance.subtract(volume.getVolume(), mathContext);

		if (equityBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientEquitiesException();
		}

		final BigDecimal tradeValue = price.getPrice().multiply(volume.getVolume(), mathContext);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.calculateFee(tradeValue, equity.getType(), tradesThisMonth);

		// Record of the sell transaction
		notifyListeners(new BrokerageAccountEvent(startingEquityBalance, equityBalance, volume.getVolume(),
		        BrokerageAccountEventType.SELL, tradeDate, tradeValue, tradeFee));

		return tradeValue.subtract(tradeFee, mathContext);
	}

	@Override
	public BigDecimal getEquityBalance() {
		return equityBalance;
	}

	@Override
	public BigDecimal calculateFee( final BigDecimal tradeValue, final EquityClass type, final LocalDate tradeDate ) {
		return transactionFee.calculateFee(tradeValue, type, monthlyTradeCounter.get(tradeDate));
	}

	/**
	 * Retrieves the current number of equities held.
	 * 
	 * @return the number of single type equities held, including any fractions.
	 */
	public BigDecimal getBalance() {
		return equityBalance;
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

	@Override
	public BigDecimal calculateBuy( final Price price, final EquityOrderVolume volume, final LocalDate tradeDate ) {
		final BigDecimal tradeValue = price.getPrice().multiply(volume.getVolume(), mathContext);
		final int tradesThisMonth = monthlyTradeCounter.add(tradeDate);
		final BigDecimal tradeFee = transactionFee.calculateFee(tradeValue, equity.getType(), tradesThisMonth);

		return tradeValue.add(tradeFee, mathContext);
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

		final LocalDate tradingDate = tradingData.getDate();

		final BigDecimal feeInEquities = equityManagementFee.update(equityBalance, lastManagementFee, tradingData);

		// Only when there's a fee apply & record an event
		if (BigDecimal.ZERO.compareTo(feeInEquities) != 0) {

			final BigDecimal startingEquityBalance = equityBalance;
			final BigDecimal transactionValue = tradingData.getClosingPrice().getPrice().multiply(feeInEquities,
			        mathContext);

			// Erode the original equity balance with the management fee
			equityBalance = equityBalance.subtract(feeInEquities, mathContext);

			lastManagementFee = equityManagementFee.getLastManagementFeeDate(tradingDate);

			// TODO maybe a separate equity object to manage the events: fees / dividends / splits
			notifyListeners(new SingleEquityEvent(equity, startingEquityBalance, equityBalance, feeInEquities,
			        EquityEventType.MANAGEMENT_FEE, tradingDate, transactionValue));
		}
	}

	@Override
	public String getName() {
		return brokerName;
	}
}
