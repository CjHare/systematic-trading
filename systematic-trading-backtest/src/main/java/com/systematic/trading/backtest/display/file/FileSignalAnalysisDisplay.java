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
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.signals.model.event.SignalAnalysisEvent;
import com.systematic.trading.signals.model.event.SignalAnalysisListener;

/**
 * Interested in displaying signal analysis events.
 * 
 * @author CJ Hare
 */
public class FileSignalAnalysisDisplay implements SignalAnalysisListener {
	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileSignalAnalysisDisplay.class );

	private final String outputFilename;

	/** Pool of execution threads to delegate IO operations. */
	private final ExecutorService pool;

	public FileSignalAnalysisDisplay( final String outputFilename, final ExecutorService pool ) {
		this.outputFilename = outputFilename;
		this.pool = pool;

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
	}

	@Override
	public void event( final SignalAnalysisEvent event ) {

		final Runnable task = () -> {
			try (final PrintWriter out = new PrintWriter(
					new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
				final String output = String.format( "Signal event: %s on date: %s", event.getSignalType(),
						event.getSignalDate() );

				out.println( output );
			} catch (final IOException e) {
				LOG.error( e );
			}
		};

		pool.execute( task );
	}
}
