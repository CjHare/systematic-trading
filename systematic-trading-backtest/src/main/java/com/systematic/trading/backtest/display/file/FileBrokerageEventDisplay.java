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
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class FileBrokerageEventDisplay implements BrokerageEventListener {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileBrokerageEventDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	private final String outputFilename;

	public FileBrokerageEventDisplay( final String outputFilename ) {
		this.outputFilename = outputFilename;

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
	}

	@Override
	public void event( final BrokerageEvent event ) {

		try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
			final String output = String.format( "Brokerage Account - %s: %s - equity balance %s -> %s on %s",
					event.getType(), TWO_DECIMAL_PLACES.format( event.getEquityAmount() ),
					TWO_DECIMAL_PLACES.format( event.getStartingEquityBalance() ),
					TWO_DECIMAL_PLACES.format( event.getEndEquityBalance() ), event.getTransactionDate() );

			out.println( output );
		} catch (final IOException e) {
			LOG.error( e );
		}
	}
}
