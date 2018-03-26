/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.signal.generator;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import com.systematic.trading.model.signal.SignalType;
import com.systematic.trading.signal.model.DatedSignal;

/**
 * Calculator for bullish or bearish signals expecting input data in the form of <T>.
 * 
 * @author CJ Hare
 */
public interface SignalGenerator<T> {

	/**
	 * Generates any appropriate signals from the indicator output.
	 * 
	 * @param indicatorOutput
	 *            indicator appropriate output from the trading price data.
	 * @param signalRange
	 *            boolean test for whether a date is within the acceptable range for a signal.
	 * @return any signals generated by the calculator.
	 */
	List<DatedSignal> generate( T indicatorOutput, Predicate<LocalDate> signalRange );

	/**
	 * Retrieves the type of signals created.
	 * 
	 * @return type of signal created by the calculator.
	 */
	SignalType type();
}
