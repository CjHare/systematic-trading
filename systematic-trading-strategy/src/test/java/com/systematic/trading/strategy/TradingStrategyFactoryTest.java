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
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.model.EquityClass;
import com.systematic.trading.strategy.confirmation.Confirmation;
import com.systematic.trading.strategy.entry.Entry;
import com.systematic.trading.strategy.entry.TradingStrategyConfirmationEntry;
import com.systematic.trading.strategy.entry.TradingStrategyIndicatorEntry;
import com.systematic.trading.strategy.entry.TradingStrategyOperatorEntry;
import com.systematic.trading.strategy.entry.TradingStrategyPeriodicEntry;
import com.systematic.trading.strategy.entry.size.EntrySize;
import com.systematic.trading.strategy.exit.Exit;
import com.systematic.trading.strategy.exit.TradingStrategyExit;
import com.systematic.trading.strategy.exit.size.ExitSize;
import com.systematic.trading.strategy.indicator.Indicator;
import com.systematic.trading.strategy.operator.Operator;
import com.systematic.trading.strategy.operator.TradingStrategyAndOperator;
import com.systematic.trading.strategy.operator.TradingStrategyOrOperator;
import com.systematic.trading.strategy.periodic.Periodic;

/**
 * Verifying the trading strategy factory is creating the correct instances.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyFactoryTest {

	@Mock
	private Entry leftEntry;

	@Mock
	private Entry righEntry;

	/** Factory instance being tested. */
	private TradingStrategyFactory factory;

	@Before
	public void setUp() {

		factory = new TradingStrategyFactory();
	}

	@Test
	public void strategy() {

		final Entry entry = mock(Entry.class);
		final EntrySize entryPositionSizing = mock(EntrySize.class);
		final Exit exit = mock(Exit.class);
		final ExitSize exitPositionSizing = mock(ExitSize.class);

		final Strategy strategy = factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing,
		        EquityClass.STOCK, 2);

		assertEquals(TradingStrategy.class, strategy.getClass());
	}

	@Test
	public void confirmedByEntry() {

		final Entry entry = factory.entry(leftEntry, mock(Confirmation.class), righEntry);

		assertEquals(TradingStrategyConfirmationEntry.class, entry.getClass());
	}

	@Test
	public void orOperatorEntry() {

		final Entry entry = factory.entry(leftEntry, new TradingStrategyOrOperator(), righEntry);

		assertEquals(TradingStrategyOperatorEntry.class, entry.getClass());
	}

	@Test
	public void andOperatorEntry() {

		final Entry entry = factory.entry(leftEntry, new TradingStrategyAndOperator(), righEntry);

		assertEquals(TradingStrategyOperatorEntry.class, entry.getClass());
	}

	@Test
	public void indicatorEntry() {

		final Indicator indicator = mock(Indicator.class);

		final Entry entry = factory.entry(indicator);

		assertEquals(TradingStrategyIndicatorEntry.class, entry.getClass());
	}

	@Test
	public void periodicEntry() {

		final Periodic indicator = mock(Periodic.class);

		final Entry entry = factory.entry(indicator);

		assertEquals(TradingStrategyPeriodicEntry.class, entry.getClass());
	}

	@Test
	public void exit() {

		final Exit exit = factory.exit();

		assertEquals(TradingStrategyExit.class, exit.getClass());
	}

	@Test
	public void orOperator() {

		final Operator op = factory.operator(Operator.Selection.OR);

		assertEquals(TradingStrategyOrOperator.class, op.getClass());
	}

	@Test
	public void andOperator() {

		final Operator op = factory.operator(Operator.Selection.AND);

		assertEquals(TradingStrategyAndOperator.class, op.getClass());
	}
}