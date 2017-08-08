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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.context.BacktestBootstrapContextBulider;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.DescriptionGenerator;
import com.systematic.trading.backtest.output.NoBacktestOutput;
import com.systematic.trading.backtest.output.elastic.ElasticBacktestOutput;
import com.systematic.trading.backtest.output.file.CompleteFileOutputService;
import com.systematic.trading.backtest.output.file.MinimalFileOutputService;
import com.systematic.trading.backtest.output.file.util.ClearFileDestination;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.model.EquityClass;
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

	private final MathContext mathContext;

	// TODO the description is specific to the type of output - file, console, elastic :. refactor - move into BacktestLaunchArgumentParser
	private final DescriptionGenerator description = new DescriptionGenerator();

	private final DataServiceUpdater updateService;

	public BacktestApplication( final MathContext mathContext ) throws ServiceException {
		this.mathContext = mathContext;

		try {
			this.updateService = new DataServiceUpdaterImpl();
		} catch (ServiceException e) {
			throw new BacktestInitialisationException(e);
		}
	}

	public void runBacktest( final BacktestConfiguration configuration, final LaunchArguments parserdArguments )
	        throws ServiceException {

		// Date range is from the first of the starting month until now
		final LocalDate simulationStartDate = parserdArguments.getStartDate().getDate();
		final LocalDate simulationEndDate = parserdArguments.getEndDate().getDate();

		// Currently only for the single equity
		final EquityConfiguration equity = new EquityConfiguration(parserdArguments.getTickerSymbol().getSymbol(),
		        EquityClass.STOCK);

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final Period warmUpPeriod = getWarmUpPeriod();
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(simulationStartDate,
		        simulationEndDate, warmUpPeriod);

		recordSimulationDates(warmUpPeriod, simulationDates);

		// Retrieve the set of trading data
		final TickerSymbolTradingData tradingData = getTradingData(equity.getEquityIdentity(), simulationDates);

		// Multi-threading support
		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool(cores);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date

		try {
			for (final DepositConfiguration depositAmount : DepositConfiguration.values()) {
				final List<BacktestBootstrapConfiguration> configurations = configuration.get(equity, simulationDates,
				        depositAmount);
				clearOutputDirectory(depositAmount, parserdArguments);
				runBacktest(depositAmount, parserdArguments, configurations, tradingData, pool);
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

	private BacktestOutput getOutput( final DepositConfiguration depositAmount, final LaunchArguments arguments,
	        final BacktestBootstrapConfiguration configuration, final ExecutorService pool )
	        throws BacktestInitialisationException {

		final BacktestBatchId batchId = getBatchId(configuration);
		final OutputType type = arguments.getOutputType();

		try {
			switch (type) {
				case ELASTIC_SEARCH:
					return new ElasticBacktestOutput(batchId);
				case FILE_COMPLETE:
					return new CompleteFileOutputService(batchId,
					        getOutputDirectory(getOutputDirectory(depositAmount, arguments).get(), configuration), pool,
					        mathContext);
				case FILE_MINIMUM:
					return new MinimalFileOutputService(batchId,
					        getOutputDirectory(getOutputDirectory(depositAmount, arguments).get(), configuration), pool,
					        mathContext);
				case NO_DISPLAY:
					return new NoBacktestOutput();
				default:
					throw new IllegalArgumentException(String.format("Display Type not catered for: %s", type));
			}
		} catch (final IOException e) {
			throw new BacktestInitialisationException(e);
		}
	}

	private void runBacktest( final DepositConfiguration depositAmount, final LaunchArguments arguments,
	        final List<BacktestBootstrapConfiguration> configurations, final TickerSymbolTradingData tradingData,
	        final ExecutorService pool ) throws BacktestInitialisationException {

		for (final BacktestBootstrapConfiguration configuration : configurations) {
			final BacktestOutput output = getOutput(depositAmount, arguments, configuration, pool);
			final BacktestBootstrapContext context = createContext(configuration);
			final BacktestBootstrap bootstrap = new BacktestBootstrap(configuration, context, output, tradingData,
			        mathContext);

			LOG.info("Backtesting beginning for: {}", () -> description.getDescription(configuration));

			bootstrap.run();

			LOG.info("Backtesting complete for: {}", () -> description.getDescription(configuration));
		}

		LOG.info("All Simulations have been completed for deposit amount: {}", () -> depositAmount);
	}

	private void clearOutputDirectory( final DepositConfiguration depositAmount, final LaunchArguments arguments ) {
		//TODO delete must run BEFORE any of the tests! that'll ensure race conditions are avoided

		//TODO this should happen only once & be moved into the file DAOs
		//TODO currently deleting at the deposit level, move up? i.e. ..\results\WEEKLY_150\
		// Arrange output to files, only once per a run

		if (isFileBasedDisplay(arguments)) {
			final String outputDirectory = arguments.getOutputDirectory(depositAmount);
			new ClearFileDestination(outputDirectory).clear();
		}
	}

	private Optional<String> getOutputDirectory( final DepositConfiguration depositAmount,
	        final LaunchArguments arguments ) {
		return isFileBasedDisplay(arguments) ? Optional.of(arguments.getOutputDirectory(depositAmount))
		        : Optional.empty();
	}

	private boolean isFileBasedDisplay( final LaunchArguments arguments ) {
		return arguments.getOutputType() == OutputType.FILE_COMPLETE
		        || arguments.getOutputType() == OutputType.FILE_MINIMUM;
	}

	private BacktestBootstrapContext createContext( final BacktestBootstrapConfiguration configuration ) {
		return new BacktestBootstrapContextBulider(mathContext).withConfiguration(configuration).build();
	}

	private TickerSymbolTradingData getTradingData( final EquityIdentity equity,
	        final BacktestSimulationDates simulationDate ) throws ServiceException {

		final LocalDate startDate = simulationDate.getStartDate().minus(simulationDate.getWarmUp());
		final LocalDate endDate = simulationDate.getEndDate();

		if (startDate.getDayOfMonth() != 1) {
			LOG.info(String.format(
			        "Remote data retrieval for start date: %s has been adjusted to the beginning of the month ",
			        startDate));
		}

		if (endDate.getDayOfMonth() != 1) {
			LOG.warn(
			        "With the current data retrieval implementation, an End date of the first day of the month is more efficient");
		}

		final LocalDate retrievalStartDate = startDate.withDayOfMonth(1);

		// Retrieve and cache data range from remote data source		
		updateService.get(equity.getTickerSymbol(), retrievalStartDate, endDate);

		// Retrieve from local cache the desired data range
		final TradingDayPrices[] data = new HibernateDataService().get(equity.getTickerSymbol(), startDate, endDate);

		return new BacktestTickerSymbolTradingData(equity, data);
	}

	private BacktestBatchId getBatchId( final BacktestBootstrapConfiguration configuration ) {
		return new BacktestBatchId(description.getDescription(configuration),
		        description.getEntryLogic(configuration.getEntryLogic()),
		        configuration.getEntryLogic().getMinimumTrade(), configuration.getEntryLogic().getMaximumTrade());
	}

	private String getOutputDirectory( final String baseOutputDirectory,
	        final BacktestBootstrapConfiguration configuration ) {
		return String.format("%s%s", baseOutputDirectory, description.getDescription(configuration));
	}

	private void recordSimulationDates( final Period warmUpPeriod, final BacktestSimulationDates simulationDates ) {
		LOG.info("{}", () -> String.format("Simulation Warm Up Period of Days: %s, Months: %s, Years: %s",
		        warmUpPeriod.getDays(), warmUpPeriod.getMonths(), warmUpPeriod.getYears()));
		LOG.info("Simulation Start Date: {}", simulationDates.getStartDate());
		LOG.info("Simulation End Date: {}", simulationDates.getEndDate());
	}
}