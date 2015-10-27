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
package com.systematic.trading.backtest.analysis.statistics;

import java.math.BigDecimal;

import com.systematic.trading.backtest.event.CashEvent;

/**
 * Statistics over the occurring cash events.
 * 
 * @author CJ Hare
 */
public interface CashEventStatistics {

	/**
	 * Cash event that merits the attention of the statistical recording.
	 * 
	 * @param event details of the cash event to record.
	 */
	void event( CashEvent event );

	/**
	 * Retrieves the sum of the deposit events.
	 * 
	 * @return total of the deposit events currently received.
	 */
	BigDecimal getAmountDeposited();

	/**
	 * Retrieves the sum of the interest events.
	 * 
	 * @return total of the interest events currently received.
	 */
	BigDecimal getInterestEarned();

	/**
	 * Number of credit transactions recorded.
	 * 
	 * @return number of cash credit actions carried out.
	 */
	int getCreditEventCount();

	/**
	 * Number of debit transactions recorded.
	 * 
	 * @return number of cash bedbit actions carried out.
	 */
	int getDebitEventCount();

	/**
	 * Number of deposit transactions recorded.
	 * 
	 * @return number of cash desposit actions carried out.
	 */
	int getDepositEventCount();

	/**
	 * Number of interest transactions recorded.
	 * 
	 * @return number of interest credit actions carried out.
	 */
	int getInterestEventCount();
}
