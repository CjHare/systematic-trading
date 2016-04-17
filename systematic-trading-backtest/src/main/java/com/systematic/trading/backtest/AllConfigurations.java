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
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfigurationGenerator;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.DescriptionGenerator;
import com.systematic.trading.backtest.display.file.FileClearDestination;
import com.systematic.trading.backtest.display.file.FileMinimalDisplay;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.backtest.model.TickerSymbolTradingDataBacktest;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.LadderedEquityManagementFeeCalculator;

/**
 * Values 100, 150, 200, 250, 300, 500.
 * 
 * @author CJ Hare
 */
public class AllConfigurations {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(AllConfigurations.class);

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	/** Minimum amount of historical data needed for back testing. */
	private static final int DAYS_IN_A_YEAR = 365;
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;

	private static final Period WEEKLY = Period.ofWeeks(1);
	private static final Period MONTHLY = Period.ofMonths(1);

	public static void main( final String... args ) throws Exception {

		final String baseOutputDirectory = getBaseOutputDirectory(args);
		final DescriptionGenerator filenameGenerator = new DescriptionGenerator();

		// Date range is from the first of the starting month until now
		final LocalDate simulationEndDate = LocalDate.now();
		final LocalDate simulationStartDate = simulationEndDate.minus(HISTORY_REQUIRED, ChronoUnit.DAYS)
		        .withDayOfMonth(1);

		// Only for the single equity
		final EquityIdentity equity = EquityConfiguration.SP_500_PRICE_INDEX.getEquityIdentity();

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final Period warmUpPeriod = getWarmUpPeriod();
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(simulationStartDate,
		        simulationEndDate, warmUpPeriod);

		// Retrieve the set of trading data
		final TickerSymbolTradingData tradingData = getTradingData(equity, simulationDates);

		// Multi-threading support
		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool(cores);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date

		try {
			for (final DepositConfiguration depositAmount : DepositConfiguration.values()) {

				final List<BacktestBootstrapConfiguration> configurations = getConfigurations(equity, simulationDates,
				        depositAmount, filenameGenerator);

				final String outputDirectory = String.format(baseOutputDirectory, depositAmount);

				runTest(depositAmount, outputDirectory, configurations, tradingData, equity, pool);
			}

		} finally {
			HibernateUtil.getSessionFactory().close();
			pool.shutdown();

			LOG.info("Waiting at most 90 minutes for result output to complete...");
			pool.awaitTermination(90, TimeUnit.MINUTES);
		}

		LOG.info("Finished outputting results");
	}

	private static Period getWarmUpPeriod() {

		int windUp = 0;

		for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
			if (macdConfiguration.getSlowTimePeriods() > windUp)
				windUp = macdConfiguration.getSlowTimePeriods();
		}
		for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {
			if (rsiConfiguration.getLookback() > windUp) {
				windUp = rsiConfiguration.getLookback();
			}
		}
		for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
			if (smaConfiguration.getLookback() + smaConfiguration.getDaysOfGradient() > windUp) {
				windUp = smaConfiguration.getLookback() + smaConfiguration.getDaysOfGradient();
			}
		}

		return Period.ofDays(windUp);
	}

	private static BacktestDisplay getDisplay( final String outputDirectory, final ExecutorService pool )
	        throws IOException {
		// return new FileDisplay( outputDirectory, pool, MATH_CONTEXT );
		return new FileMinimalDisplay(outputDirectory, pool, MATH_CONTEXT);
		// return new FileNoDisplay();
	}

	public static void runTest( final DepositConfiguration depositAmount, final String baseOutputDirectory,
	        final List<BacktestBootstrapConfiguration> configurations, final TickerSymbolTradingData tradingData,
	        final EquityIdentity equity, final ExecutorService pool ) throws Exception {

		// Arrange output to files, only once per a run
		new FileClearDestination(baseOutputDirectory);

		for (final BacktestBootstrapConfiguration configuration : configurations) {
			final String outputDirectory = getOutputDirectory(baseOutputDirectory, equity, configuration);
			final BacktestDisplay fileDisplay = getDisplay(outputDirectory, pool);

			final BacktestBootstrap bootstrap = new BacktestBootstrap(tradingData, configuration, fileDisplay,
			        MATH_CONTEXT);

			LOG.info(String.format("Backtesting beginning for: %s", configuration.getDescription()));

			bootstrap.run();

			LOG.info(String.format("Backtesting complete for: %s", configuration.getDescription()));
		}

		LOG.info(String.format("All Simulations have been completed for deposit amount: %s", depositAmount));

	}

	private static TickerSymbolTradingData getTradingData( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDate ) {

		final LocalDate startDate = simulationDate.getSimulationStartDate().minus(simulationDate.getWarmUp());
		final LocalDate endDate = simulationDate.getSimulationEndDate();

		// Retrieve and cache data range from remote data source
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get(equity.getTickerSymbol(), startDate, endDate);

		// Retrieve from local cache the desired data range
		final DataService service = HibernateDataService.getInstance();
		final TradingDayPrices[] data = service.get(equity.getTickerSymbol(), startDate, endDate);

		return new TickerSymbolTradingDataBacktest(equity, data);
	}

	// TODO these fees should go somewhere, another configuration enum?
	private static EquityManagementFeeCalculator getVanguardEftFeeCalculator() {
		// new ZeroEquityManagementFeeStructure()
		return new FlatEquityManagementFeeCalculator(BigDecimal.valueOf(0.0018), MATH_CONTEXT);
	}

	private static EquityManagementFeeCalculator getVanguardRetailFeeCalculator() {
		final BigDecimal[] vanguardFeeRange = { BigDecimal.valueOf(50000), BigDecimal.valueOf(100000) };
		final BigDecimal[] vanguardPercentageFee = { BigDecimal.valueOf(0.009), BigDecimal.valueOf(0.006),
		        BigDecimal.valueOf(0.0035) };
		return new LadderedEquityManagementFeeCalculator(vanguardFeeRange, vanguardPercentageFee, MATH_CONTEXT);
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDates, final DepositConfiguration deposit,
	        final DescriptionGenerator descriptionGenerator ) {

		final BacktestBootstrapConfigurationGenerator configurationGenerator = new BacktestBootstrapConfigurationGenerator(
		        equity, simulationDates, deposit, descriptionGenerator, MATH_CONTEXT);

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();

		// Vanguard Retail
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.VANGUARD_RETAIL,
		        WEEKLY, getVanguardRetailFeeCalculator()));

		// CMC Weekly
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.CMC_MARKETS,
		        WEEKLY, getVanguardEftFeeCalculator()));

		// CMC Monthly
		configurations.add(configurationGenerator.getPeriodicConfiguration(BrokerageFeesConfiguration.CMC_MARKETS,
		        MONTHLY, getVanguardEftFeeCalculator()));

		// All signal based use the trading account
		final BrokerageFeesConfiguration brokerage = BrokerageFeesConfiguration.CMC_MARKETS;

		for (final MaximumTrade maximumTrade : MaximumTrade.values()) {
			for (final MinimumTrade minimumTrade : MinimumTrade.values()) {
				for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {
					for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

						// MACD & RSI
						configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
						        getVanguardEftFeeCalculator(), brokerage, macdConfiguration, rsiConfiguration));
					}

					// MACD only
					configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
					        getVanguardEftFeeCalculator(), brokerage, macdConfiguration));

					for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
						for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

							// MACD, SMA & RSI
							configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade,
							        maximumTrade, getVanguardEftFeeCalculator(), brokerage, macdConfiguration,
							        smaConfiguration, rsiConfiguration));
						}

						// MACD & SMA
						configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
						        getVanguardEftFeeCalculator(), brokerage, macdConfiguration, smaConfiguration));
					}
				}

				for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
					for (final RsiConfiguration rsiConfiguration : RsiConfiguration.values()) {

						// SMA & RSI
						configurations.add(configurationGenerator.getIndicatorConfiguration(minimumTrade, maximumTrade,
						        getVanguardEftFeeCalculator(), brokerage, smaConfiguration, rsiConfiguration));
					}
				}
			}
		}

		return configurations;
	}

	private static String getOutputDirectory( final String baseOutputDirectory, final EquityIdentity equity,
	        final BacktestBootstrapConfiguration configuration ) {
		return String.format("%s%s_%s", baseOutputDirectory, equity.getTickerSymbol(), configuration.getDescription());
	}

	private static String getBaseOutputDirectory( final String... args ) {

		if (args != null && args.length > 0) {
			return args[0] + "/%s/";
		}

		return "../../simulations/%s/";
	}
}
