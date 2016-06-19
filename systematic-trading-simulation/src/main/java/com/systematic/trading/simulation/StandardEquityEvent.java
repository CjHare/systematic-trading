package com.systematic.trading.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class StandardEquityEvent {

	private final BigDecimal startingEquityBalance;
	private final BigDecimal endEquityBalance;
	private final BigDecimal transactionValue;
	private final BigDecimal equityAmount;
	private final LocalDate transactionDate;

	public StandardEquityEvent(final BigDecimal equityAmount, final BigDecimal startingEquityBalance,
	        final BigDecimal endEquityBalance, final LocalDate transactionDate, final BigDecimal transactionValue) {
		this.startingEquityBalance = startingEquityBalance;
		this.endEquityBalance = endEquityBalance;
		this.transactionValue = transactionValue;
		this.transactionDate = transactionDate;
		this.equityAmount = equityAmount;
	}

	public BigDecimal getStartingEquityBalance() {
		return startingEquityBalance;
	}

	public BigDecimal getEndEquityBalance() {
		return endEquityBalance;
	}

	public BigDecimal getTransactionValue() {
		return transactionValue;
	}

	public BigDecimal getEquityAmount() {
		return equityAmount;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}
}
