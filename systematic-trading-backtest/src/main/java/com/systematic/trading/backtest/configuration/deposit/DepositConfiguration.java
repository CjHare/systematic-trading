/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.configuration.deposit;

import java.math.BigDecimal;
import java.time.Period;

/**
 * Amount of deposits an their frequency.
 * 
 * @author CJ Hare
 */
public enum DepositConfiguration {

	NONE(null, null),
	WEEKLY_150(BigDecimal.valueOf(150), Period.ofWeeks(1)),
	WEEKLY_200(BigDecimal.valueOf(200), Period.ofWeeks(1)),
	WEEKLY_250(BigDecimal.valueOf(250), Period.ofWeeks(1)),
	WEEKLY_2000(BigDecimal.valueOf(2000), Period.ofWeeks(1));

	private final BigDecimal amount;
	private final transient Period frequency;

	DepositConfiguration( final BigDecimal amount, final Period frequency ) {
		this.frequency = frequency;
		this.amount = amount;
	}

	public BigDecimal aAmount() {
		return amount;
	}

	public Period frequency() {
		return frequency;
	}
}