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
package com.systematic.trading.backtest.configuration.strategy.indicator;

import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;

/**
 * @author CJ Hare
 */
public enum SmaUptrendConfiguration implements IndicatorConfiguration {

	SHORT(20, 5, "SMA-Uptrend-Short"),
	MEDIUM(50, 5, "SMA-Uptrend-Medium"),
	LONG(100, 5, "SMA-Uptrend-Long");

	private final int lookback;
	private final int daysOfGradient;
	private final String description;

	SmaUptrendConfiguration( final int lookback, final int daysOfGradient, final String description ) {
		this.daysOfGradient = daysOfGradient;
		this.lookback = lookback;
		this.description = description;
	}

	public int getLookback() {
		return lookback;
	}

	public int getDaysOfGradient() {
		return daysOfGradient;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public IndicatorId getId() {
		return new IndicatorId(description);
	}
}