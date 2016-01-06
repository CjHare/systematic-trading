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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.HoldForeverWeeklyDespositConfiguration;
import com.systematic.trading.backtest.configuration.fee.AvailableFeeStructure;
import com.systematic.trading.backtest.configuration.fee.FeeStructureFactory;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.file.FileClearDestination;
import com.systematic.trading.backtest.display.file.FileDisplay;
import com.systematic.trading.backtest.model.TickerSymbolTradingDataBacktest;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.simulation.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.simulation.logic.RelativeTradeValue;
import com.systematic.trading.simulation.logic.TradeValue;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Decides the persistence type to use, in addition to the type of back testing and equity it is
 * performed on.
 * 
 * @author CJ Hare
 */
public class BacktestOnlyOne {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( BacktestOnlyOne.class );

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;

	public static void main( final String... args ) throws Exception {

		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool( cores );

		final EquityIdentity equity = getEquityIdentity();

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );
		final List<BacktestBootstrapConfiguration> configurations = getConfigurations( startDate, endDate );

		final String baseOutputDirectory = getBaseOutputDirectory( args );

		// Arrange output to files, only once per a run
		new FileClearDestination( baseOutputDirectory );

		try {
			final TickerSymbolTradingData tradingData = getTradingData( equity, startDate, endDate );

			for (final BacktestBootstrapConfiguration configuration : configurations) {
				final String outputDirectory = getOutputDirectory( baseOutputDirectory, equity, configuration );

				final BacktestDisplay fileDisplay = new FileDisplay( outputDirectory, pool, MATH_CONTEXT );

				final BacktestBootstrap bootstrap = new BacktestBootstrap( tradingData, configuration, fileDisplay,
						MATH_CONTEXT );

				bootstrap.run();

				LOG.info( String.format( "Backtesting complete for: %s", configuration.getDescription() ) );
			}

			LOG.info( "All Simulations have been completed" );

		} finally {
			HibernateUtil.getSessionFactory().close();
			pool.shutdown();
		}
	}

	private static TickerSymbolTradingData getTradingData( final EquityIdentity equity, final LocalDate startDate,
			final LocalDate endDate ) {

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( equity.getTickerSymbol(), startDate, endDate );

		final DataService service = HibernateDataService.getInstance();
		final TradingDayPrices[] data = service.get( equity.getTickerSymbol(), startDate, endDate );

		final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, data );

		return tradingData;

	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final LocalDate startDate,
			final LocalDate endDate ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();

		String description;

		MovingAveragingConvergeDivergenceSignals macd;
		// RelativeStrengthIndexSignals rsi;

		final BigDecimal minimumTradeValue = BigDecimal.valueOf( 500 );
		final BigDecimal maximumTradeValue = BigDecimal.valueOf( 0.75 );

		final TradeValue tradeValue = new RelativeTradeValue( minimumTradeValue, maximumTradeValue, MATH_CONTEXT );

		final String minimumTradeDescription = String.valueOf( minimumTradeValue.longValue() );
		final String maximumTradeDescription = String.valueOf( maximumTradeValue.doubleValue() );

		final MacdConfiguration macdConfiguration = MacdConfiguration.SHORT;

		macd = new MovingAveragingConvergeDivergenceSignals( macdConfiguration.getFastTimePeriods(),
				macdConfiguration.getSlowTimePeriods(), macdConfiguration.getSignalTimePeriods(), MATH_CONTEXT );

		description = String.format( "%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", macdConfiguration.getDescription(),
				minimumTradeDescription, maximumTradeDescription );

		final BrokerageFeeStructure tradingFeeStructure = FeeStructureFactory
				.createFeeStructure( AvailableFeeStructure.CMC_MARKETS, MATH_CONTEXT );

		configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, tradeValue, description,
				tradingFeeStructure, MATH_CONTEXT, macd ) );

		return configurations;
	}

	private static EquityIdentity getEquityIdentity() {
		final String tickerSymbol = "^GSPC"; 	// S&P 500 - price return index
		final EquityClass equityType = EquityClass.STOCK;
		return new EquityIdentity( tickerSymbol, equityType );
	}

	private static String getOutputDirectory( final String baseOutputDirectory, final EquityIdentity equity,
			final BacktestBootstrapConfiguration configuration ) {
		return String.format( "%s%s_%s", baseOutputDirectory, equity.getTickerSymbol(),
				configuration.getDescription() );
	}

	private static String getBaseOutputDirectory( final String... args ) {

		if (args != null && args.length > 0) {
			return args[0];
		}

		return "../../simulations/";
	}

}
