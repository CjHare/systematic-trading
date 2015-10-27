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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.analysis.statistics.impl.CumulativeEventStatistics;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.brokerage.fees.impl.VanguardRetailFeeStructure;
import com.systematic.trading.backtest.brokerage.impl.SingleEquityClassBroker;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.cash.impl.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.backtest.cash.impl.FlatInterestRate;
import com.systematic.trading.backtest.cash.impl.RegularDepositCashAccountDecorator;
import com.systematic.trading.backtest.display.console.ConsoleEventDisplay;
import com.systematic.trading.backtest.display.console.ConsoleEventStatisticsDisplay;
import com.systematic.trading.backtest.display.console.ConsoleHeaderDisplay;
import com.systematic.trading.backtest.display.console.ConsoleNetWorthSummaryDisplay;
import com.systematic.trading.backtest.display.console.ConsoleReturnOnInvestmentDisplay;
import com.systematic.trading.backtest.event.data.TickerSymbolTradingRangeImpl;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.impl.DateTriggeredEntryLogic;
import com.systematic.trading.backtest.logic.impl.HoldForeverExitLogic;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.event.EventListener;
import com.systematic.trading.event.data.TickerSymbolTradingRange;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Assumption: no liquidity issues == can always execute a trade.
 * 
 * @author CJ Hare
 */
public class BacktestFrequentBuyHold {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 5 * DAYS_IN_A_YEAR;

	public static void main( final String... args ) {

		final String tickerSymbol = "^GSPC"; 	// S&P 500 - price return index
		final EquityClass equityType = EquityClass.STOCK;

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );
		final TradingDayPrices[] tradingData = BacktestCommon.getTradingData( tickerSymbol, startDate, endDate );

		// First data point may not be the requested start date
		final LocalDate earliestDate = BacktestCommon.getEarliestDate( tradingData );

		// Cumulative recording of investment progression
		final EventListener roiDisplay = new ConsoleReturnOnInvestmentDisplay();
		final CulmativeReturnOnInvestmentCalculator roi = BacktestCommon.createRoiCalculator( earliestDate, roiDisplay,
				MATH_CONTEXT );
		final CulmativeReturnOnInvestmentCalculatorListener cumulativeRoi = new CulmativeReturnOnInvestmentCalculatorListener(
				MATH_CONTEXT );
		roi.addListener( cumulativeRoi );

		// Weekly purchase of $100
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		final EntryLogic entry = new DateTriggeredEntryLogic( oneHundredDollars, equityType, earliestDate, weekly,
				MATH_CONTEXT );

		// Displays the events as they are generated
		final EventListener eventDisplay = new ConsoleEventDisplay();

		// Statistics recorder for the various cash account, brokerage and order events
		final EventStatistics eventStatistics = new CumulativeEventStatistics();

		// Never sell
		final ExitLogic exit = new HoldForeverExitLogic();

		// Cash account with flat interest of 1.5% - $100 deposit weekly, zero starting balance
		final CashAccount cashAccount = createCashAccountWeeklyDepositFlatInterestRate( earliestDate );
		cashAccount.addListener( roi );
		cashAccount.addListener( eventDisplay );
		cashAccount.addListener( eventStatistics );

		// ETF Broker with Bell Direct fees
		final BrokerageFeeStructure tradingFeeStructure = new VanguardRetailFeeStructure( MATH_CONTEXT );
		final Brokerage broker = new SingleEquityClassBroker( tradingFeeStructure, equityType, MATH_CONTEXT );
		broker.addListener( eventDisplay );
		broker.addListener( eventStatistics );

		final Simulation simulation = new Simulation( earliestDate, endDate, tradingData, broker, cashAccount, roi,
				entry, exit );
		simulation.addListener( eventDisplay );
		simulation.addListener( eventStatistics );

		final TickerSymbolTradingRange tickerSymbolTradingRange = new TickerSymbolTradingRangeImpl( tickerSymbol,
				startDate, endDate, tradingData.length );
		new ConsoleHeaderDisplay().displayHeader( tickerSymbolTradingRange );

		simulation.run();

		HibernateUtil.getSessionFactory().close();

		// Display summaries
		new ConsoleEventStatisticsDisplay( eventStatistics ).displayEventSummary();
		new ConsoleNetWorthSummaryDisplay( broker, tradingData, cashAccount, cumulativeRoi ).displayNetWorth();
	}

	private static CashAccount createCashAccountWeeklyDepositFlatInterestRate( final LocalDate earliestDate ) {
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		final InterestRate annualInterestRate = new FlatInterestRate( BigDecimal.valueOf( 1.5 ), MATH_CONTEXT );
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CashAccount underlyingAccount = new CalculatedDailyPaidMonthlyCashAccount( annualInterestRate,
				openingFunds, earliestDate, MATH_CONTEXT );
		return new RegularDepositCashAccountDecorator( oneHundredDollars, underlyingAccount, earliestDate, weekly );
	}
}
