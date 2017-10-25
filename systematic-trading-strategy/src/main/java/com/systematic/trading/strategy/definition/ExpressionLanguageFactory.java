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
package com.systematic.trading.strategy.definition;

import com.systematic.trading.strategy.TradingStrategy;
import com.systematic.trading.strategy.entry.TradingStrategyConfirmationEntry;
import com.systematic.trading.strategy.entry.TradingStrategyEntry;
import com.systematic.trading.strategy.entry.TradingStrategyIndicatorEntry;
import com.systematic.trading.strategy.entry.TradingStrategyPeriodicEntry;
import com.systematic.trading.strategy.exit.TradingStrategyExit;
import com.systematic.trading.strategy.indicator.IndicatorConfiguration;
import com.systematic.trading.strategy.indicator.TradingStrategyIndicator;
import com.systematic.trading.strategy.operator.TradingStrategyAndOperator;
import com.systematic.trading.strategy.operator.TradingStrategyOrOperator;

/**
 * Implementation of a TradingStrategyExpressionLanguage using a facade to aggregate specialist factories.
 * 
 * @author CJ Hare
 */
public class ExpressionLanguageFactory implements ExpressionLanguage {

	@Override
	public Strategy strategy( final Entry entry, final EntrySize entryPositionSizing, final Exit exit,
	        final ExitSize exitPositionSizing ) {
		return new TradingStrategy(entry, entryPositionSizing, exit, exitPositionSizing);
	}

	@Override
	public Entry entry( final Entry leftEntry, final Operator operator, final Entry righEntry ) {
		return new TradingStrategyEntry(leftEntry, operator, righEntry);
	}

	@Override
	public Entry entry( final Indicator anchor, final Confirmation confirmBy, final Indicator confirmation ) {
		return new TradingStrategyConfirmationEntry(anchor, confirmBy, confirmation);
	}

	@Override
	public Entry entry( final Indicator indicator ) {
		return new TradingStrategyIndicatorEntry(indicator);
	}

	@Override
	public Entry entry( final Periodic periodic ) {
		return new TradingStrategyPeriodicEntry(periodic);
	}

	@Override
	public Exit exit() {
		return new TradingStrategyExit();
	}

	@Override
	public Indicator indicator( final IndicatorConfiguration indicator ) {
		return new TradingStrategyIndicator(indicator);
	}

	@Override
	public Operator operator( final Operator.Selection operator ) {

		switch (operator) {
			case OR:
				return new TradingStrategyOrOperator();

			default:
			case AND:
				return new TradingStrategyAndOperator();
		}
	}
}