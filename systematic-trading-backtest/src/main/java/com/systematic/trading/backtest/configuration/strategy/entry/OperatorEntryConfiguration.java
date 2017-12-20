/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest.configuration.strategy.entry;

import java.time.Period;
import java.time.temporal.TemporalAmount;

import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.description.DescriptionGenerator;

/**
 * @author CJ Hare
 */
public class OperatorEntryConfiguration implements EntryConfiguration {

	private final EntryConfiguration leftEntry;
	private final OperatorConfiguration.Selection op;
	private final EntryConfiguration righEntry;

	public OperatorEntryConfiguration( final EntryConfiguration leftEntry, final OperatorConfiguration.Selection op,
	        final EntryConfiguration righEntry ) {
		this.leftEntry = leftEntry;
		this.op = op;
		this.righEntry = righEntry;
	}

	public EntryConfiguration leftEntry() {

		return leftEntry;
	}

	public OperatorConfiguration.Selection operator() {

		return op;
	}

	public EntryConfiguration righEntry() {

		return righEntry;
	}

	@Override
	public String description( final DescriptionGenerator description ) {

		return description.entry(leftEntry, op, righEntry);
	}

	@Override
	public boolean hasSubEntry() {

		return true;
	}

	@Override
	public TemporalAmount priceDataRange() {

		return Period.ofDays(1);
	}
}