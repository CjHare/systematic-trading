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
package com.systematic.trading.signals.generator.sma;

import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageIndicator;
import com.systematic.trading.maths.indicator.sma.SimpleMovingAverageLine;
import com.systematic.trading.signals.generator.GenericSignalsTestBase;

/**
 * Verify the SimpleMovingAverageGradientSignals interacts correctly with it's aggregated components.
 * 
 * @author CJ Hare
 */
public class SimpleMovingAverageGradientSignalsTest
        extends GenericSignalsTestBase<SimpleMovingAverageLine, SimpleMovingAverageIndicator> {

	/** Number of days needed to correctly calculate the first RSI value.*/
	private static final int LOOKBACK = 26;

	/** Number of days needed to correctly calculate the first RSI value.*/
	private static final int DAYS_OF_GRADIENT = 5;

	/** Minimum number of useful days for RSI evaluation, */
	private static final int REQUIRED_TRADING_DAYS = DAYS_OF_GRADIENT + LOOKBACK;

	@Override
	protected int requiredNumberOfTradingDays() {
		return REQUIRED_TRADING_DAYS;
	}
}