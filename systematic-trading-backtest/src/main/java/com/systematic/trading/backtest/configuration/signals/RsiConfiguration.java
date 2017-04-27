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
package com.systematic.trading.backtest.configuration.signals;

import java.math.BigDecimal;

import com.systematic.trading.signals.model.IndicatorSignalType;

/**
 * Configuration for the MACD signal calculator.
 * 
 * @author CJ Hare
 */
public enum RsiConfiguration implements SignalConfiguration {

	SHORT(7, BigDecimal.valueOf(30), BigDecimal.valueOf(70), "ShortRSI"),
	MEDIUM(14, BigDecimal.valueOf(30), BigDecimal.valueOf(70), "MediumRSI");

	private final String description;
	private final int lookback;
	private final BigDecimal oversold;
	private final BigDecimal overbought;

	RsiConfiguration( final int lookback, final BigDecimal overbought, final BigDecimal oversold,
	        final String description ) {
		this.description = description;
		this.lookback = lookback;
		this.oversold = oversold;
		this.overbought = overbought;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public int getLookback() {
		return lookback;
	}

	public BigDecimal getOversold() {
		return oversold;
	}

	public BigDecimal getOverbought() {
		return overbought;
	}

	@Override
	public IndicatorSignalType getType() {
		return IndicatorSignalType.RSI;
	}
}