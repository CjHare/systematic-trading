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
import com.systematic.trading.backtest.event.recorder.NetWorthSummary;
import com.systematic.trading.backtest.event.recorder.data.impl.BacktestTickerSymbolTradingRange;
import com.systematic.trading.backtest.event.recorder.impl.BacktestConsoleEventRecorder;
import com.systematic.trading.backtest.event.recorder.impl.BacktestConsoleNetWorthRecorder;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.impl.HoldForeverExitLogic;
import com.systematic.trading.backtest.logic.impl.SignalTriggeredEntryLogic;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceImpl;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.event.recorder.EventRecorder;
import com.systematic.trading.event.recorder.data.TickerSymbolTradingRange;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.StochasticOscillatorSignals;
import com.systematic.trading.signals.model.AnalysisLongBuySignals;
import com.systematic.trading.signals.model.configuration.AllSignalsConfiguration;
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

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );

		final String tickerSymbol = "^GSPC"; 	// S&P 500 - price return index

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( tickerSymbol, startDate, endDate );

		final DataService service = DataServiceImpl.getInstance();
		final TradingDayPrices[] tradingData = service.get( tickerSymbol, startDate, endDate );

		final TickerSymbolTradingRange tickerSymbolTradingRange = new BacktestTickerSymbolTradingRange( tickerSymbol,
				startDate, endDate, tradingData.length );

		final EquityClass equityType = EquityClass.STOCK;

		final EventRecorder eventRecorder = new BacktestConsoleEventRecorder();

		final LocalDate openingDate = getEarliestDate( tradingData );

		// Indicator triggered purchases
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 70, 30 );
		final MovingAveragingConvergeDivergenceSignals macd = new MovingAveragingConvergeDivergenceSignals( 10, 20, 7 );
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 10, 3, 3 );
		final LongBuySignalConfiguration configuration = new AllSignalsConfiguration( rsi, macd, stochastic );
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();

		// Only signals from the last two days are of interest
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new RsiMacdOnSameDaySignalFilter(),
				Period.ofDays( 5 ) );
		filters.add( filter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( configuration, filters );

		final EntryLogic entry = new SignalTriggeredEntryLogic( eventRecorder, equityType, BigDecimal.valueOf( 1000 ),
				buyLongAnalysis, MATH_CONTEXT );

		// Never sell
		final ExitLogic exit = new HoldForeverExitLogic();

		// Cash account with flat interest of 1.5% - $100 deposit weekly, zero starting balance
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		final InterestRate rate = new FlatInterestRate( BigDecimal.valueOf( 1.5 ), MATH_CONTEXT );
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CashAccount underlyingAccount = new CalculatedDailyPaidMonthlyCashAccount( rate, openingFunds,
				openingDate, eventRecorder, MATH_CONTEXT );
		final CashAccount cashAccount = new RegularDepositCashAccountDecorator( oneHundredDollars, underlyingAccount,
				openingDate, weekly );

		// ETF Broker with CmC markets fees
		final BrokerageFeeStructure tradingFeeStructure = new CmcMarketsFeeStructure( MATH_CONTEXT );
		final Brokerage broker = new SingleEquityClassBroker( tradingFeeStructure, equityType, eventRecorder,
				MATH_CONTEXT );

		final Simulation simulation = new Simulation( startDate, endDate, tradingData, broker, cashAccount, entry, exit );

		// TODO number of each order type event
		// TODO % actual return
		// TODO & yearly return

		eventRecorder.header();
		eventRecorder.header( tickerSymbolTradingRange );

		simulation.run();

		HibernateUtil.getSessionFactory().close();

		eventRecorder.eventSummary();

		final NetWorthSummary netWorth = new BacktestConsoleNetWorthRecorder( broker, tradingData, cashAccount );
		netWorth.display();
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
