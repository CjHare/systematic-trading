package com.systematic.trading.backtest;

import java.time.Period;
import java.util.List;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;

/**
 * Provides access to the various configurations that are to be back tested.
 * 
 * @author CJ Hare
 */
public interface BacktestConfiguration {

	List<BacktestBootstrapConfiguration> get( EquityConfiguration equity, BacktestSimulationDates simulationDates,
	        DepositConfiguration deposit );

	/**
	 * Largest perio warm up required by any the strategies.
	 */
	Period getWarmUpPeriod();
}