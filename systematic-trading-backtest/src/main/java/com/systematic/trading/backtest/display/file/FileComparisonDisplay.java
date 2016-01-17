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
package com.systematic.trading.backtest.display.file;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;

import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.statistics.CashEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.analysis.statistics.OrderEventStatistics;

/**
 * Persists the comparison displays into a file.
 * <p/>
 * Assumption is the directory is already empty.
 * 
 * @author CJ Hare
 */
public class FileComparisonDisplay extends FileDisplayMultithreading implements NetWorthEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".00" );

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );

	private final MathContext mathContext;

	private final EventStatistics statistics;

	public FileComparisonDisplay( final EventStatistics statistics, final String outputFilename,
			final ExecutorService pool, final MathContext mathContext ) {
		super( outputFilename, pool );
		this.mathContext = mathContext;
		this.statistics = statistics;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE.equals( state )) {
			write( createOutput( statistics, event ) );
		}
	}

	private String createOutput( final EventStatistics statistics, final NetWorthEvent event ) {

		final BigDecimal balance = event.getEquityBalance();
		final BigDecimal holdingValue = event.getEquityBalanceValue();
		final BigDecimal cashBalance = event.getCashBalance();
		final BigDecimal netWorth = event.getNetWorth();
		final OrderEventStatistics orders = statistics.getOrderEventStatistics();

		final int entryEventCount = orders.getEntryEventCount();
		final int entryEventDeletedCount = orders.getDeleteEntryEventCount();
		final int entryEventExecutedCount = orders.getEntryEventCount() - orders.getDeleteEntryEventCount();

		final int exitEventCount = orders.getExitEventCount();
		final int exitEventDeletedCount = orders.getDeleteExitEventCount();
		final int exitEventExecutedCount = orders.getExitEventCount() - orders.getDeleteExitEventCount();

		final CashEventStatistics cash = statistics.getCashEventStatistics();
		final BigDecimal deposited = cash.getAmountDeposited();
		final BigDecimal profit = netWorth.subtract( deposited, mathContext );
		final BigDecimal roi = profit.divide( netWorth, mathContext ).multiply( ONE_HUNDRED, mathContext );

		return String.format(
				"ROI: %s, Total Net Worth: %s, Number of equities: %s, Holdings value: %s, Cash account: %s, Deposited: %s, Profit: %s,  Entry orders placed: %s, Entry orders executed: %s, Entry orders deleted: %s, Exit orders placed: %s, Exit orders executed: %s, Exit orders deleted: %s, %s",
				TWO_DECIMAL_PLACES.format( roi ), TWO_DECIMAL_PLACES.format( netWorth ),
				TWO_DECIMAL_PLACES.format( balance ), TWO_DECIMAL_PLACES.format( holdingValue ),
				TWO_DECIMAL_PLACES.format( cashBalance ), TWO_DECIMAL_PLACES.format( deposited ),
				TWO_DECIMAL_PLACES.format( profit ), entryEventCount, entryEventExecutedCount, entryEventDeletedCount,
				exitEventCount, exitEventExecutedCount, exitEventDeletedCount, event.getDescription() );
	}
}
