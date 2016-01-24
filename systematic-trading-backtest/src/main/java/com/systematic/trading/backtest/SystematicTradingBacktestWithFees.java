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
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFactory;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFilterConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.IndicatorSignalGeneratorFactory;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.RsiConfiguration;
import com.systematic.trading.backtest.configuration.signals.SmaConfiguration;
import com.systematic.trading.backtest.display.BacktestDisplay;
import com.systematic.trading.backtest.display.file.FileClearDestination;
import com.systematic.trading.backtest.display.file.FileMinimalDisplay;
import com.systematic.trading.backtest.model.TickerSymbolTradingDataBacktest;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.FlatEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.LadderedEquityManagementFeeCalculator;
import com.systematic.trading.simulation.equity.fee.management.PeriodicEquityManagementFeeStructure;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.logic.HoldForeverExitLogic;
import com.systematic.trading.simulation.logic.RelativeTradeValue;
import com.systematic.trading.simulation.logic.TradeValue;

/**
 * Performs back testing of trading logic over a historical data set.
 * <p/>
 * Decides the persistence type to use, in addition to the type of back testing and equity it is
 * performed on.
 * 
 * @author CJ Hare
 */
public class SystematicTradingBacktestWithFees {

	// TODO this is a duplicate of SystematicTradingBacktest but with different fees - refacrtor!!

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( SystematicTradingBacktestWithFees.class );

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int DAYS_IN_A_YEAR = 365;

	/** Minimum amount of historical data needed for back testing. */
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;

	private static final Period ONE_YEAR = Period.ofYears( 1 );

	public static void main( final String... args ) throws Exception {

		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool( cores );

		final EquityIdentity equity = getEquityIdentity();

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final BigDecimal depositAmount = getDepositAmount( args );
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );
		final List<BacktestBootstrapConfiguration> configurations = getConfigurations( equity, startDate, endDate,
				depositAmount );

		final String baseOutputDirectory = getBaseOutputDirectory( args );

		// Arrange output to files, only once per a run
		new FileClearDestination( baseOutputDirectory );

		try {
			final TickerSymbolTradingData tradingData = getTradingData( equity, startDate, endDate );

			for (final BacktestBootstrapConfiguration configuration : configurations) {
				final String outputDirectory = getOutputDirectory( baseOutputDirectory, equity, configuration );
				// final BacktestDisplay fileDisplay = new FileDisplay( outputDirectory, pool,
				// MATH_CONTEXT );
				final BacktestDisplay fileDisplay = new FileMinimalDisplay( outputDirectory, pool, MATH_CONTEXT );

				final BacktestBootstrap bootstrap = new BacktestBootstrap( tradingData, configuration, fileDisplay,
						MATH_CONTEXT );

				LOG.info( String.format( "Backtesting beginning for: %s", configuration.getDescription() ) );

				bootstrap.run();

				LOG.info( String.format( "Backtesting complete for: %s", configuration.getDescription() ) );
			}

			LOG.info( "All Simulations have been completed" );

		} finally {
			HibernateUtil.getSessionFactory().close();
			pool.shutdown();
		}
	}

	private static TickerSymbolTradingData getTradingData( final EquityIdentity equity, final LocalDate startDate,
			final LocalDate endDate ) {

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( equity.getTickerSymbol(), startDate, endDate );

		final DataService service = HibernateDataService.getInstance();
		final TradingDayPrices[] data = service.get( equity.getTickerSymbol(), startDate, endDate );

		final TickerSymbolTradingData tradingData = new TickerSymbolTradingDataBacktest( equity, data );

		return tradingData;

	}

	private static ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}

	private static EquityManagementFeeCalculator getVanguardRetailFeeCalculator() {
		final BigDecimal[] vanguardFeeRange = { BigDecimal.valueOf( 50000 ), BigDecimal.valueOf( 100000 ) };
		final BigDecimal[] vanguardPercentageFee = { BigDecimal.valueOf( 0.009 ), BigDecimal.valueOf( 0.006 ),
				BigDecimal.valueOf( 0.0035 ) };
		return new LadderedEquityManagementFeeCalculator( vanguardFeeRange, vanguardPercentageFee, MATH_CONTEXT );
	}

	private static EquityManagementFeeCalculator getVanguardEftFeeCalculator() {
		return new FlatEquityManagementFeeCalculator( BigDecimal.valueOf( 0.0018 ), MATH_CONTEXT );
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final EquityIdentity equityIdentity,
			final LocalDate startDate, final LocalDate endDate, final BigDecimal depositAmount ) {
		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();

		final Period depositFrequency = Period.ofDays( 7 );

		CashAccount cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency, MATH_CONTEXT );
		final LocalDate managementFeeStartDate = LocalDate.of( startDate.getYear(), 1, 1 );

		final EquityManagementFeeCalculator vanguardRetailFeeCalculator = getVanguardRetailFeeCalculator();
		EquityConfiguration equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
				managementFeeStartDate, vanguardRetailFeeCalculator, ONE_YEAR ) );
		Brokerage vanguard = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.VANGUARD_RETAIL, startDate,
				MATH_CONTEXT );
		EntryLogic entryLogic = EntryLogicFactory.create( equityIdentity, startDate, depositFrequency, MATH_CONTEXT );
		BacktestBootstrapConfiguration configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(),
				vanguard, cashAccount, "BuyWeekly_HoldForever" );
		configurations.add( configuration );

		// Configuration with different entry values
		final BigDecimal[] minimumTradeValues = { BigDecimal.valueOf( 500 ), BigDecimal.valueOf( 1000 ),
				BigDecimal.valueOf( 1500 ), BigDecimal.valueOf( 2000 ) };

		final BigDecimal[] maximumTradeValues = { BigDecimal.valueOf( .25 ), BigDecimal.valueOf( .5 ),
				BigDecimal.valueOf( .75 ), BigDecimal.valueOf( 1 ) };

		IndicatorSignalGenerator sma, macd, rsi;
		EquityManagementFeeCalculator vanguardEtfFeeCalculator;
		Brokerage cmcMarkets;
		String description;

		// TODO different RSI values
		final RsiConfiguration rsiConfiguration = RsiConfiguration.MEDIUM;

		// TODO tidy up
		// TODO move the description out into the BacktestBootstrapConfiguration
		// TODO make the factories consistent in their approach
		for (final BigDecimal maximumTradeValue : maximumTradeValues) {

			for (final BigDecimal minimumTradeValue : minimumTradeValues) {

				final TradeValue tradeValue = new RelativeTradeValue( minimumTradeValue, maximumTradeValue,
						MATH_CONTEXT );

				final String minimumTradeDescription = String.valueOf( minimumTradeValue.longValue() );
				final String maximumTradeDescription = String.valueOf( maximumTradeValue.doubleValue() );

				for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {

					macd = IndicatorSignalGeneratorFactory.create( macdConfiguration, MATH_CONTEXT );

					entryLogic = EntryLogicFactory.create( equityIdentity, tradeValue,
							EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, macd );
					description = String.format( "%s_Minimum-%s_Maximum-%s_HoldForever",
							macdConfiguration.getDescription(), minimumTradeDescription, maximumTradeDescription );
					vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
					equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
							managementFeeStartDate, vanguardEtfFeeCalculator, ONE_YEAR ) );
					cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS, startDate,
							MATH_CONTEXT );
					cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency, MATH_CONTEXT );
					configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets,
							cashAccount, description );
					configurations.add( configuration );

					macd = IndicatorSignalGeneratorFactory.create( macdConfiguration, MATH_CONTEXT );
					rsi = IndicatorSignalGeneratorFactory.create( rsiConfiguration, MATH_CONTEXT );

					entryLogic = EntryLogicFactory.create( equityIdentity, tradeValue,
							EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, rsi, macd );
					description = String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever",
							macdConfiguration.getDescription(), rsiConfiguration.getDescription(),
							minimumTradeDescription, maximumTradeDescription );
					vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
					equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
							managementFeeStartDate, vanguardEtfFeeCalculator, ONE_YEAR ) );
					cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS, startDate,
							MATH_CONTEXT );
					cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency, MATH_CONTEXT );
					configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets,
							cashAccount, description );
					configurations.add( configuration );

					for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {

						sma = IndicatorSignalGeneratorFactory.create( smaConfiguration, MATH_CONTEXT );
						macd = IndicatorSignalGeneratorFactory.create( macdConfiguration, MATH_CONTEXT );

						entryLogic = EntryLogicFactory.create( equityIdentity, tradeValue,
								EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, sma, macd );
						description = String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever",
								macdConfiguration.getDescription(), smaConfiguration.getDescription(),
								minimumTradeDescription, maximumTradeDescription );
						vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
						equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
								managementFeeStartDate, vanguardEtfFeeCalculator, ONE_YEAR ) );
						cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS,
								startDate, MATH_CONTEXT );
						cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency,
								MATH_CONTEXT );
						configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets,
								cashAccount, description );
						configurations.add( configuration );

						sma = IndicatorSignalGeneratorFactory.create( smaConfiguration, MATH_CONTEXT );
						macd = IndicatorSignalGeneratorFactory.create( macdConfiguration, MATH_CONTEXT );
						rsi = IndicatorSignalGeneratorFactory.create( rsiConfiguration, MATH_CONTEXT );

						entryLogic = EntryLogicFactory.create( equityIdentity, tradeValue,
								EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, rsi, sma, macd );
						description = String.format( "%s-%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever",
								macdConfiguration.getDescription(), smaConfiguration.getDescription(),
								rsiConfiguration.getDescription(), minimumTradeDescription, maximumTradeDescription );
						vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
						equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
								managementFeeStartDate, vanguardEtfFeeCalculator, ONE_YEAR ) );
						cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS,
								startDate, MATH_CONTEXT );
						cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency,
								MATH_CONTEXT );
						configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets,
								cashAccount, description );
						configurations.add( configuration );
					}
				}

				for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {
					description = String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever",
							smaConfiguration.getDescription(), rsiConfiguration.getDescription(),
							minimumTradeDescription, maximumTradeDescription );

					rsi = IndicatorSignalGeneratorFactory.create( rsiConfiguration, MATH_CONTEXT );
					sma = IndicatorSignalGeneratorFactory.create( smaConfiguration, MATH_CONTEXT );

					entryLogic = EntryLogicFactory.create( equityIdentity, tradeValue,
							EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, rsi, sma );
					vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
					equity = new EquityConfiguration( equityIdentity, new PeriodicEquityManagementFeeStructure(
							managementFeeStartDate, vanguardEtfFeeCalculator, ONE_YEAR ) );
					cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS, startDate,
							MATH_CONTEXT );
					cashAccount = CashAccountFactory.create( startDate, depositAmount, depositFrequency, MATH_CONTEXT );
					configuration = new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets,
							cashAccount, description );
					configurations.add( configuration );
				}
			}
		}

		return configurations;
	}

	private static EquityIdentity getEquityIdentity() {
		final String tickerSymbol = "^GSPC"; 	// S&P 500 - price return index
		final EquityClass equityType = EquityClass.STOCK;
		return new EquityIdentity( tickerSymbol, equityType );
	}

	private static String getOutputDirectory( final String baseOutputDirectory, final EquityIdentity equity,
			final BacktestBootstrapConfiguration configuration ) {
		return String.format( "%s%s_%s", baseOutputDirectory, equity.getTickerSymbol(),
				configuration.getDescription() );
	}

	private static String getBaseOutputDirectory( final String... args ) {

		if (args != null && args.length > 1) {
			return args[1];
		}

		return "../../simulations-managment-fee/";
	}

	private static BigDecimal getDepositAmount( final String... args ) {

		if (args != null && args.length > 0) {
			return BigDecimal.valueOf( Double.parseDouble( args[0] ) );
		}

		return BigDecimal.valueOf( 100 );
	}
}
