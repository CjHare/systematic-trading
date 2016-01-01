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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.analysis.statistics.OrderEventStatistics;

/**
 * Persists the comparison displays into a file.
 * <p/>
 * Assumption is the directory is already empty.
 * 
 * @author CJ Hare
 */
public class FileNetWorthComparisonDisplay implements NetWorthEventListener {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileNetWorthComparisonDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".00" );

	private final String outputFilename;

	/** Pool of execution threads to delegate IO operations. */
	private final ExecutorService pool;

	private final EventStatistics statistics;

	public FileNetWorthComparisonDisplay( final EventStatistics statistics, final String outputFilename,
			final ExecutorService pool ) {
		this.statistics = statistics;
		this.outputFilename = outputFilename;
		this.pool = pool;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE.equals( state )) {

			// Create the output now, as it uses a field
			final String output = createOutput( statistics, event );

			final Runnable task = () -> {
				try (final PrintWriter out = new PrintWriter(
						new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
					out.println( output );
				} catch (final IOException e) {
					LOG.error( e );
				}
			};

			pool.execute( task );
		}
	}

	private String createOutput( final EventStatistics statistics, final NetWorthEvent event ) {

		final BigDecimal balance = event.getEquityBalance();
		final BigDecimal holdingValue = event.getEquityBalanceValue();
		final BigDecimal cashBalance = event.getCashBalance();
		final BigDecimal netWorth = event.getNetWorth();
		final OrderEventStatistics orders = statistics.getOrderEventStatistics();

		return String.format(
				"Total Net Worth: %s, Number of equities: %s, Holdings value: %s, Cash account: %s, Entry orders placed: %s, Entry orders deleted: %s, Exit orders placed: %s, Exit orders deleted: %s %s",
				TWO_DECIMAL_PLACES.format( netWorth ), TWO_DECIMAL_PLACES.format( balance ),
				TWO_DECIMAL_PLACES.format( holdingValue ), TWO_DECIMAL_PLACES.format( cashBalance ),
				orders.getEntryEventCount(), orders.getDeleteEntryEventCount(),

				orders.getExitEventCount(), orders.getDeleteExitEventCount(), event.getDescription() );
	}
}
