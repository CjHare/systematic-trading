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
import com.systematic.trading.backtest.configuration.WeeklyBuyWeeklyDespoitConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.file.FileClearDestination;
import com.systematic.trading.backtest.display.file.FileDisplay;
import com.systematic.trading.backtest.display.file.FileNetWorthComparisonDisplay;
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
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.logic.MinimumTradeValue;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Decides the persistence type to use, in addition to the type of back testing and equity it is
 * performed on.
 * 
 * @author CJ Hare
 */
public class SystematicTradingBacktest {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( SystematicTradingBacktest.class );

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

		try {
			final TickerSymbolTradingData tradingData = getTradingData( equity, startDate, endDate );

			// Arrange output to files
			new FileClearDestination( "../../simulations/" );

			final NetWorthEventListener netWorthComparisonDisplay = new FileNetWorthComparisonDisplay(
					"../../simulations/summary.txt", pool );

			for (final BacktestBootstrapConfiguration configuration : configurations) {
				final String outputDirectory = getOutputDirectory( equity, configuration );
				final BacktestDisplay fileDisplay = new FileDisplay( outputDirectory, pool );

				final BacktestBootstrap bootstrap = new BacktestBootstrap( tradingData, configuration, fileDisplay,
						netWorthComparisonDisplay, MATH_CONTEXT );

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

		final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, startDate, endDate,
				data );

		return tradingData;

	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final LocalDate startDate,
			final LocalDate endDate ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();

		configurations.add( new WeeklyBuyWeeklyDespoitConfiguration( startDate, endDate, MATH_CONTEXT ) );

		// Configuration with different entry values
		final BigDecimal[] minimumTradeValues = { BigDecimal.valueOf( 500 ), BigDecimal.valueOf( 1000 ),
				BigDecimal.valueOf( 1500 ), BigDecimal.valueOf( 2000 ) };

		String description;

		// TODO reuse stores
		final int days = HoldForeverWeeklyDespositConfiguration.maximumDaysOfSignalsAnalysed();
		int maximumTradingDays;
		SimpleMovingAverageGradientSignals sma;
		MovingAveragingConvergeDivergenceSignals macd;
		RelativeStrengthIndexSignals rsi;

		int macdTradingDays, smaTradingDays, rsiTradingDays;

		// TODO different RSI values
		final RsiConfiguration rsiConfiguration = RsiConfiguration.MEDIUM;

		// TODO tidy up
		for (final BigDecimal minimumTradeValue : minimumTradeValues) {

			final MinimumTradeValue minimumTrade = new MinimumTradeValue( minimumTradeValue );
			final String minimumTradeDescription = String.valueOf( minimumTrade.getValue().longValue() );

			for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {

				macdTradingDays = macdConfiguration.getSignalTimePeriods() + macdConfiguration.getSlowTimePeriods() + 3;
				maximumTradingDays = macdTradingDays + days;

				macd = new MovingAveragingConvergeDivergenceSignals( macdConfiguration.getFastTimePeriods(),
						macdConfiguration.getSlowTimePeriods(), macdConfiguration.getSignalTimePeriods(),
						maximumTradingDays, MATH_CONTEXT );

				description = String.format( "%s_Minimum-%s_HoldForever", macdConfiguration.getDescription(),
						minimumTradeDescription );
				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, macd ) );

				// Largest of the MACD, RSI
				macdTradingDays = macdConfiguration.getSignalTimePeriods() + macdConfiguration.getSlowTimePeriods() + 3;
				rsiTradingDays = RsiConfiguration.MEDIUM.getSlowRsi() + 1;
				maximumTradingDays = macdTradingDays > rsiTradingDays ? macdTradingDays : rsiTradingDays;
				maximumTradingDays += days;

				macd = new MovingAveragingConvergeDivergenceSignals( macdConfiguration.getFastTimePeriods(),
						macdConfiguration.getSlowTimePeriods(), macdConfiguration.getSignalTimePeriods(),
						maximumTradingDays, MATH_CONTEXT );

				rsi = new RelativeStrengthIndexSignals( rsiConfiguration.getFastRsi(), rsiConfiguration.getSlowRsi(),
						rsiConfiguration.getOverbought(), rsiConfiguration.getOversold(), maximumTradingDays,
						MATH_CONTEXT );

				description = String.format( "%s-%s_SameDay_Minimum-%s_HoldForever", macdConfiguration.getDescription(),
						rsiConfiguration.getDescription(), minimumTradeDescription );
				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, rsi, macd ) );

				for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {

					// Largest of the MACD, SMA
					macdTradingDays = macdConfiguration.getSignalTimePeriods() + macdConfiguration.getSlowTimePeriods()
							+ 3;
					smaTradingDays = smaConfiguration.getLookback() + smaConfiguration.getDaysOfGradient();
					maximumTradingDays = smaTradingDays > macdTradingDays ? smaTradingDays : macdTradingDays;
					maximumTradingDays += days;

					sma = new SimpleMovingAverageGradientSignals( smaConfiguration.getLookback(),
							smaConfiguration.getDaysOfGradient(), smaConfiguration.getType(), maximumTradingDays,
							MATH_CONTEXT );

					macd = new MovingAveragingConvergeDivergenceSignals( macdConfiguration.getFastTimePeriods(),
							macdConfiguration.getSlowTimePeriods(), macdConfiguration.getSignalTimePeriods(),
							maximumTradingDays, MATH_CONTEXT );

					description = String.format( "%s-%s_SameDay_Minimum-%s_HoldForever",
							macdConfiguration.getDescription(), smaConfiguration.getDescription(),
							minimumTradeDescription );
					configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
							description, MATH_CONTEXT, sma, macd ) );

					// Largest of the MACD, RSI, SMA
					macdTradingDays = macdConfiguration.getSignalTimePeriods() + macdConfiguration.getSlowTimePeriods()
							+ 3;
					smaTradingDays = smaConfiguration.getLookback() + smaConfiguration.getDaysOfGradient();
					maximumTradingDays = smaTradingDays > macdTradingDays ? smaTradingDays : macdTradingDays;
					rsiTradingDays = RsiConfiguration.MEDIUM.getSlowRsi() + 1;
					maximumTradingDays = maximumTradingDays > rsiTradingDays ? maximumTradingDays : rsiTradingDays;
					maximumTradingDays += days;

					sma = new SimpleMovingAverageGradientSignals( smaConfiguration.getLookback(),
							smaConfiguration.getDaysOfGradient(), smaConfiguration.getType(), maximumTradingDays,
							MATH_CONTEXT );

					macd = new MovingAveragingConvergeDivergenceSignals( macdConfiguration.getFastTimePeriods(),
							macdConfiguration.getSlowTimePeriods(), macdConfiguration.getSignalTimePeriods(),
							maximumTradingDays, MATH_CONTEXT );

					rsi = new RelativeStrengthIndexSignals( rsiConfiguration.getFastRsi(),
							rsiConfiguration.getSlowRsi(), rsiConfiguration.getOverbought(),
							rsiConfiguration.getOversold(), maximumTradingDays, MATH_CONTEXT );

					description = String.format( "%s-%s-%s_SameDay_Minimum-%s_HoldForever",
							macdConfiguration.getDescription(), smaConfiguration.getDescription(),
							rsiConfiguration.getDescription(), minimumTradeDescription );
					configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
							description, MATH_CONTEXT, rsi, sma, macd ) );
				}
			}

			for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
				description = String.format( "%s-%s_SameDay_Minimum-%s_HoldForever", smaConfiguration.getDescription(),
						rsiConfiguration.getDescription(), minimumTradeDescription );

				rsiTradingDays = RsiConfiguration.MEDIUM.getSlowRsi() + 1;
				smaTradingDays = smaConfiguration.getLookback() + smaConfiguration.getDaysOfGradient();
				maximumTradingDays = smaTradingDays > rsiTradingDays ? smaTradingDays : rsiTradingDays;
				maximumTradingDays += days;

				rsi = new RelativeStrengthIndexSignals( rsiConfiguration.getFastRsi(), rsiConfiguration.getSlowRsi(),
						rsiConfiguration.getOverbought(), rsiConfiguration.getOversold(), maximumTradingDays,
						MATH_CONTEXT );

				sma = new SimpleMovingAverageGradientSignals( smaConfiguration.getLookback(),
						smaConfiguration.getDaysOfGradient(), smaConfiguration.getType(), maximumTradingDays,
						MATH_CONTEXT );

				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, rsi, sma ) );
			}
		}

		return configurations;
	}

	private static EquityIdentity getEquityIdentity() {
		final String tickerSymbol = "^GSPC"; 	// S&P 500 - price return index
		final EquityClass equityType = EquityClass.STOCK;
		return new EquityIdentity( tickerSymbol, equityType );
	}

	private static String getOutputDirectory( final EquityIdentity equity,
			final BacktestBootstrapConfiguration configuration ) {
		return String.format( "../../simulations/%s_%s", equity.getTickerSymbol(), configuration.getDescription() );
	}
}
