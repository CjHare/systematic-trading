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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.Backtest;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.output.BacktestOutput;
import com.systematic.trading.backtest.output.BacktestOutputPreparation;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;

/**
 * Performs a daily analysis to generate buy signals.
 * 
 * @author CJ Hare
 */
public class BacktestAnalysis {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(BacktestAnalysis.class);

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

	public void runBacktest( final BacktestBootstrapConfiguration backtestConfiguration,
	        final EquityConfiguration equity ) throws ServiceException {

		// Multi-threading support for output classes
		final ExecutorService outputPool = Executors.newFixedThreadPool(4);

		final BacktestOutputPreparation outputPreparation = getOutputPreparation();
		outputPreparation.setUp();

		final StopWatch timer = new StopWatch();
		timer.start();

		try {
			new Backtest(dataService, dataServiceUpdater).run(equity, backtestConfiguration,
			        getOutput(backtestConfiguration, outputPool));

		} finally {
			HibernateUtil.getSessionFactory().close();
			closePool(outputPool);
		}

		timer.stop();

		outputPreparation.tearDown();
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

	private BacktestOutput getOutput( final BacktestBootstrapConfiguration configuration, final ExecutorService pool )
	        throws BacktestInitialisationException {

		//TODO console display
		return null;
	}
}