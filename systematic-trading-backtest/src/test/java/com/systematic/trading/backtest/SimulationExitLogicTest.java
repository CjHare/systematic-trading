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
package com.systematic.trading.backtest;

import static com.systematic.trading.backtest.matcher.TradingDayPricesDateMatcher.argumentMatches;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.Simulation;
import com.systematic.trading.simulation.analysis.roi.ReturnOnInvestmentListener;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.exception.OrderException;

/**
 * Tests the Cash Account component of the simulation class.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationExitLogicTest {

	private static LocalDate[] UNORDERED_DATE = { LocalDate.of(2000, Month.APRIL, 1),
	        LocalDate.of(2000, Month.APRIL, 9), LocalDate.of(2000, Month.APRIL, 2), LocalDate.of(2000, Month.APRIL, 3),
	        LocalDate.of(2000, Month.APRIL, 4), LocalDate.of(2000, Month.APRIL, 8), LocalDate.of(2000, Month.APRIL, 6),
	        LocalDate.of(2000, Month.APRIL, 7), LocalDate.of(2000, Month.APRIL, 5) };

	@Mock
	private Brokerage broker;

	@Mock
	private CashAccount funds;

	@Mock
	private EntryLogic entry;

	@Mock
	private ExitLogic exit;

	@Mock
	private ReturnOnInvestmentListener roiCalculator;

	@Mock
	private EquityOrder order;

	private Simulation simulation;

	@Before
	public void setUp() {

		TradingDayPrices[] sortedPoints = createTradingDayPrices();
		final EquityIdentity equity = new EquityIdentity("A", EquityClass.STOCK, 4);
		final TickerSymbolTradingData tradingData = new BacktestTickerSymbolTradingData(equity, sortedPoints);
		simulation = new Simulation(tradingData, broker, funds, roiCalculator, entry, exit);

	}

	@Test
	public void processOrder() throws OrderException {
		setUpExitOrder();

		simulationTick();

		verifyExitLogicTick();
		verifyExitOrder();
	}

	private void verifyExitLogicTick() {
		final LocalDate earliestDate = LocalDate.of(2000, Month.APRIL, 1);
		verify(exit).update(eq(broker), argumentMatches(earliestDate));
	}

	private void simulationTick() {
		simulation.run();
	}

	private void verifyExitOrder() throws OrderException {
		final LocalDate secondEarliestDate = LocalDate.of(2000, Month.APRIL, 2);
		verify(order).areExecutionConditionsMet(argumentMatches(secondEarliestDate));
		verify(order).isValid(argumentMatches(secondEarliestDate));
		verify(order).execute(eq(broker), eq(broker), eq(funds), argumentMatches(secondEarliestDate));
	}

	private void setUpExitOrder() {
		when(order.areExecutionConditionsMet(any(TradingDayPrices.class))).thenReturn(true);
		when(order.isValid(any(TradingDayPrices.class))).thenReturn(true);
		when(exit.update(any(Brokerage.class), any(TradingDayPrices.class))).thenReturn(order);
	}

	private TradingDayPrices createTradingDayPrices( final LocalDate date ) {
		TradingDayPrices price = mock(TradingDayPrices.class);
		when(price.getDate()).thenReturn(date);
		when(price.toString()).thenReturn(date.toString());
		return price;
	}

	private TradingDayPrices[] createTradingDayPrices() {
		final TradingDayPrices[] unordered = new TradingDayPrices[UNORDERED_DATE.length];

		for (int i = 0; i < unordered.length; i++) {
			unordered[i] = createTradingDayPrices(UNORDERED_DATE[i]);
		}

		Arrays.sort(unordered, ( TradingDayPrices o1, TradingDayPrices o2 ) -> o1.getDate().compareTo(o2.getDate()));

		return unordered;
	}
}