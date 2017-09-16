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
package com.systematic.trading.signals;

import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signal.event.SignalAnalysisListener;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.filter.SignalFilter;

/**
 * Analysis of buy signals.
 * 
 * @author CJ Hare
 */
public interface AnalysisBuySignals {

	/**
	 * Given a set of trading data, parses with indicators to generate buy signals.
	 * 
	 * @param data trading day data.
	 * @return any signals generated over the given data.
	 */
	List<BuySignal> analyse( TradingDayPrices[] data );

	/**
	 * The maximum number of trading days data used by the signal analysers.
	 * 
	 * @return maximum number of data to provide to the analysis.
	 */
	int getMaximumNumberOfTradingDaysRequired();

	/**
	 * Adds an interested party to those notified for signal analysis events.
	 * 
	 * @param listener will receive signal events.
	 */
	void addListener( SignalAnalysisListener listener );

	/**
	 * Filters used in the analysis of buy signals.
	 * 
	 * @return all the filters used during analysis.
	 */
	List<SignalFilter> getFilters();
}