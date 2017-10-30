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
package com.systematic.trading.backtest.configuration.strategy;

import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmationConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.ConfirmedByEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.IndicatorEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.PeriodicEntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;

/**
 * Factory methods for creating the strategy configuration that mirrors the ExpressionLanguage.
 * 
 * @author CJ Hare
 */
public class StrategyConfigurationFactory {

	//TODO provide factory methods for the sizing too

	public StrategyConfiguration strategy( final EntryConfiguration entry,
	        final EntrySizeConfiguration entryPositionSizing, final ExitConfiguration exit,
	        final ExitSizeConfiguration exitPositionSizing ) {
		return new StrategyConfiguration(entry, entryPositionSizing, exit, exitPositionSizing);
	}

	public EntryConfiguration entry( final EntryConfiguration leftEntry, final OperatorConfiguration.Selection op,
	        final EntryConfiguration righEntry ) {
		// TODO Auto-generated method stub
		return null;
	}

	public EntryConfiguration entry( final EntryConfiguration anchor, final ConfirmationConfiguration.Type confirmBy,
	        final EntryConfiguration confirmation ) {
		return new ConfirmedByEntryConfiguration(anchor, confirmBy, confirmation);
	}

	public EntryConfiguration entry( final IndicatorConfiguration indicator ) {
		return new IndicatorEntryConfiguration(indicator);
	}

	public EntryConfiguration entry( final PeriodicConfiguration frequency ) {
		return new PeriodicEntryConfiguration(frequency);
	}

	public ExitConfiguration exit() {
		return new ExitConfiguration();
	}
}