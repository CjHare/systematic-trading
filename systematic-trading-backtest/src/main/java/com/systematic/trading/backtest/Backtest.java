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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.brokerage.fees.impl.BellDirectFeeStructure;
import com.systematic.trading.backtest.brokerage.impl.SingleEquityClassBroker;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.cash.impl.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.backtest.cash.impl.FlatInterestRate;
import com.systematic.trading.backtest.cash.impl.RegularDepositCashAccountDecorator;
import com.systematic.trading.backtest.event.recorder.EventRecorder;
import com.systematic.trading.backtest.event.recorder.impl.ConsoleEventRecorder;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.impl.DateTriggeredEntryLogic;
import com.systematic.trading.backtest.logic.impl.HoldForeverExitLogic;
import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceImpl;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.util.HibernateUtil;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Assumption: no liquidity issues == can always execute a trade.
 * 
 * @author CJ Hare
 */
public class Backtest {

	private static final Logger LOG = LogManager.getLogger( Backtest.class );

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 5 * DAYS_IN_A_YEAR;

	public static void main( final String... args ) {

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );

		final String tickerSymbol = "^GSPC";
		// final String tickerSymbol = "USD";

		LOG.info( String.format( "Including data set for %s from %s to %s", tickerSymbol, startDate, endDate ) );

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( tickerSymbol, startDate, endDate );

		final DataService service = DataServiceImpl.getInstance();
		final DataPoint[] tradingDate = service.get( tickerSymbol, startDate, endDate );

		// TODO 1st question) returns of $100 weekly DCA via ETF vs Retail fund

		final EquityClass equityType = EquityClass.STOCK;

		final EventRecorder eventRecorder = new ConsoleEventRecorder();

		final LocalDate openingDate = getEarliestDate( tradingDate );

		// Weekly purchase of $100
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		final EntryLogic entry = new DateTriggeredEntryLogic( oneHundredDollars, eventRecorder, equityType,
				openingDate, weekly, MATH_CONTEXT );

		// Never sell
		final ExitLogic exit = new HoldForeverExitLogic();

		// Cash account with flat interest of 1.5% - $100 deposit weekly, zero starting balance
		final InterestRate rate = new FlatInterestRate( BigDecimal.valueOf( 1.5 ), MATH_CONTEXT );
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CashAccount underlyingAccount = new CalculatedDailyPaidMonthlyCashAccount( rate, openingFunds,
				openingDate, eventRecorder, MATH_CONTEXT );
		final CashAccount cashAccount = new RegularDepositCashAccountDecorator( oneHundredDollars, underlyingAccount,
				openingDate, weekly );

		// ETF Broker with Bell Direct fees
		final BrokerageFeeStructure fees = new BellDirectFeeStructure( MATH_CONTEXT );
		final Brokerage broker = new SingleEquityClassBroker( fees, equityType, eventRecorder, MATH_CONTEXT );

		final Simulation simulation = new Simulation( startDate, endDate, tradingDate, broker, cashAccount, entry, exit );

		// TODO metrics, time in / out market etc

		// TODO metrics of accounts, cash flow, share purchases

		simulation.run();

		HibernateUtil.getSessionFactory().close();

		((ConsoleEventRecorder) eventRecorder).summary();
	}

	private static LocalDate getEarliestDate( final DataPoint[] data ) {
		LocalDate earliest = data[0].getDate();

		for (final DataPoint today : data) {
			if (earliest.isAfter( today.getDate() )) {
				earliest = today.getDate();
			}
		}

		return earliest;
	}
}
