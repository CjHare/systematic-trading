/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package com.systematic.trading.backtest.configuration.strategy.indicator;

import java.math.BigDecimal;

import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;

/**
 * @author CJ Hare
 */
public enum RsiConfiguration implements IndicatorConfiguration {

	SHORT(7, BigDecimal.valueOf(30), BigDecimal.valueOf(70), "RSI-Short"),
	MEDIUM(14, BigDecimal.valueOf(30), BigDecimal.valueOf(70), "RSI-Medium"),
	LONG(21, BigDecimal.valueOf(30), BigDecimal.valueOf(70), "RSI-Long");

	private final int lookback;
	private final BigDecimal oversold;
	private final BigDecimal overbought;
	private final String description;

	RsiConfiguration( final int lookback, final BigDecimal overbought, final BigDecimal oversold,
	        final String description ) {
		this.lookback = lookback;
		this.oversold = oversold;
		this.overbought = overbought;
		this.description = description;
	}

	public int lookback() {

		return lookback;
	}

	public BigDecimal oversold() {

		return oversold;
	}

	public BigDecimal overbought() {

		return overbought;
	}

	public String description() {

		return description;
	}

	@Override
	public IndicatorId id() {

		return new IndicatorId(description);
	}
}