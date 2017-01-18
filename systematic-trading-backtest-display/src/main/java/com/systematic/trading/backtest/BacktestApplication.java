/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.context.BacktestBootstrapContextBulider;
import com.systematic.trading.backtest.display.BacktestOutput;
import com.systematic.trading.backtest.display.DescriptionGenerator;
import com.systematic.trading.backtest.display.NoDisplay;
import com.systematic.trading.backtest.display.file.ClearFileDestination;
import com.systematic.trading.backtest.display.file.CompleteFileDisplay;
import com.systematic.trading.backtest.display.file.MinimalFileDisplay;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.backtest.model.TickerSymbolTradingDataBacktest;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;

/**
 * Performs the initial configuration common between all back tests, including hardware configuration.
 * 
 * @author CJ Hare
 */
public class BacktestApplication {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(BacktestApplication.class);

	/** Minimum amount of historical data needed for back testing. */
	private static final int DAYS_IN_A_YEAR = 365;
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;

	public enum DisplayType {
		FILE_FULL,
		FILE_MINIMUM,
		NO_DISPLAY;
	}

	private final MathContext mathContext;
	private final DescriptionGenerator description = new DescriptionGenerator();

	public BacktestApplication(final MathContext mathContext) {
		this.mathContext = mathContext;
	}

	public void runTest( final BacktestConfiguration configuration, final DisplayType outputType, final String... args )
	        throws ServiceException {

		final String baseOutputDirectory = getBaseOutputDirectory(args);

		// Date range is from the first of the starting month until now
		final LocalDate simulationEndDate = LocalDate.now();
		final LocalDate simulationStartDate = simulationEndDate.minus(HISTORY_REQUIRED, ChronoUnit.DAYS)
		        .withDayOfMonth(1);

		// Only for the single equity
		final EquityConfiguration equity = EquityConfiguration.SP_500_PRICE_INDEX;

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final Period warmUpPeriod = getWarmUpPeriod();
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(simulationStartDate,
		        simulationEndDate, warmUpPeriod);

		// Retrieve the set of trading data
		final TickerSymbolTradingData tradingData = getTradingData(equity.getEquityIdentity(), simulationDates);

		// Multi-threading support
		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool(cores);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date

		try {
			for (final DepositConfiguration depositAmount : DepositConfiguration.values()) {
				final List<BacktestBootstrapConfiguration> tests = configuration.get(equity, simulationDates,
				        depositAmount);
				final String outputDirectory = String.format(baseOutputDirectory, depositAmount);

				runTest(depositAmount, outputDirectory, tests, tradingData, outputType, pool);
			}

		} finally {
			HibernateUtil.getSessionFactory().close();
			closePool(pool);
		}

		LOG.info("Finished outputting results");
	}

	private static void closePool( final ExecutorService pool ) {
		pool.shutdown();

		LOG.info("Waiting at most 90 minutes for result output to complete...");
		try {
			pool.awaitTermination(90, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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

	private BacktestOutput getDisplay( final DisplayType type, final String outputDirectory,
	        final ExecutorService pool ) throws BacktestInitialisationException {
		try {
			switch (type) {
				case FILE_FULL:
					return new CompleteFileDisplay(outputDirectory, pool, mathContext);
				case FILE_MINIMUM:
					return new MinimalFileDisplay(outputDirectory, pool, mathContext);
				case NO_DISPLAY:
					return new NoDisplay();
				default:
					throw new IllegalArgumentException(String.format("Display Type not catered for: %s", type));
			}
		} catch (final IOException e) {
			throw new BacktestInitialisationException(e);
		}
	}

	private void runTest( final DepositConfiguration depositAmount, final String baseOutputDirectory,
	        final List<BacktestBootstrapConfiguration> configurations, final TickerSymbolTradingData tradingData,
	        final DisplayType type, final ExecutorService pool ) throws BacktestInitialisationException {

		// Arrange output to files, only once per a run
		ClearFileDestination destination = new ClearFileDestination(baseOutputDirectory);
		destination.clear();

		for (final BacktestBootstrapConfiguration configuration : configurations) {
			final String outputDirectory = getOutputDirectory(baseOutputDirectory, configuration);
			final BacktestOutput fileDisplay = getDisplay(type, outputDirectory, pool);
			final BacktestBootstrapContext context = createContext(configuration);

			final BacktestBootstrap bootstrap = new BacktestBootstrap(configuration, context, fileDisplay, tradingData,
			        mathContext);

			LOG.info(String.format("Backtesting beginning for: %s", description.getDescription(configuration)));

			bootstrap.run();

			LOG.info(String.format("Backtesting complete for: %s", description.getDescription(configuration)));
		}

		LOG.info(String.format("All Simulations have been completed for deposit amount: %s", depositAmount));

	}

	private BacktestBootstrapContext createContext( final BacktestBootstrapConfiguration configuration ) {
		final DepositConfiguration deposit = configuration.getDeposit();
		final BacktestSimulationDates simulationDates = configuration.getBacktestDates();
		final BacktestBootstrapContextBulider builder = new BacktestBootstrapContextBulider(configuration.getEquity(),
		        simulationDates, deposit, mathContext);

		final BrokerageFeesConfiguration brokerageType = configuration.getBrokerageFees();
		final EntryLogicConfiguration entry = configuration.getEntryLogic();
		final MinimumTrade minimumTrade = entry.getMinimumTrade();
		final MaximumTrade maximumTrade = entry.getMaximumTrade();

		//TODO move this switch into the builder
		switch (configuration.getEntryLogic().getType()) {
			case PERIODIC:
				final Period purchaseFrequency = configuration.getEntryLogic().getPeriodic().getFrequency();
				return builder.periodic(brokerageType, purchaseFrequency);

			case CONFIRMATION_SIGNAL:
				return builder.confirmationSignal(minimumTrade, maximumTrade, brokerageType, entry);

			case SAME_DAY_SIGNALS:
				return builder.indicatorsOnSameDay(minimumTrade, maximumTrade, brokerageType, entry);

			default:
				throw new IllegalArgumentException(
				        String.format("Unexpected entry logic type: %s", configuration.getEntryLogic().getType()));
		}
	}

	private TickerSymbolTradingData getTradingData( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDate ) throws ServiceException {

		final LocalDate startDate = simulationDate.getStartDate().minus(simulationDate.getWarmUp());
		final LocalDate endDate = simulationDate.getEndDate();

		// Retrieve and cache data range from remote data source
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get(equity.getTickerSymbol(), startDate, endDate);

		// Retrieve from local cache the desired data range
		final DataService service = HibernateDataService.getInstance();
		final TradingDayPrices[] data = service.get(equity.getTickerSymbol(), startDate, endDate);

		return new TickerSymbolTradingDataBacktest(equity, data);
	}

	private String getOutputDirectory( final String baseOutputDirectory,
	        final BacktestBootstrapConfiguration configuration ) {
		return String.format("%s%s", baseOutputDirectory, description.getDescription(configuration));
	}

	private String getBaseOutputDirectory( final String... args ) {

		if (args != null && args.length > 0) {
			return args[0] + "/%s/";
		}

		return "../../simulations/%s/";
	}
}