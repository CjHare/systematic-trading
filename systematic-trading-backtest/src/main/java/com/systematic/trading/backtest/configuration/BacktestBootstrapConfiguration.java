package com.systematic.trading.backtest.configuration;

import java.math.BigDecimal;
import java.util.Optional;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfigurationType;
import com.systematic.trading.backtest.configuration.cash.DepositConfiguration;
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
	private final CashAccountConfigurationType cashAccount;
	private final Optional<DepositConfiguration> deposit;
	private final BigDecimal openingFunds;
	private final StrategyConfiguration strategy;
	private final EquityConfiguration equity;
	private BigDecimal cashAccountInterestRate;

	public BacktestBootstrapConfiguration( final BacktestSimulationDates backtestDates,
	        final BrokerageTransactionFeeStructure brokerageFees, final CashAccountConfigurationType cashAccount,
	        final BigDecimal cashAccountInterestRate, final BigDecimal openingFunds, final DepositConfiguration deposit,
	        StrategyConfiguration strategy, final EquityConfiguration equity ) {
		this.backtestDates = backtestDates;
		this.brokerageFees = brokerageFees;
		this.cashAccount = cashAccount;
		this.cashAccountInterestRate = cashAccountInterestRate;
		this.openingFunds = openingFunds;
		this.deposit = Optional.of(deposit);
		this.strategy = strategy;
		this.equity = equity;
	}

	public BacktestBootstrapConfiguration( final BacktestSimulationDates backtestDates,
	        final BrokerageTransactionFeeStructure brokerageFees, final CashAccountConfigurationType cashAccount,
	        final BigDecimal cashAccountInterestRate, final BigDecimal openingFunds, StrategyConfiguration strategy,
	        final EquityConfiguration equity ) {
		this.backtestDates = backtestDates;
		this.brokerageFees = brokerageFees;
		this.cashAccount = cashAccount;
		this.cashAccountInterestRate = cashAccountInterestRate;
		this.openingFunds = openingFunds;
		this.deposit = Optional.empty();
		this.strategy = strategy;
		this.equity = equity;
	}

	public BacktestSimulationDates backtestDates() {

		return backtestDates;
	}

	public BrokerageTransactionFeeStructure brokerageFees() {

		return brokerageFees;
	}

	public CashAccountConfigurationType cashAccount() {

		return cashAccount;
	}

	public BigDecimal openingFunds() {

		return openingFunds;
	}

	public Optional<DepositConfiguration> deposit() {

		return deposit;
	}

	public StrategyConfiguration strategy() {

		return strategy;
	}

	public EquityConfiguration equity() {

		return equity;
	}

	public BigDecimal cashAccountInterestRate() {

		return cashAccountInterestRate;
	}
}