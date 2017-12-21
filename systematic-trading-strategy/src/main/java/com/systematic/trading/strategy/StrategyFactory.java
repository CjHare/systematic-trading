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
package com.systematic.trading.strategy;

import com.systematic.trading.model.equity.EquityClass;
import com.systematic.trading.strategy.confirmation.Confirmation;
import com.systematic.trading.strategy.entry.Entry;
import com.systematic.trading.strategy.entry.size.EntrySize;
import com.systematic.trading.strategy.exit.Exit;
import com.systematic.trading.strategy.exit.size.ExitSize;
import com.systematic.trading.strategy.indicator.Indicator;
import com.systematic.trading.strategy.operator.Operator;
import com.systematic.trading.strategy.periodic.Periodic;

/**
 * Abstract Factory, providing these regular expression features:
 * 
 * Strategy := (StrategyEntry) (EntrySize) (Exit) (ExitSize)
 * 
 * Entry := (Entry) (Operator) (Entry) (indicator) (Confirmation) (Indicator) (indicator) (Periodic)
 * 
 * Exit := (Never)
 * 
 * Indicator := ATR EMA MACD SMA RSI
 * 
 * Operator := OR AND
 * 
 * Position sizing determines the value of the order to place.
 * 
 * (Never) is syntactic sugar, as it provide no function it is absent from implementation.
 * 
 * @author CJ Hare
 */
public interface StrategyFactory {

	Strategy strategy( Entry entry, EntrySize entryPositionSizing, Exit exit, ExitSize exitPositionSizing,
	        EquityClass type, int scale );

	Entry entry( Entry leftEntry, Operator op, Entry righEntry );

	Entry entry( Entry anchor, Confirmation confirmBy, Entry confirmation );

	Entry entry( Indicator indicator );

	Entry entry( Periodic periodic );

	Exit exit();

	Operator operator( Operator.Selection operator );
}