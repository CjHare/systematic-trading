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

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.HoldForeverWeeklyDespositConfiguration;
import com.systematic.trading.backtest.configuration.WeeklyBuyWeeklyDespoitConfiguration;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.NetWorthComparisonDisplay;
import com.systematic.trading.backtest.display.file.FileDisplay;
import com.systematic.trading.backtest.display.file.FileNetWorthComparisonDisplay;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals.GradientType;
import com.systematic.trading.simulation.brokerage.EquityClass;
import com.systematic.trading.simulation.brokerage.EquityIdentity;
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

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;

	public static void main( final String... args ) throws Exception {

		final EquityIdentity equity = getEquityIdentity();

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );
		final List<BacktestBootstrapConfiguration> configurations = getConfigurations( startDate, endDate );

		// Arrange output to files
		final NetWorthComparisonDisplay netWorthComparisonDisplay = new FileNetWorthComparisonDisplay(
				"../../simulations/summary.txt" );

		for (final BacktestBootstrapConfiguration configuration : configurations) {
			final String outputDirectory = getOutputDirectory( equity, configuration );
			final BacktestDisplay fileDisplay = new FileDisplay( outputDirectory );

			netWorthComparisonDisplay.setDescription( configuration.getDescription() );

			final BacktestBootstrap bootstrap = new BacktestBootstrap( equity, configuration, fileDisplay,
					netWorthComparisonDisplay, MATH_CONTEXT );

			bootstrap.run();
		}

		HibernateUtil.getSessionFactory().close();
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final LocalDate startDate,
			final LocalDate endDate ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();

		configurations.add( new WeeklyBuyWeeklyDespoitConfiguration( startDate, endDate, MATH_CONTEXT ) );

		// Configuration with different entry values
		final BigDecimal[] minimumTradeValues = { BigDecimal.valueOf( 500 ), BigDecimal.valueOf( 1000 ),
				BigDecimal.valueOf( 1500 ), BigDecimal.valueOf( 2000 ) };

		final RelativeStrengthIndexSignals rsiStandard = new RelativeStrengthIndexSignals( 70, 30, MATH_CONTEXT );

		String description;

		// TODO tidy up
		for (final BigDecimal minimumTradeValue : minimumTradeValues) {

			final MinimumTradeValue minimumTrade = new MinimumTradeValue( minimumTradeValue );
			final String minimumTradeDescription = String.valueOf( minimumTrade.getValue().longValue() );

			for (final MacdConfiguration macd : MacdConfiguration.values()) {

				description = String.format( "%s_Minimum-%s_HoldForever", macd.getDescription(),
						minimumTradeDescription );
				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, macd.getMacd() ) );

				description = String.format( "%s-Rsi_SameDay_Minimum-%s_HoldForever", macd.getDescription(),
						minimumTradeDescription );
				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, rsiStandard, macd.getMacd() ) );

				for (final SmaConfiguration sma : SmaConfiguration.values()) {

					description = String.format( "%s-%s_SameDay_Minimum-%s_HoldForever", macd.getDescription(),
							sma.getDescription(), minimumTradeDescription );
					configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
							description, MATH_CONTEXT, sma.getSma(), macd.getMacd() ) );

					description = String.format( "%s-%s-Rsi_SameDay_Minimum-%s_HoldForever", macd.getDescription(),
							sma.getDescription(), minimumTradeDescription );
					configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
							description, MATH_CONTEXT, rsiStandard, sma.getSma(), macd.getMacd() ) );
				}
			}

			for (final SmaConfiguration sma : SmaConfiguration.values()) {
				description = String.format( "%s-Rsi_SameDay_Minimum-%s_HoldForever", sma.getDescription(),
						minimumTradeDescription );
				configurations.add( new HoldForeverWeeklyDespositConfiguration( startDate, endDate, minimumTrade,
						description, MATH_CONTEXT, rsiStandard, sma.getSma() ) );
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

	// TODO separate classes?
	private enum MacdConfiguration {

		SHORT( new MovingAveragingConvergeDivergenceSignals( 5, 10, 3, MATH_CONTEXT ), "Short-Macd" ),
		MEDIUM( new MovingAveragingConvergeDivergenceSignals( 10, 20, 7, MATH_CONTEXT ), "Medium-Macd" ),
		LONG( new MovingAveragingConvergeDivergenceSignals( 25, 50, 10, MATH_CONTEXT ), "Long-Macd" ),
		LONGEST( new MovingAveragingConvergeDivergenceSignals( 50, 100, 35, MATH_CONTEXT ), "Longest-Macd" );

		private final MovingAveragingConvergeDivergenceSignals macd;
		private final String description;

		private MacdConfiguration( final MovingAveragingConvergeDivergenceSignals macd, final String description ) {
			this.macd = macd;
			this.description = description;
		}

		public MovingAveragingConvergeDivergenceSignals getMacd() {
			return macd;
		}

		public String getDescription() {
			return description;
		}
	}

	private enum SmaConfiguration {

		SHORT( new SimpleMovingAverageGradientSignals( 20, 5, GradientType.POSITIVE, MATH_CONTEXT ), "Positive-Short-Sma" ),
		MEDIUM( new SimpleMovingAverageGradientSignals( 50, 7, GradientType.POSITIVE, MATH_CONTEXT ), "Positive-Medium-Sma" ),
		LONG( new SimpleMovingAverageGradientSignals( 100, 10, GradientType.POSITIVE, MATH_CONTEXT ), "Positive-Medium-Sma" ),
		LONGEST( new SimpleMovingAverageGradientSignals( 200, 20, GradientType.POSITIVE, MATH_CONTEXT ), "Positive-Long-Sma" );

		private final SimpleMovingAverageGradientSignals sma;
		private final String description;

		private SmaConfiguration( final SimpleMovingAverageGradientSignals sma, final String description ) {
			this.sma = sma;
			this.description = description;
		}

		public SimpleMovingAverageGradientSignals getSma() {
			return sma;
		}

		public String getDescription() {
			return description;
		}
	}
}
