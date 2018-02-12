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
package com.systematic.trading.analysis;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.analysis.event.LogEntryOrderEventListner;
import com.systematic.trading.backtest.Backtest;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfigurationFactory;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.EmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.IndicatorConfigurationTranslator;
import com.systematic.trading.backtest.configuration.strategy.indicator.RsiConfiguration;
import com.systematic.trading.backtest.configuration.strategy.indicator.SmaUptrendConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.backtest.context.BacktestBootstrapContextBulider;
import com.systematic.trading.backtest.description.DescriptionGenerator;
import com.systematic.trading.backtest.description.StandardDescriptionGenerator;
import com.systematic.trading.backtest.event.BacktestEventListener;
import com.systematic.trading.backtest.exception.BacktestInitialisationException;
import com.systematic.trading.backtest.exception.InvalidSimulationDatesException;
import com.systematic.trading.backtest.input.BacktestEndDate;
import com.systematic.trading.backtest.input.BacktestStartDate;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceType;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.input.AnalysisLaunchArguments;
import com.systematic.trading.input.CommandLineLaunchArgumentsParser;
import com.systematic.trading.input.DataServiceTypeLaunchArgument;
import com.systematic.trading.input.EquityArguments;
import com.systematic.trading.input.EquityDatasetLaunchArgument;
import com.systematic.trading.input.LaunchArgument.ArgumentKey;
import com.systematic.trading.input.LaunchArgumentValidator;
import com.systematic.trading.input.OpeningFundsLaunchArgument;
import com.systematic.trading.input.TickerSymbolLaunchArgument;
import com.systematic.trading.model.equity.EquityClass;

/**
 * An analysis to generate buy signals to execute on a daily basis, a specialized version of a back
 * test with today as the end date.
 * 
 * @author CJ Hare
 */
public class EntryOrderAnalysis {

	/** Classes' logger. */
	private static final Logger LOG = LogManager.getLogger(EntryOrderAnalysis.class);

	/** Days of signals analysis to generate and display. */
	private static final int DAYS_OF_SIGNALS = 3;

	/** For analysis we only want to use the starting funds, no interest payments. */
	private static final BigDecimal IGNORE_INTEREST_RATE = BigDecimal.ZERO;

	/** Ensures all the necessary trading data get retrieved into the local source. */
	private final DataServiceUpdater dataServiceUpdater;

	/** Local source of the trading prices. */
	private final DataService dataService;

	/** Display name for the strategy analysed. */
	private final DescriptionGenerator description;

	public static void main( final String... args ) throws ServiceException {

		final LaunchArgumentValidator validator = new LaunchArgumentValidator();
		final Map<ArgumentKey, String> arguments = new CommandLineLaunchArgumentsParser().parse(args);
		final AnalysisLaunchArguments launchArgs = new AnalysisLaunchArguments(
		        new EquityArguments(new DataServiceTypeLaunchArgument(), new EquityDatasetLaunchArgument(validator),
		                new TickerSymbolLaunchArgument(validator), arguments),
		        new OpeningFundsLaunchArgument(validator), arguments);

		new EntryOrderAnalysis(launchArgs.dataService()).run(launchArgs);
	}

	public EntryOrderAnalysis( final DataServiceType serviceType ) throws BacktestInitialisationException {

		try {
			this.dataServiceUpdater = new DataServiceUpdaterImpl(serviceType);
		} catch (ServiceException e) {
			throw new BacktestInitialisationException(e);
		}

		this.dataService = new HibernateDataService();
		this.description = new StandardDescriptionGenerator();
	}

	private EquityConfiguration equity( final AnalysisLaunchArguments launchArgs ) {

		return new EquityConfiguration(launchArgs.equityDataset(), launchArgs.tickerSymbol(), EquityClass.STOCK);
	}

	private void run( final AnalysisLaunchArguments launchArgs ) throws ServiceException {

		final EquityConfiguration equity = equity(launchArgs);
		final BacktestBootstrapConfiguration backtestConfiguration = configuration(equity, launchArgs.openingFunds());
		recordStrategy(backtestConfiguration.strategy());
		recordAnalysisPeriod(backtestConfiguration.backtestDates());

		final StopWatch timer = new StopWatch();
		timer.start();

		try {
			new Backtest(dataService, dataServiceUpdater).run(equity, backtestConfiguration.backtestDates(),
			        context(backtestConfiguration, output()), output());

		} finally {
			HibernateUtil.sessionFactory().close();
		}

		timer.stop();
		recordExecutionTime(timer);
	}

	private BacktestBootstrapContext context( final BacktestBootstrapConfiguration config,
	        final BacktestEventListener listener ) {

		return new BacktestBootstrapContextBulider().withConfiguration(config).withSignalAnalysisListeners(listener)
		        .build();
	}

	private void recordStrategy( final StrategyConfiguration strategy ) {

		LOG.info("{}", () -> String.format("Strategy: %s", strategy.description(description)));
	}

	private void recordExecutionTime( final StopWatch timer ) {

		LOG.info(() -> String.format("Finished, time taken: %s", Duration.ofMillis(timer.getTime())));
	}

	private void recordAnalysisPeriod( final BacktestSimulationDates analysisPeriod ) {

		LOG.info("{}", () -> String.format("Analysis start: %s, end: %s", analysisPeriod.startDate(),
		        analysisPeriod.endDate()));
	}

	private BacktestBootstrapConfiguration configuration( final EquityConfiguration equity,
	        final BigDecimal openingFunds ) throws InvalidSimulationDatesException {

		final StrategyConfiguration strategy = strategy();
		final LocalDate today = LocalDate.now();

		final BacktestSimulationDates simulationDates = new BacktestSimulationDates(
		        new BacktestStartDate(today.minus(strategy.entry().priceDataRange()).minusDays(DAYS_OF_SIGNALS)),
		        new BacktestEndDate(today));

		return new BacktestBootstrapConfiguration(simulationDates, new SelfWealthBrokerageFees(),
		        new CashAccountConfiguration(IGNORE_INTEREST_RATE, openingFunds), strategy, equity);
	}

	private BacktestEventListener output() {

		return new LogEntryOrderEventListner();
	}

	/**
	 * Hard coded strategy: entry: (SMA-Long OR EMA-Long) AND RSI-Short
	 */
	private StrategyConfiguration strategy() {

		final IndicatorConfigurationTranslator converter = new IndicatorConfigurationTranslator();
		final StrategyConfigurationFactory factory = new StrategyConfigurationFactory();
		final EmaUptrendConfiguration emaConfiguration = EmaUptrendConfiguration.LONG;
		final SmaUptrendConfiguration smaConfiguration = SmaUptrendConfiguration.LONG;
		final RsiConfiguration rsiConfiguration = RsiConfiguration.SHORT;
		final MinimumTrade minimumTrade = MinimumTrade.ZERO;
		final MaximumTrade maximumTrade = MaximumTrade.ALL;
		final EntryConfiguration entry = factory.entry(
		        factory.entry(factory.entry(converter.translate(emaConfiguration)), OperatorConfiguration.Selection.OR,
		                factory.entry(converter.translate(smaConfiguration))),
		        OperatorConfiguration.Selection.AND, factory.entry(converter.translate(rsiConfiguration)));
		final EntrySizeConfiguration entryPositionSizing = new EntrySizeConfiguration(minimumTrade, maximumTrade);
		final ExitConfiguration exit = factory.exit();
		final ExitSizeConfiguration exitPositionSizing = new ExitSizeConfiguration();

		return factory.strategy(entry, entryPositionSizing, exit, exitPositionSizing);
	}
}