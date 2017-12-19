/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.signal.model.DatedSignal;
import com.systematic.trading.simulation.brokerage.BrokerageTransaction;
import com.systematic.trading.simulation.brokerage.BrokerageTransactionFee;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.EquityOrderInsufficientFundsAction;
import com.systematic.trading.strategy.entry.Entry;
import com.systematic.trading.strategy.entry.size.EntrySize;
import com.systematic.trading.strategy.exit.Exit;
import com.systematic.trading.strategy.exit.size.ExitSize;

/**
 * Verifying the Trading Strategy delegates correctly to the injected dependencies.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyTest {

	private static final EquityClass TYPE = EquityClass.STOCK;
	private static final int SCALE = 55;

	@Mock
	private Entry entry;

	@Mock
	private EntrySize entryPositionSizing;

	@Mock
	private Exit exit;

	@Mock
	private ExitSize exitPositionSizing;

	@Mock
	private BrokerageTransactionFee fees;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private BrokerageTransaction brokerage;

	@Mock
	private TradingDayPrices data;

	/** Strategy instance being tested. */
	private Strategy strategy;

	@Before
	public void setUp() {
		strategy = new TradingStrategy(entry, entryPositionSizing, exit, exitPositionSizing, TYPE, SCALE);

		setUpTradingDayPrices();
	}

	@Test
	public void actionOnInsufficentFunds() {
		final EquityOrder order = setUpOrder();

		final EquityOrderInsufficientFundsAction action = strategy.actionOnInsufficentFunds(order);

		verifyInsufficentFundsAction(EquityOrderInsufficientFundsAction.DELETE, action);
	}

	/**
	 * Warm up period adds a conversion from trading days to normal weekdays (including non-trading days).
	 */
	@Test
	public void warmUpPeriod() {
		setUpWarmupPeriod(5);

		final Period warmUp = strategy.warmUpPeriod();

		verifyWarmUpPeriod(8, warmUp);
	}

	@Test
	public void entryUpdateNoEnoughPriceData() {
		setUpNotEnoughDataPricePoints();

		final Optional<EquityOrder> order = entryTick();

		verifyNoOrder(order);
	}

	@Test
	public void entryUpdate() {
		setUpSignal(0);
		setUpEntryPositionSizing(3.67);
		setUpFees(0.88);

		final Optional<EquityOrder> order = entryTick();

		verifyOrder(order);
		verifyFeeDelegation(3.67);
		verifyAnalysisDelegation();
		verifyEntryPositionSizingDelegation();
	}

	@Test
	public void entryUpdateNoOrder() {
		setUpSignal(0);
		setUpEntryPositionSizing(0.75);
		setUpFees(0.88);

		final Optional<EquityOrder> order = entryTick();

		verifyNoOrder(order);
		verifyFeeDelegation(0.75);
		verifyAnalysisDelegation();
		verifyEntryPositionSizingDelegation();
	}

	@Test
	public void entryUpdateNoOrderSignalYesterday() {
		setUpSignal(1);

		final Optional<EquityOrder> order = entryTick();

		verifyNoOrder(order);
		verifyAnalysisDelegation();
	}

	@Test
	public void entryUpdateNoAction() {

		final Optional<EquityOrder> order = entryTick();

		verifyNoOrder(order);
	}

	@Test
	public void exitUpdateNoAction() {

		final Optional<EquityOrder> order = exitTick();

		verifyNoOrder(order);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exitUpdate() {
		setUpExitUpdateAction();

		exitTick();
	}

	private Optional<EquityOrder> entryTick() {
		return strategy.entryTick(fees, cashAccount, data);
	}

	private Optional<EquityOrder> exitTick() {
		return strategy.exitTick(brokerage, data);
	}

	private void setUpSignal( final int ticksPrevious ) {
		final DatedSignal signal = mock(DatedSignal.class);
		when(signal.date()).thenReturn(LocalDate.now().minusDays(ticksPrevious));
		final List<DatedSignal> signals = new ArrayList<>();
		signals.add(signal);
		when(entry.analyse(any(TradingDayPrices[].class))).thenReturn(signals);
	}

	private void setUpNotEnoughDataPricePoints() {
		when(entry.numberOfTradingDaysRequired()).thenReturn(9);
	}

	private void setUpTradingDayPrices() {
		when(data.date()).thenReturn(LocalDate.now());

		final ClosingPrice closing = mock(ClosingPrice.class);
		when(closing.getPrice()).thenReturn(BigDecimal.ONE);
		when(data.closingPrice()).thenReturn(closing);
	}

	private void setUpFees( final double value ) {
		when(fees.cost(any(BigDecimal.class), any(EquityClass.class), any(LocalDate.class)))
		        .thenReturn(BigDecimal.valueOf(value));
	}

	private void setUpExitUpdateAction() {
		when(exit.exitTick(any(BrokerageTransaction.class), any(TradingDayPrices.class))).thenReturn(true);
	}

	private void setUpWarmupPeriod( final int period ) {
		when(entry.numberOfTradingDaysRequired()).thenReturn(period);
	}

	private void setUpEntryPositionSizing( final double value ) {
		when(entryPositionSizing.entryPositionSize(any(CashAccount.class))).thenReturn(BigDecimal.valueOf(value));
	}

	private EquityOrder setUpOrder() {
		return mock(EquityOrder.class);
	}

	private void verifyWarmUpPeriod( final int expected, final Period actual ) {
		assertNotNull(actual);
		assertEquals(Period.ofDays(expected), actual);
	}

	private void verifyInsufficentFundsAction( final EquityOrderInsufficientFundsAction expected,
	        final EquityOrderInsufficientFundsAction actual ) {
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	private void verifyNoOrder( final Optional<EquityOrder> actual ) {
		assertNotNull(actual);
		assertEquals("Not expecting any order", false, actual.isPresent());
	}

	private void verifyOrder( final Optional<EquityOrder> actual ) {
		assertNotNull(actual);
		assertNotNull(actual.get());
	}

	private void verifyAnalysisDelegation() {
		verify(entry).analyse(any(TradingDayPrices[].class));
	}

	private void verifyEntryPositionSizingDelegation() {
		verify(entryPositionSizing).entryPositionSize(cashAccount);
	}

	private void verifyFeeDelegation( final double entrySizePosition ) {
		verify(fees).cost(BigDecimal.valueOf(entrySizePosition), TYPE, LocalDate.now());
	}
}