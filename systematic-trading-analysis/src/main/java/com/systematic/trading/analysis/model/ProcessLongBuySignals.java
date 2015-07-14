/**
 * Copyright (c) 2015, CJ Hare
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
package com.systematic.trading.signals.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.signals.Equity;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.SignalFilter;

public class ProcessLongBuySignals {

	private static final Logger LOG = LogManager.getLogger( ProcessLongBuySignals.class );

	private final LongBuySignalConfiguration configuration;

	private final List<SignalFilter> filters;

	public ProcessLongBuySignals( final LongBuySignalConfiguration configuration, final List<SignalFilter> filters ) {
		this.configuration = configuration;
		this.filters = filters;
	}

	public List<BuySignal> process( final Equity equity, final DataPoint[] data ) {

		final AnalysisLongBuySignals buyLong = new AnalysisLongBuySignals( configuration, filters );

		try {
			return buyLong.analyse( data );
		} catch (final TooFewDataPoints e) {
			LOG.error( String.format( "Too few data points for: %s", equity.getSymbol() ), e );
		}

		return new ArrayList<BuySignal>( 0 );
	}
}
