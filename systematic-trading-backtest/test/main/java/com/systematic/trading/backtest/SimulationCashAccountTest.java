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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.data.DataPoint;

/**
 * Tests the Cash Account component of the simulation class.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationCashAccountTest {

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

	private DataPoint[] createUnorderedDataPoints() {
		final DataPoint[] unordered = new DataPoint[UNORDERED_DATE.length];

		for (int i = 0; i < unordered.length; i++) {
			unordered[i] = mock( DataPoint.class );
			when( unordered[i].getDate() ).thenReturn( UNORDERED_DATE[i] );
			when( unordered[i].toString() ).thenReturn( UNORDERED_DATE[i].toString() );
		}

		return unordered;
	}

	@Test
	public void runChronoligicallyOrderedDates() {
		final DataPoint[] unorderedPoints = createUnorderedDataPoints();
		final InOrder inOrder = inOrder( broker, funds, entry, exit );

		final Simulation simulation = new Simulation( unorderedPoints, broker, funds, entry, exit );

		simulation.run();

		final LocalDate[] sortedPoints = UNORDERED_DATE.clone();
		Arrays.sort( sortedPoints );

		for (int i = 0; i < sortedPoints.length; i++) {
			inOrder.verify( funds ).update( sortedPoints[i] );
		}
	}
}