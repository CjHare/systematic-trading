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

import com.systematic.trading.backtest.analysis.CumulativeReturnOnInvestment;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.display.NetWorthSummaryDisplay;
import com.systematic.trading.data.TradingDayPrices;

/**
 * Displays to the console the net worth.
 * 
 * @author CJ Hare
 */
public class FileNetWorthSummaryDisplay implements NetWorthSummaryDisplay {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileNetWorthSummaryDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	private final Brokerage broker;
	private final TradingDayPrices[] tradingData;
	private final CashAccount cashAccount;
	private final CumulativeReturnOnInvestment cumulativeRoi;

	private final String outputFilename;

	public FileNetWorthSummaryDisplay( final Brokerage broker, final TradingDayPrices[] tradingData,
			final CashAccount cashAccount, final CumulativeReturnOnInvestment cumulativeRoi, final String outputFilename ) {
		this.broker = broker;
		this.tradingData = tradingData;
		this.cashAccount = cashAccount;
		this.cumulativeRoi = cumulativeRoi;
		this.outputFilename = outputFilename;

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
	}

	@Override
	public void displayNetWorth() {
		try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
			out.println( createOutput() );
		} catch (final IOException e) {
			LOG.error( e );
		}
	}

	private String createOutput() {

		final TradingDayPrices latest = getLatestDataPoint( tradingData );
		final BigDecimal balance = broker.getEquityBalance();
		final BigDecimal lastClosingPrice = latest.getClosingPrice().getPrice();
		final BigDecimal holdingValue = balance.multiply( lastClosingPrice );
		final StringBuilder output = new StringBuilder();

		output.append( "\n=== Net Worth Summary ===\n" );
		output.append( String.format( "Number of equities: %s\n", TWO_DECIMAL_PLACES.format( balance ) ) );
		output.append( String.format( "Last closing price: %s\n", TWO_DECIMAL_PLACES.format( lastClosingPrice ) ) );
		output.append( String.format( "Holdings value: %s\n", TWO_DECIMAL_PLACES.format( holdingValue ) ) );
		output.append( String.format( "Cash account: %s\n", TWO_DECIMAL_PLACES.format( cashAccount.getBalance() ) ) );

		output.append( String.format( "\nTotal Net Worth: %s\n",
				TWO_DECIMAL_PLACES.format( cashAccount.getBalance().add( holdingValue ) ) ) );

		output.append( String.format( "\nInvestment Cumulative ROI: %s\n",
				TWO_DECIMAL_PLACES.format( cumulativeRoi.getCumulativeReturnOnInvestment() ) ) );

		return output.toString();
	}

	private TradingDayPrices getLatestDataPoint( final TradingDayPrices[] tradingDate ) {
		TradingDayPrices latest = tradingDate[0];

		for (int i = 1; i < tradingDate.length; i++) {
			if (tradingDate[i].getDate().isAfter( latest.getDate() )) {
				latest = tradingDate[i];
			}
		}

		return latest;
	}
}
