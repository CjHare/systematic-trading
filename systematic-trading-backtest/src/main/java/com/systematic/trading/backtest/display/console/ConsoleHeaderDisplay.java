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
package com.systematic.trading.backtest.display.console;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.event.data.TickerSymbolTradingRange;

/**
 * Displays the header text in the console.
 * 
 * @author CJ Hare
 */
public class ConsoleHeaderDisplay {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	public void displayHeader( final TickerSymbolTradingRange range ) {

		System.out.println( "\n" );
		System.out.println( "#######################" );
		System.out.println( "### Backtest Events ###" );
		System.out.println( "#######################" );
		System.out.println( "\n" );

		System.out.println( String.format( "Data set for %s from %s to %s", range.getTickerSymbol(),
				range.getStartDate(), range.getEndDate() ) );

		final long daysBetween = ChronoUnit.DAYS.between( range.getStartDate(), range.getEndDate() );
		final double percentageTradingDays = ((double) range.getNumberOfTradingDays() / daysBetween) * 100;

		System.out.println( String.format( "# trading days: %s over %s days (%s percentage trading days)",
				range.getNumberOfTradingDays(), daysBetween, TWO_DECIMAL_PLACES.format( percentageTradingDays ) ) );

		System.out.println( "\n" );
	}
}