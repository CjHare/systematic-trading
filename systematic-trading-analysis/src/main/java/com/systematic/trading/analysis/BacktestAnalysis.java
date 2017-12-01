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
package com.systematic.trading.analysis;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.Backtest;
import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.BacktestConfiguration;
import com.systematic.trading.backtest.BacktestSimulationDates;
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
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ConfigurationValidationException;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.model.EquityClass;

/**
 * Performs a daily analysis to generate buy signals.
 * 
 * @author CJ Hare
 */
public class BacktestAnalysis {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(BacktestAnalysis.class);

	// TODO the description is specific to the type of output - file, console, elastic :. refactor - move into BacktestLaunchArgumentParser
	private final DescriptionGenerator description = new StandardDescriptionGenerator();

	/** Ensures all the necessary trading data get retrieved into the local source. */
	private final DataServiceUpdater dataServiceUpdater;

	/** Local source of the trading prices.*/
	private final DataService dataService;

	public BacktestAnalysis( final DataServiceType serviceType ) throws ServiceException {

		try {
			this.dataServiceUpdater = new DataServiceUpdaterImpl(serviceType);
		} catch (ServiceException e) {
			throw new BacktestInitialisationException(e);
		}

		this.dataService = new HibernateDataService();
	}

	//TODO problems!!!! BacktestConfiguration may need to be trial
	public void runBacktest( final BacktestConfiguration configuration ) throws ServiceException {

		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus(HISTORY_REQUIRED, ChronoUnit.DAYS);

		// Currently only for the single equity
		final EquityConfiguration equity = new EquityConfiguration(parserdArguments.getEquityDataset(),
		        parserdArguments.getTickerSymbol(), EquityClass.STOCK);

		//TODO convert into input arguments
		final DepositConfiguration depositAmount = DepositConfiguration.WEEKLY_200;

		// Move the date to included the necessary wind up time for the signals to behave correctly
		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(startDate, endDate);

		// Multi-threading support for output classes
		final ExecutorService outputPool = Executors.newFixedThreadPool(4);

		// TODO run the test over the full period with exclusion on filters
		// TODO no deposits until actual start date, rather then from the warm-up period

		final BacktestOutputPreparation outputPreparation = getOutputPreparation();
		outputPreparation.setUp();

		final StopWatch timer = new StopWatch();
		timer.start();

		final List<BacktestBootstrapConfiguration> backtestConfigurations = configuration.get(equity, simulationDates,
		        depositAmount);

		try {
			for (final BacktestBootstrapConfiguration backtestConfiguration : backtestConfigurations) {
				final BacktestOutput output = getOutput(depositAmount, backtestConfiguration, outputPool);

				LOG.info("Backtesting beginning for: {}",
				        () -> description.bootstrapConfigurationWithDeposit(backtestConfiguration, depositAmount));

				new Backtest(dataService, dataServiceUpdater).run(equity, backtestConfiguration, output);

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

	private BacktestOutputPreparation getOutputPreparation() {
		return new BacktestOutputPreparation() {
		};
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

	private BacktestOutput getOutput( final DepositConfiguration depositAmount,
	        final BacktestBootstrapConfiguration configuration, final ExecutorService pool )
	        throws BacktestInitialisationException {

		//TODO console display
		return null;
	}
}