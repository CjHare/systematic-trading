/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.input.LaunchArguments;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.BacktestOutputPreparation;
import com.systematic.trading.backtest.output.DescriptionGenerator;
import com.systematic.trading.backtest.output.NoBacktestOutput;
import com.systematic.trading.backtest.output.StandardDescriptionGenerator;
import com.systematic.trading.backtest.output.elastic.ElasticBacktestOutput;
import com.systematic.trading.backtest.output.elastic.ElasticBacktestOutputPreparation;
import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfigurationSingleton;
import com.systematic.trading.backtest.output.file.CompleteFileOutputService;
import com.systematic.trading.backtest.output.file.MinimalFileOutputService;
import com.systematic.trading.backtest.output.file.dao.impl.FileValidatedBackestOutputFileConfigurationDao;
import com.systematic.trading.backtest.output.file.util.ClearFileDestination;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ConfigurationValidationException;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.model.EquityClass;

/**
 * Setup specific behaviour for the Trial of back tests.
 * 
 * @author CJ Hare
 */
public class BacktestTrial {
	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(Backtest.class);

	// TODO the description is specific to the type of output - file, console, elastic :. refactor - move into BacktestLaunchArgumentParser
	private final DescriptionGenerator description = new StandardDescriptionGenerator();

	private final DataServiceUpdater updateService;

	public BacktestTrial( final DataServiceType serviceType ) throws ServiceException {

		try {
			this.updateService = new DataServiceUpdaterImpl(serviceType);
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
		final EquityConfiguration equity = new EquityConfiguration(parserdArguments.getEquityDataset(),
		        parserdArguments.getTickerSymbol(), EquityClass.STOCK);

		//TODO convert into input arguments
		final DepositConfiguration depositAmount = DepositConfiguration.WEEKLY_200;

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(simulationStartDate,
		        simulationEndDate);
		recordSimulationDates(simulationDates);

		// Multi-threading support for output classes
		final ExecutorService outputPool = getOutputPool(parserdArguments);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date, rather then from the warm-up period

		final BacktestOutputPreparation outputPreparation = getOutput(parserdArguments);
		outputPreparation.setUp();

		final StopWatch timer = new StopWatch();
		timer.start();

		final List<BacktestBootstrapConfiguration> backtestConfigurations = configuration.get(equity, simulationDates,
		        depositAmount);

		try {
			clearOutputDirectory(depositAmount, parserdArguments);

			for (final BacktestBootstrapConfiguration backtestConfiguration : backtestConfigurations) {
				final BacktestOutput output = getOutput(depositAmount, parserdArguments, backtestConfiguration,
				        outputPool);

				LOG.info("Backtesting beginning for: {}",
				        () -> description.bootstrapConfigurationWithDeposit(backtestConfiguration, depositAmount));

				new Backtest(updateService).run(equity, depositAmount, parserdArguments, backtestConfiguration, output);

				LOG.info("Backtesting complete for: {}",
				        () -> description.bootstrapConfigurationWithDeposit(backtestConfiguration, depositAmount));
			}
		} finally {
			HibernateUtil.getSessionFactory().close();
			closePool(outputPool);
		}

		timer.stop();

		outputPreparation.tearDown();

		LOG.info(() -> String.format("Finished outputting %s results, time taken: %s", backtestConfigurations.size(),
		        Duration.ofMillis(timer.getTime())));
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

	private BacktestOutput getOutput( final DepositConfiguration depositAmount, final LaunchArguments arguments,
	        final BacktestBootstrapConfiguration configuration, final ExecutorService pool )
	        throws BacktestInitialisationException {

		final BacktestBatchId batchId = getBatchId(configuration, depositAmount);
		final OutputType type = arguments.getOutputType();

		try {
			switch (type) {
				case ELASTIC_SEARCH:
					return new ElasticBacktestOutput(batchId, pool,
					        BackestOutputElasticConfigurationSingleton.getConfiguration());
				case FILE_COMPLETE:
					return new CompleteFileOutputService(batchId,
					        getOutputDirectory(getOutputDirectory(depositAmount, arguments), configuration), pool);
				case FILE_MINIMUM:
					return new MinimalFileOutputService(batchId,
					        getOutputDirectory(getOutputDirectory(depositAmount, arguments), configuration), pool);
				case NO_DISPLAY:
					return new NoBacktestOutput();
				default:
					throw new IllegalArgumentException(unsupportedMessage(type));
			}
		} catch (final IOException e) {
			throw new BacktestInitialisationException(e);
		}
	}

	private String getOutputDirectory( final DepositConfiguration depositAmount, final LaunchArguments arguments ) {
		return isFileBasedDisplay(arguments) ? arguments.getOutputDirectory(depositAmount) : "";
	}

	private BacktestBatchId getBatchId( final BacktestBootstrapConfiguration configuration,
	        final DepositConfiguration depositAmount ) {
		return new BacktestBatchId(description.bootstrapConfigurationWithDeposit(configuration, depositAmount));
	}

	private String getOutputDirectory( final String baseOutputDirectory,
	        final BacktestBootstrapConfiguration configuration ) {
		return String.format("%s%s", baseOutputDirectory, description.bootstrapConfiguration(configuration));
	}

	private BacktestOutputPreparation getOutput( final LaunchArguments arguments ) {
		final OutputType type = arguments.getOutputType();

		switch (type) {
			case ELASTIC_SEARCH:
				return new ElasticBacktestOutputPreparation(
				        BackestOutputElasticConfigurationSingleton.getConfiguration());
			case FILE_COMPLETE:
			case FILE_MINIMUM:
			case NO_DISPLAY:
				return new BacktestOutputPreparation() {
				};
			default:
				throw new IllegalArgumentException(unsupportedMessage(type));
		}
	}

	private ExecutorService getOutputPool( final LaunchArguments arguments )
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {
		final OutputType type = arguments.getOutputType();

		switch (type) {
			case ELASTIC_SEARCH:
				return Executors.newFixedThreadPool(
				        BackestOutputElasticConfigurationSingleton.getConfiguration().getNumberOfConnections());
			case FILE_COMPLETE:
			case FILE_MINIMUM:
				return Executors.newFixedThreadPool(
				        new FileValidatedBackestOutputFileConfigurationDao().get().getNumberOfThreads());
			case NO_DISPLAY:
				return Executors.newSingleThreadScheduledExecutor();
			default:
				throw new IllegalArgumentException(unsupportedMessage(type));
		}
	}

	private void clearOutputDirectory( final DepositConfiguration depositAmount, final LaunchArguments arguments )
	        throws ServiceException {
		//TODO delete must run BEFORE any of the tests! that'll ensure race conditions are avoided

		//TODO this should happen only once & be moved into the file DAOs
		//TODO currently deleting at the deposit level, move up? i.e. ..\results\WEEKLY_150\
		// Arrange output to files, only once per a run

		if (isFileBasedDisplay(arguments)) {
			final String outputDirectory = arguments.getOutputDirectory(depositAmount);
			try {
				new ClearFileDestination(outputDirectory).clear();
			} catch (final IOException e) {
				throw new BacktestInitialisationException(e);
			}
		}
	}

	private boolean isFileBasedDisplay( final LaunchArguments arguments ) {
		return arguments.getOutputType() == OutputType.FILE_COMPLETE
		        || arguments.getOutputType() == OutputType.FILE_MINIMUM;
	}

	private void recordSimulationDates( final BacktestSimulationDates simulationDates ) {
		LOG.info("Simulation Start Date: {}", simulationDates.getStartDate());
		LOG.info("Simulation End Date: {}", simulationDates.getEndDate());
	}

	private String unsupportedMessage( final OutputType type ) {
		return String.format("Output Type unsupported: %s", type);
	}
}