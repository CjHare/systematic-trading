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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.SimulationStateListener.SimulationState;
import com.systematic.trading.backtest.analysis.networth.NetWorthEvent;
import com.systematic.trading.backtest.display.NetWorthComparisonDisplay;

/**
 * Persists the comparison displays into a file.
 * 
 * @author CJ Hare
 */
public class FileNetWorthComparisonDisplay implements NetWorthComparisonDisplay {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileNetWorthComparisonDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".00" );

	private final String outputFilename;

	private String description;

	public FileNetWorthComparisonDisplay( final String outputFilename ) {

		// Ensure the directory exists
		final File outputDirectoryFile = new File( outputFilename ).getParentFile();
		if (!outputDirectoryFile.exists()) {
			if (!outputDirectoryFile.mkdirs()) {
				throw new IllegalArgumentException( String.format(
						"Failed to create / access directory parent directory: %s", outputFilename ) );
			}
		}

		// Ensure the directory is empty
		for (final File file : outputDirectoryFile.listFiles()) {
			file.delete();
		}

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}

		this.outputFilename = outputFilename;
	}

	public void setDescription( final String description ) {
		this.description = description;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE.equals( state )) {

			try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
				out.println( createOutput( event ) );
			} catch (final IOException e) {
				LOG.error( e );
			}
		}
	}

	private String createOutput( final NetWorthEvent event ) {

		final BigDecimal balance = event.getEquityBalance();
		final BigDecimal holdingValue = event.getEquityBalanceValue();
		final BigDecimal cashBalance = event.getCashBalance();
		final BigDecimal netWorth = event.getNetWorth();

		return String.format( "Total Net Worth: %s, Number of equities: %s, Holdings value: %s, Cash account: %s, %s",
				TWO_DECIMAL_PLACES.format( netWorth ), TWO_DECIMAL_PLACES.format( balance ),
				TWO_DECIMAL_PLACES.format( holdingValue ), TWO_DECIMAL_PLACES.format( cashBalance ), description );
	}
}
