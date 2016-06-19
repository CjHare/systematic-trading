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
package com.systematic.trading.simulation.analysis.statistics;

import java.math.BigDecimal;

import com.systematic.trading.simulation.cash.event.CashEvent;

/**
 * Cumulative recording of the cash events for statistical purposes.
 * 
 * @author CJ Hare
 */
public class CumulativeCashEventStatistics implements CashEventStatistics {

	private BigDecimal amountDeposited = BigDecimal.ZERO;
	private BigDecimal interestEarned = BigDecimal.ZERO;
	private int creditEventCount = 0;
	private int debitEventCount = 0;
	private int depositEventCount = 0;
	private int interestEventCount = 0;

	@Override
	public void event( final CashEvent event ) {

		switch (event.getType()) {
			case CREDIT:
				creditEventCount++;
			break;
			case DEBIT:
				debitEventCount++;
			break;
			case DEPOSIT:
				depositEventCount++;
				amountDeposited = amountDeposited.add(event.getAmount());
			break;
			case INTEREST:
				interestEventCount++;
				interestEarned = interestEarned.add(event.getAmount());
			break;
			default:
				throw new IllegalArgumentException(String.format("Cash event type %s is unexpected", event.getType()));
		}
	}

	@Override
	public BigDecimal getAmountDeposited() {
		return amountDeposited;
	}

	@Override
	public BigDecimal getInterestEarned() {
		return interestEarned;
	}

	@Override
	public int getCreditEventCount() {
		return creditEventCount;
	}

	@Override
	public int getDebitEventCount() {
		return debitEventCount;
	}

	@Override
	public int getDepositEventCount() {
		return depositEventCount;
	}

	@Override
	public int getInterestEventCount() {
		return interestEventCount;
	}
}
