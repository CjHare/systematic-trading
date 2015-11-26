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
package com.systematic.trading.backtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.model.TickerSymbolTradingDataBacktest;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.Simulation;
import com.systematic.trading.simulation.analysis.roi.ReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;

/**
 * Testing the basic functionality of the simulation.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationTest {

	private static LocalDate[] UNORDERED_DATE = { LocalDate.of( 2000, Month.APRIL, 1 ),
			LocalDate.of( 2000, Month.APRIL, 9 ), LocalDate.of( 2000, Month.APRIL, 2 ),
			LocalDate.of( 2000, Month.APRIL, 3 ), LocalDate.of( 2000, Month.APRIL, 4 ),
			LocalDate.of( 2000, Month.APRIL, 8 ), LocalDate.of( 2000, Month.APRIL, 6 ),
			LocalDate.of( 2000, Month.APRIL, 7 ), LocalDate.of( 2000, Month.APRIL, 5 ) };

	private static final LocalDate startDate = LocalDate.of( 2000, Month.APRIL, 1 );
	private static final LocalDate endDate = LocalDate.of( 2000, Month.APRIL, 9 );

	@Mock
	private Brokerage broker;
	@Mock
	private CashAccount funds;
	@Mock
	private EntryLogic entry;
	@Mock
	private ExitLogic exit;
	@Mock
	private ReturnOnInvestmentCalculator roiCalculator;

	private TradingDayPrices[] createUnorderedDataPoints() {
		final TradingDayPrices[] unordered = new TradingDayPrices[UNORDERED_DATE.length];

		for (int i = 0; i < unordered.length; i++) {
			unordered[i] = mock( TradingDayPrices.class );
			when( unordered[i].getDate() ).thenReturn( UNORDERED_DATE[i] );
			when( unordered[i].toString() ).thenReturn( UNORDERED_DATE[i].toString() );
		}

		return unordered;
	}

	@Test
	public void create() {
		final EquityIdentity equity = new EquityIdentity( "A", EquityClass.STOCK );
		final TradingDayPrices[] unorderedPoints = createUnorderedDataPoints();
		final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, startDate, endDate,
				unorderedPoints );

		new Simulation( tradingData, broker, funds, roiCalculator, entry, exit );
	}

	@Test
	public void createWithException() {
		final TradingDayPrices[] unorderedPoints = createUnorderedDataPoints();
		unorderedPoints[1] = unorderedPoints[0];

		final EquityIdentity equity = new EquityIdentity( "A", EquityClass.STOCK );

		try {
			final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, startDate, endDate,
					unorderedPoints );
			new Simulation( tradingData, broker, funds, roiCalculator, entry, exit );

			fail( "Expecting exception for duplicate data point date" );
		} catch (final IllegalArgumentException e) {
			assertEquals( "Duplicate trading dates provided", e.getMessage() );
		}
	}

}
