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
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.event.brokerage.BrokerageEvent;
import com.systematic.trading.event.brokerage.BrokerageEventListener;
import com.systematic.trading.event.cash.CashEvent;
import com.systematic.trading.event.cash.CashEventListener;
import com.systematic.trading.event.data.TickerSymbolTradingRange;
import com.systematic.trading.event.order.OrderEvent;
import com.systematic.trading.event.order.OrderEventListener;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class FileEventDisplay implements CashEventListener, OrderEventListener, BrokerageEventListener {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileEventDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	private final String outputFilename;

	public FileEventDisplay( final String outputFilename, final TickerSymbolTradingRange range ) {
		this.outputFilename = outputFilename;

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}

		try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
			out.println( createHeaderOutput( range ) );
		} catch (final IOException e) {
			LOG.error( e );
		}
	}

	private String createHeaderOutput( final TickerSymbolTradingRange range ) {

		final StringBuilder output = new StringBuilder();
		output.append( "\n" );
		output.append( "#######################\n" );
		output.append( "### Backtest Events ###\n" );
		output.append( "#######################\n" );
		output.append( "\n" );

		output.append( String.format( "Data set for %s from %s to %s\n", range.getTickerSymbol(), range.getStartDate(),
				range.getEndDate() ) );

		final long daysBetween = ChronoUnit.DAYS.between( range.getStartDate(), range.getEndDate() );
		final double percentageTradingDays = ((double) range.getNumberOfTradingDays() / daysBetween) * 100;

		output.append( String.format( "# trading days: %s over %s days (%s percentage trading days)\n",
				range.getNumberOfTradingDays(), daysBetween, TWO_DECIMAL_PLACES.format( percentageTradingDays ) ) );

		output.append( "\n" );

		return output.toString();
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

	@Override
	public void event( final OrderEvent event ) {

		try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
			final String output = String.format( "Place Order - %s total cost %s created after c.o.b on %s",
					event.getType(), TWO_DECIMAL_PLACES.format( event.getTotalCost() ), event.getTransactionDate() );

			out.println( output );
		} catch (final IOException e) {
			LOG.error( e );
		}

	}

	@Override
	public void event( final CashEvent event ) {

		try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
			final String output = String.format( "Cash Account - %s: %s - funds %s -> %s on %s", event.getType(),
					TWO_DECIMAL_PLACES.format( event.getAmount() ),
					TWO_DECIMAL_PLACES.format( event.getFundsBefore() ),
					TWO_DECIMAL_PLACES.format( event.getFundsAfter() ), event.getTransactionDate() );

			out.println( output );
		} catch (final IOException e) {
			LOG.error( e );
		}

	}
}
