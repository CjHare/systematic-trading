package com.systematic.trading.backtest;

import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;

/**
 * Provides access to the various configurations that are to be back tested.
 * 
 * @author CJ Hare
 */
public interface BacktestConfiguration {

	List<BacktestBootstrapConfiguration> configuration(
	        EquityConfiguration equity,
	        BacktestSimulationDates simulationDates,
	        CashAccountConfiguration cashAccount );
}
