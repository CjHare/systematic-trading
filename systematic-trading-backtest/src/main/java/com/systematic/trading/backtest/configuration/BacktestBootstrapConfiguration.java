package com.systematic.trading.backtest.configuration;

import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.backtest.model.BacktestSimulationDates;

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
	private final EntryLogicConfiguration entry;
	private final EquityConfiguration equity;
	private final ExitLogicConfiguration exit;
	private final MaximumTrade maximumTrade;
	private final MinimumTrade minimumTrade;

	public BacktestBootstrapConfiguration(final BacktestSimulationDates backtestDates,
	        final BrokerageFeesConfiguration brokerageFees, final CashAccountConfiguration cashAccount,
	        final DepositConfiguration deposit, final EntryLogicConfiguration entry, final EquityConfiguration equity,
	        final ExitLogicConfiguration exit, final MaximumTrade maximumTrade, final MinimumTrade minimumTrade) {
		this.backtestDates = backtestDates;
		this.brokerageFees = brokerageFees;
		this.cashAccount = cashAccount;
		this.deposit = deposit;
		this.entry = entry;
		this.equity = equity;
		this.exit = exit;
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
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

	public EntryLogicConfiguration getEntryLogic() {
		return entry;
	}

	public EquityConfiguration getEquity() {
		return equity;
	}

	public ExitLogicConfiguration getExitLogic() {
		return exit;
	}

	public MaximumTrade getMaximumTrade() {
		return maximumTrade;
	}

	public MinimumTrade getMinimumTrade() {
		return minimumTrade;
	}
}