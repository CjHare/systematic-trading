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
package com.systematic.trading.backtest.configuration;

import java.math.MathContext;
import java.time.LocalDate;

import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.SingleEquityClassBroker;
import com.systematic.trading.simulation.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.logic.HoldForeverExitLogic;

/**
 * Configuration for signal triggered entry logic, with weekly contribution to cash account.
 * <p/>
 * <ul>
 * <li>Exit logic: never sell</li>
 * <li>Cash account: zero starting, weekly 100 dollar deposit</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public class HoldForeverWeeklyDespositConfiguration extends DefaultConfiguration
		implements BacktestBootstrapConfiguration {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Description used for uniquely identifying the configuration. */
	private final String description;

	/** Fees applied to each equity transaction. */
	private final BrokerageFeeStructure tradingFeeStructure;

	/** Cash account to use during the simulation. */
	private final CashAccount cashAccount;

	/** Decision maker for when to enter a trade. */
	private final EntryLogic entryLogic;

	public HoldForeverWeeklyDespositConfiguration( final LocalDate startDate, final LocalDate endDate,
			final String description, final BrokerageFeeStructure tradingFeeStructure, final CashAccount cashAccount,
			final EntryLogic entryLogic, final MathContext mathContext ) {

		// TODO use a single equity configuration, no abstract parent
		super( startDate, endDate );

		this.tradingFeeStructure = tradingFeeStructure;
		this.description = description;
		this.cashAccount = cashAccount;
		this.mathContext = mathContext;
		this.entryLogic = entryLogic;
	}

	@Override
	public ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}

	@Override
	public Brokerage getBroker( final EquityIdentity equity ) {
		return new SingleEquityClassBroker( tradingFeeStructure, equity.getType(), mathContext );
	}

	@Override
	public CashAccount getCashAccount( final LocalDate openingDate ) {
		return cashAccount;
	}

	@Override
	public EntryLogic getEntryLogic( final EquityIdentity equity, final LocalDate openingDate ) {
		return entryLogic;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
