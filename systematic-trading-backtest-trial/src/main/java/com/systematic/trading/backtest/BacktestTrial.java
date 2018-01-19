/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.cash.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.context.BacktestBootstrapContextBulider;
import com.systematic.trading.backtest.description.DescriptionGenerator;
import com.systematic.trading.backtest.description.DirectoryDescriptionGenerator;
import com.systematic.trading.backtest.description.StandardDescriptionGenerator;
import com.systematic.trading.backtest.description.StandardDirectoryDescriptionGenerator;
import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.backtest.event.BacktestEventListenerPreparation;
import com.systematic.trading.backtest.event.SilentBacktestEventLisener;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;
import com.systematic.trading.backtest.input.DepositFrequency;
import com.systematic.trading.backtest.input.OutputType;
import com.systematic.trading.backtest.output.elastic.ElasticBacktestOutput;
import com.systematic.trading.backtest.output.elastic.ElasticBacktestOutputPreparation;
import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfigurationSingleton;
import com.systematic.trading.backtest.output.file.CompleteFileOutputService;
import com.systematic.trading.backtest.output.file.MinimalFileOutputService;
import com.systematic.trading.backtest.output.file.dao.impl.FileValidatedBackestOutputFileConfigurationDao;
import com.systematic.trading.backtest.output.file.util.ClearFileDestination;
import com.systematic.trading.configuration.exception.ConfigurationValidationException;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.input.BacktestLaunchArguments;
import com.systematic.trading.model.equity.EquityClass;

/**
 * Setup specific behaviour for the Trial of back tests.
 * 
 * @author CJ Hare
 */
public class BacktestTrial {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(BacktestTrial.class);

	// TODO the description is specific to the type of output - file, console, elastic :. refactor -
	// move into
	// BacktestLaunchArgumentParser
	private final DescriptionGenerator description = new StandardDescriptionGenerator();

	// TODO this should be in the File output
	private final DirectoryDescriptionGenerator directoryDescription = new StandardDirectoryDescriptionGenerator();

	/** Ensures all the necessary trading data get retrieved into the local source. */
	private final DataServiceUpdater dataServiceUpdater;

	/** Local source of the trading prices. */
	private final DataService dataService;

	public BacktestTrial( final DataServiceType serviceType ) throws ServiceException {

		try {
			this.dataServiceUpdater = new DataServiceUpdaterImpl(serviceType);
		} catch (ServiceException e) {
			throw new BacktestInitialisationException(e);
		}

		this.dataService = new HibernateDataService();
	}

	public void runBacktest( final BacktestConfiguration configuration, final BacktestLaunchArguments parserdArguments )
	        throws ServiceException {

		// Date range is from the first of the starting month until now
		final BacktestStartDate simulationStartDate = parserdArguments.startDate();
		final BacktestEndDate simulationEndDate = parserdArguments.endDate();

		final EquityConfiguration equity = equity(parserdArguments);
		final CashAccountConfiguration cashAccount = cashAcount(parserdArguments);
		final DepositConfiguration deposit = deposit(cashAccount);

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(simulationStartDate,
		        simulationEndDate);
		recordSimulationDates(simulationDates);

		// Multi-threading support for output classes
		final ExecutorService outputPool = outputPool(parserdArguments);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date, rather then from the warm-up period

		final BacktestEventListenerPreparation outputPreparation = output(parserdArguments);
		outputPreparation.setUp();

		final StopWatch timer = new StopWatch();
		timer.start();

		final List<BacktestBootstrapConfiguration> backtestConfigurations = configuration.configuration(equity,
		        simulationDates, cashAccount);

		try {
			clearOutputDirectory(cashAccount, parserdArguments);

			for (final BacktestBootstrapConfiguration backtestConfiguration : backtestConfigurations) {
				final BacktestEventListener output = output(deposit, parserdArguments, backtestConfiguration,
				        outputPool);
				final BacktestBootstrapContext context = context(backtestConfiguration, output);

				LOG.info("Backtesting beginning for: {}",
				        () -> description.bootstrapConfigurationWithDeposit(backtestConfiguration, deposit));

				new Backtest(dataService, dataServiceUpdater).run(equity, backtestConfiguration.backtestDates(),
				        context, output);

				LOG.info("Backtesting complete for: {}",
				        () -> description.bootstrapConfigurationWithDeposit(backtestConfiguration, deposit));
			}
		} finally {
			HibernateUtil.sessionFactory().close();
			closePool(outputPool);
		}

		timer.stop();

		outputPreparation.tearDown();

		LOG.info(() -> String.format("Finished outputting %s results, time taken: %s", backtestConfigurations.size(),
		        Duration.ofMillis(timer.getTime())));
	}

	private CashAccountConfiguration cashAcount( final BacktestLaunchArguments parserdArguments ) {

		return new CashAccountConfiguration(
		        new DepositConfiguration(parserdArguments.depositAmount(), parserdArguments.depositFrequency()),
		        parserdArguments.interestRate(), parserdArguments.openingFunds());
	}

	private BacktestBootstrapContext context( final BacktestBootstrapConfiguration config,
	        final BacktestEventListener listener ) {

		return new BacktestBootstrapContextBulider().withConfiguration(config).withSignalAnalysisListeners(listener)
		        .build();
	}

	private EquityConfiguration equity( final BacktestLaunchArguments launchArgs ) {

		return new EquityConfiguration(launchArgs.equityDataset(), launchArgs.tickerSymbol(), EquityClass.STOCK);
	}

	private void closePool( final ExecutorService pool ) {

		pool.shutdown();

		LOG.info("Waiting at most 90 minutes for result output to complete...");
		try {
			pool.awaitTermination(90, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private BacktestEventListener output( final DepositConfiguration deposit, final BacktestLaunchArguments arguments,
	        final BacktestBootstrapConfiguration configuration, final ExecutorService pool )
	        throws BacktestInitialisationException {

		final BacktestBatchId batchId = batchId(configuration, deposit);
		final OutputType type = arguments.outputType();

		try {
			switch (type) {
				case ELASTIC_SEARCH:
					return new ElasticBacktestOutput(batchId, pool,
					        BackestOutputElasticConfigurationSingleton.configuration());
				case FILE_COMPLETE:
					return new CompleteFileOutputService(batchId,
					        outputDirectory(outputDirectory(deposit, arguments), configuration), pool);
				case FILE_MINIMUM:
					return new MinimalFileOutputService(batchId,
					        outputDirectory(outputDirectory(deposit, arguments), configuration), pool);
				case NO_DISPLAY:
					return new SilentBacktestEventLisener();
				default:
					throw new IllegalArgumentException(unsupportedMessage(type));
			}
		} catch (final IOException e) {
			throw new BacktestInitialisationException(e);
		}
	}

	private DepositConfiguration deposit( final CashAccountConfiguration cashAccount ) {

		final Optional<DepositConfiguration> deposit = cashAccount.deposit();

		if (deposit.isPresent()) { return deposit.get(); }

		// No deposit
		return new DepositConfiguration(BigDecimal.ZERO, DepositFrequency.MONTHLY);
	}

	private String outputDirectory( final DepositConfiguration depositAmount,
	        final BacktestLaunchArguments arguments ) {

		return isFileBasedDisplay(arguments) ? arguments.outputDirectory(directoryDescription.deposit(depositAmount))
		        : "";
	}

	private BacktestBatchId batchId( final BacktestBootstrapConfiguration configuration,
	        final DepositConfiguration depositAmount ) {

		return new BacktestBatchId(description.bootstrapConfigurationWithDeposit(configuration, depositAmount));
	}

	private String outputDirectory( final String baseOutputDirectory,
	        final BacktestBootstrapConfiguration configuration ) {

		return String.format("%s%s", baseOutputDirectory, description.bootstrapConfiguration(configuration));
	}

	private BacktestEventListenerPreparation output( final BacktestLaunchArguments arguments ) {

		final OutputType type = arguments.outputType();

		switch (type) {
			case ELASTIC_SEARCH:
				return new ElasticBacktestOutputPreparation(BackestOutputElasticConfigurationSingleton.configuration());
			case FILE_COMPLETE:
			case FILE_MINIMUM:
			case NO_DISPLAY:
				return new BacktestEventListenerPreparation() {};
			default:
				throw new IllegalArgumentException(unsupportedMessage(type));
		}
	}

	private ExecutorService outputPool( final BacktestLaunchArguments arguments )
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final OutputType type = arguments.outputType();

		switch (type) {
			case ELASTIC_SEARCH:
				return Executors.newFixedThreadPool(
				        BackestOutputElasticConfigurationSingleton.configuration().numberOfConnections());
			case FILE_COMPLETE:
			case FILE_MINIMUM:
				return Executors.newFixedThreadPool(
				        new FileValidatedBackestOutputFileConfigurationDao().configuration().numberOfThreads());
			case NO_DISPLAY:
				return Executors.newSingleThreadScheduledExecutor();
			default:
				throw new IllegalArgumentException(unsupportedMessage(type));
		}
	}

	private void clearOutputDirectory( final CashAccountConfiguration cashAccount,
	        final BacktestLaunchArguments arguments ) throws ServiceException {
		// TODO delete must run BEFORE any of the tests! that'll ensure race conditions are avoided

		// TODO this should happen only once & be moved into the file DAOs
		// TODO currently deleting at the deposit level, move up? i.e. ..\results\WEEKLY_150\
		// Arrange output to files, only once per a run

		if (isFileBasedDisplay(arguments)) {

			final Optional<DepositConfiguration> deposit = cashAccount.deposit();

			if (deposit.isPresent()) {

				final String outputDirectory = arguments.outputDirectory(directoryDescription.deposit(deposit.get()));
				try {
					new ClearFileDestination(outputDirectory).clear();
				} catch (final IOException e) {
					throw new BacktestInitialisationException(e);
				}
			}
		}
	}

	private boolean isFileBasedDisplay( final BacktestLaunchArguments arguments ) {

		return arguments.outputType() == OutputType.FILE_COMPLETE || arguments.outputType() == OutputType.FILE_MINIMUM;
	}

	private void recordSimulationDates( final BacktestSimulationDates simulationDates ) {

		LOG.info("Simulation Start Date: {}", simulationDates.startDate());
		LOG.info("Simulation End Date: {}", simulationDates.endDate());
	}

	private String unsupportedMessage( final OutputType type ) {

		return String.format("Output Type unsupported: %s", type);
	}
}