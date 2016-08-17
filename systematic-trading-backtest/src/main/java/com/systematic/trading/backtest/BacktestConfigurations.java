package com.systematic.trading.backtest;

import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.model.BacktestSimulationDates;
import com.systematic.trading.model.EquityIdentity;

/**
 * Provides access to the various configurations that are to be back tested.
 * 
 * @author CJ Hare
 */
@FunctionalInterface
public interface BacktestConfigurations {

	List<BacktestBootstrapConfiguration> get( EquityIdentity equity, BacktestSimulationDates simulationDates,
	        DepositConfiguration deposit );
}