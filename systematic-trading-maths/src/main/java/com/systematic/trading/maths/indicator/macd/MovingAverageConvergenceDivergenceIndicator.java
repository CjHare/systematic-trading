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
package com.systematic.trading.maths.indicator.macd;

import com.systematic.trading.maths.indicator.SignalCalculator;

/**
 * Moving average convergence divergence (MACD) is a trend-following momentum indicator showing the
 * relationship between two moving averages of prices.
 * 
 * Typically the MACD is calculated by subtracting the 26-day exponential moving average (EMA) from
 * the 12-day EMA. A nine-day EMA of the MACD, called the "signal line", is then plotted on top of
 * the MACD, functioning as a trigger for buy and sell signals.
 * 
 * There are three common methods used to interpret the MACD:
 * <ul>
 * <li>Crossovers - when the MACD falls below the signal line, it is a bearish signal, indicating
 * that it may be time to sell. Conversely, when the MACD rises above the signal line, the indicator
 * gives a bullish signal, suggesting upward momentum will be experienced.</li>
 * <li>Divergence - when the security price diverges from the MACD. It signals the end of the
 * current trend.</li>
 * <li>Dramatic rise - when the MACD rises dramatically; the shorter moving average pulls away from
 * the longer-term moving average, it's a signal that the security is overbought and will soon
 * return to normal levels.</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public interface MovingAverageConvergenceDivergenceIndicator
        extends SignalCalculator<MovingAverageConvergenceDivergenceLines> {}