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
package com.systematic.trading.backtest.configuration.strategy;

import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.description.DescriptionGenerator;

/**
 * Strategy configuration.
 * 
 * @author CJ Hare
 */
public class StrategyConfiguration implements Describable {

	private final EntryConfiguration entry;
	private final EntrySizeConfiguration entryPositionSizing;
	private final ExitConfiguration exit;
	private final ExitSizeConfiguration exitPositionSizing;

	public StrategyConfiguration( final EntryConfiguration entry, final EntrySizeConfiguration entryPositionSizing,
	        final ExitConfiguration exit, final ExitSizeConfiguration exitPositionSizing ) {

		this.entry = entry;
		this.exit = exit;
		this.entryPositionSizing = entryPositionSizing;
		this.exitPositionSizing = exitPositionSizing;
	}

	public EntryConfiguration entry() {

		return entry;
	}

	public EntrySizeConfiguration entryPositionSizing() {

		return entryPositionSizing;
	}

	public ExitConfiguration exit() {

		return exit;
	}

	public ExitSizeConfiguration exitPositionSizing() {

		return exitPositionSizing;
	}

	@Override
	public String description( final DescriptionGenerator description ) {

		return description.strategy(entry, entryPositionSizing, exit, exitPositionSizing);
	}
}