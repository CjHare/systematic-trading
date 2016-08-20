package com.systematic.trading.backtest;

import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.model.BacktestSimulationDates;

/**
 * Provides access to the various configurations that are to be back tested.
 * 
 * @author CJ Hare
 */
@FunctionalInterface
public interface BacktestConfiguration {

	List<BacktestBootstrapConfiguration> get( EquityConfiguration equity, BacktestSimulationDates simulationDates,
	        DepositConfiguration deposit );
}