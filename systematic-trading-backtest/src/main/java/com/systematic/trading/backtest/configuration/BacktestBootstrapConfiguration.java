package com.systematic.trading.backtest.configuration;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfigurationType;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;

/**
 * The set of configurations for the back testing.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapConfiguration {

	private final BacktestSimulationDates backtestDates;
	private final BrokerageTransactionFeeStructure brokerageFees;
	private final CashAccountConfigurationType cashAccountType;
	private final StrategyConfiguration strategy;
	private final EquityConfiguration equity;
	private final CashAccountConfiguration cashAccount;

	public BacktestBootstrapConfiguration( final BacktestSimulationDates backtestDates,
	        final BrokerageTransactionFeeStructure brokerageFees, final CashAccountConfigurationType cashAccountType,
	        final CashAccountConfiguration cashAccount, StrategyConfiguration strategy,
	        final EquityConfiguration equity ) {
		this.backtestDates = backtestDates;
		this.brokerageFees = brokerageFees;
		this.cashAccount = cashAccount;
		this.cashAccountType = cashAccountType;
		this.strategy = strategy;
		this.equity = equity;
	}

	public BacktestSimulationDates backtestDates() {

		return backtestDates;
	}

	public BrokerageTransactionFeeStructure brokerageFees() {

		return brokerageFees;
	}

	public CashAccountConfigurationType cashAccountType() {

		return cashAccountType;
	}

	public CashAccountConfiguration cashAccount() {

		return cashAccount;
	}

	public StrategyConfiguration strategy() {

		return strategy;
	}

	public EquityConfiguration equity() {

		return equity;
	}
}