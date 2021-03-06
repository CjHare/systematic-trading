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

import java.time.LocalDate;
import java.time.Period;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.model.equity.EquityIdentity;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * Connects together the various parts and performs the back testing.
 * 
 * @author CJ Hare
 */
public class Backtest {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(Backtest.class);

	/** Ensures all the necessary trading data get retrieved into the local source. */
	private final DataServiceUpdater dataServiceUpdater;

	/** Local source of the trading prices. */
	private final DataService dataService;

	public Backtest( final DataService dataService, final DataServiceUpdater dataServiceUpdater ) {

		this.dataService = dataService;
		this.dataServiceUpdater = dataServiceUpdater;
	}

	public void run(
	        final EquityConfiguration equity,
	        final BacktestSimulationDates dates,
	        final BacktestBootstrapContext context,
	        final BacktestEventListener output ) throws ServiceException {

		final Period warmUp = context.tradingStrategy().warmUpPeriod();
		logWarmUpPeriod(warmUp);

		final TickerSymbolTradingData tradingData = tradingData(
		        equity.tickerDataset(),
		        equity.equityIdentity(),
		        dates,
		        warmUp);

		new BacktestBootstrap(context, output, tradingData).run();
	}

	private TickerSymbolTradingData tradingData(
	        final String tickerDataset,
	        final EquityIdentity equity,
	        final BacktestSimulationDates simulationDate,
	        final Period warmUp ) throws ServiceException {

		final LocalDate startDateInclusive = simulationDate.startDateInclusive().minus(warmUp);
		final LocalDate endDateExclusive = simulationDate.endDateExclusive();

		logPotentialStartDateAdjustment(startDateInclusive);
		logPotentialEndDateWarning(endDateExclusive);

		final LocalDate retrievalStartDate = startDateInclusive.withDayOfMonth(1);

		// Retrieve and cache data range from remote data source
		dataServiceUpdater.get(tickerDataset, equity.tickerSymbol(), retrievalStartDate, endDateExclusive);

		// Retrieve from local cache the desired data range
		final TradingDayPrices[] prices = dataService.get(equity.tickerSymbol(), startDateInclusive, endDateExclusive);

		logdPriceData(prices);

		return new BacktestTickerSymbolTradingData(equity, prices);
	}

	private void logPotentialEndDateWarning( final LocalDate endDateExclsuive ) {

		if (endDateExclsuive.getDayOfMonth() != 1) {
			LOG.debug(
			        "With the current data retrieval implementation, an End date of the first day of the month is more efficient");
		}
	}

	private void logPotentialStartDateAdjustment( final LocalDate startDateInclusive ) {

		if (startDateInclusive.getDayOfMonth() != 1) {
			LOG.debug(
			        "Remote data retrieval for start date: {} has been adjusted to the beginning of the month ",
			        startDateInclusive);
		}
	}

	private void logWarmUpPeriod( final Period warmUpPeriod ) {

		LOG.info(
		        "Simulation Warm Up Period of Days: {}, Months: {}, Years: {}",
		        warmUpPeriod.getDays(),
		        warmUpPeriod.getMonths(),
		        warmUpPeriod.getYears());
	}

	private void logdPriceData( final TradingDayPrices[] prices ) {

		LOG.info("Number of prices for {}: {}", prices[0].tickerSymbol(), prices.length);
	}
}
