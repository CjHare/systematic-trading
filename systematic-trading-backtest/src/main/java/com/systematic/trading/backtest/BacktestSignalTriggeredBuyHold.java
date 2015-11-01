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
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.impl.PeriodicCulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.backtest.analysis.statistics.EventStatistics;
import com.systematic.trading.backtest.analysis.statistics.impl.CumulativeEventStatistics;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.brokerage.fees.impl.CmcMarketsFeeStructure;
import com.systematic.trading.backtest.brokerage.impl.SingleEquityClassBroker;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.cash.impl.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.backtest.cash.impl.FlatInterestRate;
import com.systematic.trading.backtest.cash.impl.RegularDepositCashAccountDecorator;
import com.systematic.trading.backtest.display.file.FileDisplay;
import com.systematic.trading.backtest.event.data.TickerSymbolTradingRangeImpl;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.impl.HoldForeverExitLogic;
import com.systematic.trading.backtest.logic.impl.SignalTriggeredEntryLogic;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.event.data.TickerSymbolTradingRange;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.model.AnalysisLongBuySignals;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.RsiMacdOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Assumption: no liquidity issues == can always execute a trade.
 * 
 * @author CJ Hare
 */
public class BacktestSignalTriggeredBuyHold {

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
		final CulmativeReturnOnInvestmentCalculator roi = new CulmativeReturnOnInvestmentCalculator( MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener dailyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofDays( 1 ), MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener monthlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofMonths( 1 ), MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener yearlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofYears( 1 ), MATH_CONTEXT );
		roi.addListener( dailyRoi );
		roi.addListener( monthlyRoi );
		roi.addListener( yearlyRoi );
		final CulmativeReturnOnInvestmentCalculatorListener cumulativeRoi = new CulmativeReturnOnInvestmentCalculatorListener(
				MATH_CONTEXT );
		roi.addListener( cumulativeRoi );

		// Indicator triggered purchases
		final LongBuySignalConfiguration configuration = BacktestCommon.getStandardSignalConfiguration();
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();

		// Only signals from the last two days are of interest
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new RsiMacdOnSameDaySignalFilter(),
				Period.ofDays( 5 ) );
		filters.add( filter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( configuration, filters );

		final EntryLogic entry = new SignalTriggeredEntryLogic( equityType, BigDecimal.valueOf( 1000 ),
				buyLongAnalysis, MATH_CONTEXT );

		// Displays the events as they are generated
		final TickerSymbolTradingRange tickerSymbolTradingRange = new TickerSymbolTradingRangeImpl( tickerSymbol,
				startDate, endDate, tradingData.length );

		// Statistics recorder for the various cash account, brokerage and order events
		final EventStatistics eventStatistics = new CumulativeEventStatistics();

		// Never sell
		final ExitLogic exit = new HoldForeverExitLogic();

		// Cash account with flat interest of 1.5% - $100 deposit weekly, zero starting balance
		final CashAccount cashAccount = createCashAccountWeeklyDepositFlatInterestRate( earliestDate );
		cashAccount.addListener( roi );
		cashAccount.addListener( eventStatistics );

		// ETF Broker with CmC markets fees
		final BrokerageFeeStructure tradingFeeStructure = new CmcMarketsFeeStructure( MATH_CONTEXT );
		final Brokerage broker = new SingleEquityClassBroker( tradingFeeStructure, equityType, MATH_CONTEXT );
		broker.addListener( eventStatistics );

		final Simulation simulation = new Simulation( earliestDate, endDate, tradingData, broker, cashAccount, roi,
				entry, exit );
		simulation.addListener( eventStatistics );

		final FileDisplay display = new FileDisplay( tickerSymbolTradingRange, eventStatistics, broker, cashAccount,
				cumulativeRoi, tradingData );
		simulation.addListener( display );
		broker.addListener( display );
		cashAccount.addListener( display );
		yearlyRoi.addListener( display );
		monthlyRoi.addListener( display );
		dailyRoi.addListener( display );

		simulation.run();

		HibernateUtil.getSessionFactory().close();

		// Display summaries
		display.simulationCompleted();
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
