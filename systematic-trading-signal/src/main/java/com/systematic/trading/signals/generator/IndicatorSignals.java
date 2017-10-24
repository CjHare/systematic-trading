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
package com.systematic.trading.signals.generator;

import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * Responsible for generation of signals from analysis of the trading data.
 * 
 * @author CJ Hare
 */
public interface IndicatorSignals {

	/**
	 * The maximum number of trading days data used by the signal analysers.
	 * 
	 * @return maximum number of data to provide to the analysis.
	 */
	int getRequiredNumberOfTradingDays();

	/**
	 * Perform the analysis of trading prices for the generation of signals.
	 * 
	 * @param data trading prices for calculation of signals.
	 * @return signals generated from the given trading data, empty list means zero, never
	 *         <code>null</code>.
	 */
	List<IndicatorSignal> calculate( TradingDayPrices[] data );
	//TODO return a sortedMap?
	
	/**
	 * The type of signals that are generated.
	 * 
	 * @return the unique name of indicator signals generated.
	 */
	IndicatorId getSignalId();
}