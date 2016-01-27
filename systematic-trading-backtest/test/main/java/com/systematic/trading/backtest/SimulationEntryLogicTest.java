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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import com.systematic.trading.simulation.order.EquityOrder;
import com.systematic.trading.simulation.order.exception.OrderException;

/**
 * Tests the Cash Account component of the simulation class.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationEntryLogicTest {

	private static LocalDate[] UNORDERED_DATE = { LocalDate.of( 2000, Month.APRIL, 1 ),
			LocalDate.of( 2000, Month.APRIL, 9 ), LocalDate.of( 2000, Month.APRIL, 2 ),
			LocalDate.of( 2000, Month.APRIL, 3 ), LocalDate.of( 2000, Month.APRIL, 4 ),
			LocalDate.of( 2000, Month.APRIL, 8 ), LocalDate.of( 2000, Month.APRIL, 6 ),
			LocalDate.of( 2000, Month.APRIL, 7 ), LocalDate.of( 2000, Month.APRIL, 5 ) };

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

	private TradingDayPrices[] createOrderedDataPoints( final TradingDayPrices[] unordered ) {
		final TradingDayPrices[] ordered = new TradingDayPrices[unordered.length];

		ordered[0] = unordered[0];
		ordered[1] = unordered[2];
		ordered[2] = unordered[3];
		ordered[3] = unordered[4];
		ordered[4] = unordered[8];
		ordered[5] = unordered[6];
		ordered[6] = unordered[7];
		ordered[7] = unordered[5];
		ordered[8] = unordered[1];

		return ordered;
	}

	@Test
	public void processOrder() throws OrderException {
		final EquityIdentity equity = new EquityIdentity( "A", EquityClass.STOCK, 4 );
		final TradingDayPrices[] sortedPoints = createOrderedDataPoints( createUnorderedDataPoints() );
		final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, sortedPoints );
		final Simulation simulation = new Simulation( tradingData, broker, funds, roiCalculator, entry, exit );

		final EquityOrder order = mock( EquityOrder.class );
		when( order.areExecutionConditionsMet( any( TradingDayPrices.class ) ) ).thenReturn( true );
		when( order.isValid( any( TradingDayPrices.class ) ) ).thenReturn( true );

		when( entry.update( broker, funds, sortedPoints[1] ) ).thenReturn( order );

		simulation.run();

		verify( order ).areExecutionConditionsMet( sortedPoints[2] );
		verify( order ).isValid( sortedPoints[2] );
		verify( order ).execute( broker, broker, funds, sortedPoints[2] );
	}
}
