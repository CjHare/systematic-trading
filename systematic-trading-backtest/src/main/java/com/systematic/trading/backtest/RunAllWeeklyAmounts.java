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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFactoroy;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountFactory;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFactory;
import com.systematic.trading.backtest.configuration.entry.EntryLogicFilterConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.IndicatorSignalGeneratorFactory;
import com.systematic.trading.backtest.configuration.signals.MacdConfiguration;
import com.systematic.trading.backtest.configuration.signals.MaximumTrade;
import com.systematic.trading.backtest.configuration.signals.MinimumTrade;
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
 * Values 100, 150, 200, 250, 300, 500.
 * 
 * @author CJ Hare
 */
public class RunAllWeeklyAmounts {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( RunAllWeeklyAmounts.class );

	/** Accuracy for BigDecimal operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	/** Minimum amount of historical data needed for back testing. */
	private static final int DAYS_IN_A_YEAR = 365;
	private static final int HISTORY_REQUIRED = 10 * DAYS_IN_A_YEAR;
	private static final Period ONE_YEAR = Period.ofYears( 1 );

	/** Number of decimal places the equity are traded in. */
	private static final int EQUITY_SCALE = 4;

	public static void main( final String... args ) throws Exception {

		final String baseOutputDirectory = getBaseOutputDirectory( args );

		final DepositConfiguration[] depositAmounts = { DepositConfiguration.WEEKLY_100,
				DepositConfiguration.WEEKLY_150, DepositConfiguration.WEEKLY_200, DepositConfiguration.WEEKLY_250,
				DepositConfiguration.WEEKLY_300, DepositConfiguration.WEEKLY_500 };

		final EquityIdentity equity = getEquityIdentity();

		// Date range is from the first of the starting month until now
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS ).withDayOfMonth( 1 );

		final TickerSymbolTradingData tradingData = getTradingData( equity, startDate, endDate );

		final int cores = Runtime.getRuntime().availableProcessors();
		final ExecutorService pool = Executors.newFixedThreadPool( cores );

		try {
			for (final DepositConfiguration depositAmount : depositAmounts) {

				final List<BacktestBootstrapConfiguration> configurations = getConfigurations( equity, startDate,
						endDate, depositAmount, EQUITY_SCALE );
				final String outputDirectory = String.format( baseOutputDirectory, depositAmount );

				runTest( depositAmount, outputDirectory, configurations, tradingData, equity, pool );
			}

		} finally {
			HibernateUtil.getSessionFactory().close();
			pool.shutdown();
		}
	}

	private static BacktestDisplay getDisplay( final String outputDirectory, final ExecutorService pool )
			throws IOException {

		// return new FileDisplay( outputDirectory, pool, MATH_CONTEXT );
		return new FileMinimalDisplay( outputDirectory, pool, MATH_CONTEXT );
		// return new FileNoDisplay();
	}

	public static void runTest( final DepositConfiguration depositAmount, final String baseOutputDirectory,
			final List<BacktestBootstrapConfiguration> configurations, final TickerSymbolTradingData tradingData,
			final EquityIdentity equity, final ExecutorService pool ) throws Exception {

		// Arrange output to files, only once per a run
		new FileClearDestination( baseOutputDirectory );

		for (final BacktestBootstrapConfiguration configuration : configurations) {
			final String outputDirectory = getOutputDirectory( baseOutputDirectory, equity, configuration );
			final BacktestDisplay fileDisplay = getDisplay( outputDirectory, pool );

			final BacktestBootstrap bootstrap = new BacktestBootstrap( tradingData, configuration, fileDisplay,
					MATH_CONTEXT );

			LOG.info( String.format( "Backtesting beginning for: %s", configuration.getDescription() ) );

			bootstrap.run();

			LOG.info( String.format( "Backtesting complete for: %s", configuration.getDescription() ) );
		}

		LOG.info( String.format( "All Simulations have been completed for deposit amount: %s", depositAmount ) );

	}

	private static TickerSymbolTradingData getTradingData( final EquityIdentity equity, final LocalDate startDate,
			final LocalDate endDate ) {

		// Retrieve and cache data range from remote data source
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( equity.getTickerSymbol(), startDate, endDate );

		// Retrieve from local cache the desired data range
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
		// new ZeroEquityManagementFeeStructure()
		return new FlatEquityManagementFeeCalculator( BigDecimal.valueOf( 0.0018 ), MATH_CONTEXT );
	}

	private static LocalDate getFirstDayOfYear( final LocalDate date ) {
		return LocalDate.of( date.getYear(), 1, 1 );
	}

	private static BacktestBootstrapConfiguration getVanguardRetailConfiguration( final EquityIdentity equityIdentity,
			final int equityScale, final LocalDate startDate, final LocalDate endDate,
			final DepositConfiguration deposit, final LocalDate managementFeeStartDate ) {

		final CashAccount cashAccount = CashAccountFactory.create( startDate, deposit, MATH_CONTEXT );
		final EquityManagementFeeCalculator vanguardRetailFeeCalculator = getVanguardRetailFeeCalculator();
		final EquityConfiguration equity = new EquityConfiguration( equityIdentity,
				new PeriodicEquityManagementFeeStructure( managementFeeStartDate, vanguardRetailFeeCalculator,
						ONE_YEAR ) );
		final Brokerage vanguard = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.VANGUARD_RETAIL,
				startDate, MATH_CONTEXT );
		final EntryLogic entryLogic = EntryLogicFactory.create( equityIdentity, equityScale, startDate, deposit,
				MATH_CONTEXT );
		return new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), vanguard, cashAccount,
				"VaguardRetail_BuyWeekly_HoldForever" );
	}

	private static BacktestBootstrapConfiguration getCmCMarketsWeekly( final EquityIdentity equityIdentity,
			final int equityScale, final LocalDate startDate, final LocalDate endDate,
			final DepositConfiguration deposit, final LocalDate managementFeeStartDate ) {

		final EquityManagementFeeCalculator vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
		final EquityConfiguration equity = new EquityConfiguration( equityIdentity,
				new PeriodicEquityManagementFeeStructure( managementFeeStartDate, vanguardEtfFeeCalculator,
						ONE_YEAR ) );
		final Brokerage cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS,
				startDate, MATH_CONTEXT );
		final EntryLogic entryLogic = EntryLogicFactory.create( equityIdentity, equityScale, startDate, deposit,
				MATH_CONTEXT );
		final CashAccount cashAccount = CashAccountFactory.create( startDate, deposit, MATH_CONTEXT );
		return new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets, cashAccount,
				"CMC_BuyWeekly_HoldForever" );
	}

	private static BacktestBootstrapConfiguration getCmCMarketsMonthly( final EquityIdentity equityIdentity,
			final int equityScale, final LocalDate startDate, final LocalDate endDate,
			final DepositConfiguration deposit, final LocalDate managementFeeStartDate ) {

		final EquityManagementFeeCalculator vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
		final EquityConfiguration equity = new EquityConfiguration( equityIdentity,
				new PeriodicEquityManagementFeeStructure( managementFeeStartDate, vanguardEtfFeeCalculator,
						ONE_YEAR ) );
		final Brokerage cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS,
				startDate, MATH_CONTEXT );
		final EntryLogic entryLogic = EntryLogicFactory.create( equityIdentity, equityScale, startDate,
				Period.ofMonths( 1 ), MATH_CONTEXT );
		final CashAccount cashAccount = CashAccountFactory.create( startDate, deposit, MATH_CONTEXT );
		return new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets, cashAccount,
				"CMC_BuyMonthly_HoldForever" );
	}

	private static BacktestBootstrapConfiguration getConfiguration( final EquityIdentity equityIdentity,
			final int equityScale, final LocalDate startDate, final LocalDate endDate,
			final DepositConfiguration deposit, final LocalDate managementFeeStartDate, final TradeValue tradeValue,
			final String description, final IndicatorSignalGenerator... entrySignals ) {

		final EntryLogic entryLogic = EntryLogicFactory.create( equityIdentity, equityScale, tradeValue,
				EntryLogicFilterConfiguration.SAME_DAY, MATH_CONTEXT, entrySignals );
		final EquityManagementFeeCalculator vanguardEtfFeeCalculator = getVanguardEftFeeCalculator();
		final EquityConfiguration equity = new EquityConfiguration( equityIdentity,
				new PeriodicEquityManagementFeeStructure( managementFeeStartDate, vanguardEtfFeeCalculator,
						ONE_YEAR ) );
		final Brokerage cmcMarkets = BrokerageFactoroy.create( equity, BrokerageFeesConfiguration.CMC_MARKETS,
				startDate, MATH_CONTEXT );
		final CashAccount cashAccount = CashAccountFactory.create( startDate, deposit, MATH_CONTEXT );
		return new BacktestBootstrapConfiguration( entryLogic, getExitLogic(), cmcMarkets, cashAccount, description );
	}

	private static List<BacktestBootstrapConfiguration> getConfigurations( final EquityIdentity equityIdentity,
			final LocalDate startDate, final LocalDate endDate, final DepositConfiguration deposit,
			final int equityScale ) {

		final List<BacktestBootstrapConfiguration> configurations = new ArrayList<BacktestBootstrapConfiguration>();
		final LocalDate managementFeeStartDate = getFirstDayOfYear( startDate );
		BacktestBootstrapConfiguration configuration;

		// Vanguard Retail
		configuration = getVanguardRetailConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
				managementFeeStartDate );
		configurations.add( configuration );

		// CMC Weekly
		configuration = getCmCMarketsWeekly( equityIdentity, equityScale, startDate, endDate, deposit,
				managementFeeStartDate );
		configurations.add( configuration );

		// CMC Monthly
		configuration = getCmCMarketsMonthly( equityIdentity, equityScale, startDate, endDate, deposit,
				managementFeeStartDate );
		configurations.add( configuration );

		String description;
		IndicatorSignalGenerator sma, macd, rsi;

		// TODO different RSI values
		final RsiConfiguration rsiConfiguration = RsiConfiguration.MEDIUM;

		// TODO tidy up
		// TODO make the factories consistent in their approach
		for (final MaximumTrade maximumTrade : MaximumTrade.values()) {
			for (final MinimumTrade minimumTrade : MinimumTrade.values()) {

				final TradeValue tradeValue = new RelativeTradeValue( minimumTrade.getValue(), maximumTrade.getValue(),
						MATH_CONTEXT );

				for (final MacdConfiguration macdConfiguration : MacdConfiguration.values()) {

					// MACD only
					macd = createSignalGenerator( macdConfiguration );
					description = getDescription( minimumTrade, maximumTrade, macdConfiguration );
					configuration = getConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
							managementFeeStartDate, tradeValue, description, macd );
					configurations.add( configuration );

					// MACD & RSI
					macd = createSignalGenerator( macdConfiguration );
					rsi = createSignalGenerator( rsiConfiguration );
					description = getDescription( minimumTrade, maximumTrade, macdConfiguration, rsiConfiguration );
					configuration = getConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
							managementFeeStartDate, tradeValue, description, macd, rsi );
					configurations.add( configuration );

					for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {

						// SMA & MACD
						sma = createSignalGenerator( smaConfiguration );
						macd = createSignalGenerator( macdConfiguration );
						description = getDescription( minimumTrade, maximumTrade, macdConfiguration, smaConfiguration );
						configuration = getConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
								managementFeeStartDate, tradeValue, description, sma, macd );
						configurations.add( configuration );

						// SMA, MACD & RSI
						sma = createSignalGenerator( smaConfiguration );
						macd = createSignalGenerator( macdConfiguration );
						rsi = createSignalGenerator( rsiConfiguration );
						description = getDescription( minimumTrade, maximumTrade, macdConfiguration, smaConfiguration,
								rsiConfiguration );
						configuration = getConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
								managementFeeStartDate, tradeValue, description, sma, macd, rsi );
						configurations.add( configuration );
					}
				}

				for (final SmaConfiguration smaConfiguration : SmaConfiguration.values()) {

					// RSI & SMA
					rsi = createSignalGenerator( rsiConfiguration );
					sma = createSignalGenerator( smaConfiguration );
					description = getDescription( minimumTrade, maximumTrade, smaConfiguration, rsiConfiguration );
					configuration = getConfiguration( equityIdentity, equityScale, startDate, endDate, deposit,
							managementFeeStartDate, tradeValue, description, sma, rsi );
					configurations.add( configuration );
				}
			}
		}

		return configurations;
	}

	private static IndicatorSignalGenerator createSignalGenerator( final RsiConfiguration rsiConfiguration ) {
		return IndicatorSignalGeneratorFactory.create( rsiConfiguration, MATH_CONTEXT );
	}

	private static IndicatorSignalGenerator createSignalGenerator( final SmaConfiguration smaConfiguration ) {
		return IndicatorSignalGeneratorFactory.create( smaConfiguration, MATH_CONTEXT );
	}

	private static IndicatorSignalGenerator createSignalGenerator( final MacdConfiguration macdConfiguration ) {
		return IndicatorSignalGeneratorFactory.create( macdConfiguration, MATH_CONTEXT );
	}

	private static String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final MacdConfiguration macdConfiguration ) {
		return String.format( "%s_Minimum-%s_Maximum-%s_HoldForever", macdConfiguration.getDescription(),
				minimumTrade.getDescription(), maximumTrade.getDescription() );
	}

	private static String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final MacdConfiguration macdConfiguration, final RsiConfiguration rsiConfiguration ) {
		return String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", macdConfiguration.getDescription(),
				rsiConfiguration.getDescription(), minimumTrade.getDescription(), maximumTrade.getDescription() );
	}

	private static String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final MacdConfiguration macdConfiguration, final SmaConfiguration smaConfiguration ) {
		return String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", macdConfiguration.getDescription(),
				smaConfiguration.getDescription(), minimumTrade.getDescription(), maximumTrade.getDescription() );
	}

	private static String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final MacdConfiguration macdConfiguration, final SmaConfiguration smaConfiguration,
			final RsiConfiguration rsiConfiguration ) {
		return String.format( "%s-%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", macdConfiguration.getDescription(),
				smaConfiguration.getDescription(), rsiConfiguration.getDescription(), minimumTrade.getDescription(),
				maximumTrade.getDescription() );
	}

	private static String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final SmaConfiguration smaConfiguration, final RsiConfiguration rsiConfiguration ) {
		return String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", smaConfiguration.getDescription(),
				rsiConfiguration.getDescription(), minimumTrade.getDescription(), maximumTrade.getDescription() );
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

		if (args != null && args.length > 0) {
			return args[0];
		}

		return "../../simulations/";
	}
}
