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

import java.io.IOException;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.impl.PeriodicCulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.analysis.statistics.impl.CumulativeEventStatistics;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityIdentity;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.configuration.BootstrapConfiguration;
import com.systematic.trading.backtest.configuration.impl.WeeklyBuyWeeklyDespoitConfiguration;
import com.systematic.trading.backtest.display.file.FileDisplay;
import com.systematic.trading.backtest.event.data.TickerSymbolTradingRangeImpl;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceImpl;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.event.data.TickerSymbolTradingRange;

/**
 * Performs back testing of trading logic over a historical data set.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrap {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 5 * DAYS_IN_A_YEAR;

	public static void main( final String... args ) throws IOException {

		// final BootstrapConfiguration configuration = new
		// MacdRsiSameDayEntryHoldForeverWeeklyDespositConfiguration(
		// MATH_CONTEXT );

		final BootstrapConfiguration configuration = new WeeklyBuyWeeklyDespoitConfiguration( MATH_CONTEXT );

		final EquityIdentity equity = configuration.getEquityIdentity();

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );
		final TradingDayPrices[] tradingData = getTradingData( equity, startDate, endDate );

		// First data point may not be the requested start date
		final LocalDate earliestDate = getEarliestDate( tradingData );

		// Displays the events as they are generated
		final TickerSymbolTradingRange tickerSymbolTradingRange = new TickerSymbolTradingRangeImpl( equity, startDate,
				endDate, tradingData.length );

		// Cumulative recording of investment progression
		final CulmativeReturnOnInvestmentCalculator roi = new CulmativeReturnOnInvestmentCalculator( MATH_CONTEXT );

		final PeriodicCulmativeReturnOnInvestmentCalculatorListener dailyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofDays( 1 ), MATH_CONTEXT );
		roi.addListener( dailyRoi );

		final PeriodicCulmativeReturnOnInvestmentCalculatorListener monthlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofMonths( 1 ), MATH_CONTEXT );
		roi.addListener( monthlyRoi );

		final PeriodicCulmativeReturnOnInvestmentCalculatorListener yearlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofYears( 1 ), MATH_CONTEXT );
		roi.addListener( yearlyRoi );

		final CulmativeReturnOnInvestmentCalculatorListener cumulativeRoi = new CulmativeReturnOnInvestmentCalculatorListener(
				MATH_CONTEXT );
		roi.addListener( cumulativeRoi );

		// Indicator triggered purchases
		final EntryLogic entry = configuration.getEntryLogic( equity, earliestDate );

		// Never sell
		final ExitLogic exit = configuration.getExitLogic();

		// Cash account with flat interest of 1.5% - $100 deposit weekly, zero starting balance
		final CashAccount cashAccount = configuration.getCashAccount( earliestDate );
		cashAccount.addListener( roi );

		// ETF Broker with CmC markets fees
		final Brokerage broker = configuration.getBroker( equity );

		// Engine dealing with the event flow
		final Simulation simulation = new Simulation( earliestDate, endDate, tradingData, broker, cashAccount, roi,
				entry, exit );

		// Statistics recorder for the various cash account, brokerage and order events
		final EventStatistics eventStatistics = new CumulativeEventStatistics();
		simulation.addListener( eventStatistics );
		broker.addListener( eventStatistics );
		cashAccount.addListener( eventStatistics );

		// Output display to files to analysis later
		final String outputDirectory = configuration.getOutputDirectory( equity );
		final FileDisplay display = new FileDisplay( tickerSymbolTradingRange, eventStatistics, broker, cashAccount,
				cumulativeRoi, tradingData, outputDirectory );
		simulation.addListener( display );
		broker.addListener( display );
		cashAccount.addListener( display );
		yearlyRoi.addListener( display );
		monthlyRoi.addListener( display );
		dailyRoi.addListener( display );

		simulation.run();

		HibernateUtil.getSessionFactory().close();

		// Display summaries files
		display.simulationCompleted();
	}

	private static TradingDayPrices[] getTradingData( final EquityIdentity equity, final LocalDate startDate,
			final LocalDate endDate ) {

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( equity.getTickerSymbol(), startDate, endDate );

		final DataService service = DataServiceImpl.getInstance();
		return service.get( equity.getTickerSymbol(), startDate, endDate );
	}

	private static LocalDate getEarliestDate( final TradingDayPrices[] data ) {
		LocalDate earliest = data[0].getDate();

		for (final TradingDayPrices today : data) {
			if (earliest.isAfter( today.getDate() )) {
				earliest = today.getDate();
			}
		}

		return earliest;
	}
}
