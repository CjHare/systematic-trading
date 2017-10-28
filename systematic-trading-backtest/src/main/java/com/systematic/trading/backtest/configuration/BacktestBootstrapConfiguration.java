package com.systematic.trading.backtest.configuration;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.StrategyConfiguration;

/**
 * The set of configurations for the back testing.
 * 
 * @author CJ Hare
 */
public class BacktestBootstrapConfiguration {

	private final BacktestSimulationDates backtestDates;
	private final BrokerageFeesConfiguration brokerageFees;
	private final CashAccountConfiguration cashAccount;
	private final DepositConfiguration deposit;
	private final StrategyConfiguration strategy;
	private final EquityConfiguration equity;

	public BacktestBootstrapConfiguration( final BacktestSimulationDates backtestDates,
	        final BrokerageFeesConfiguration brokerageFees, final CashAccountConfiguration cashAccount,
	        final DepositConfiguration deposit, StrategyConfiguration strategy, final EquityConfiguration equity ) {
		this.backtestDates = backtestDates;
		this.brokerageFees = brokerageFees;
		this.cashAccount = cashAccount;
		this.deposit = deposit;
		this.strategy = strategy;
		this.equity = equity;
	}

	public BacktestSimulationDates getBacktestDates() {
		return backtestDates;
	}

	public BrokerageFeesConfiguration getBrokerageFees() {
		return brokerageFees;
	}

	public CashAccountConfiguration getCashAccount() {
		return cashAccount;
	}

	public DepositConfiguration getDeposit() {
		return deposit;
	}

	public StrategyConfiguration getStrategy() {
		return strategy;
	}

	public EquityConfiguration getEquity() {
		return equity;
	}
}