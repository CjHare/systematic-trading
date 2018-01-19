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
package com.systematic.trading.maths.indicator.atr;

import com.systematic.trading.maths.indicator.SignalCalculator;

/**
 * Developed by J. Welles Wilder, the Average True Range (ATR) is an indicator that measures
 * volatility.
 * 
 * A volatility formula based only on the high-low range would fail to capture volatility from gap
 * or limit moves. ATR captures this �missing� volatility, where it is important to remember that
 * ATR does not provide an indication of price direction, merely volatility.
 * 
 * Wilder started with a concept called True Range (TR), which is defined as the greatest of the
 * following:
 * <ul>
 * <li>Method 1: Current High less the current Low</li>
 * <li>Method 2: Current High less the previous Close (absolute value)</li>
 * <li>Method 3: Current Low less the previous Close (absolute value)</li>
 * </ul>
 * 
 * If the current period's high is above the prior period's high and the low is below the prior
 * period's low, then the current period's high-low range will be used as the True Range. This is an
 * outside day that would use Method 1 to calculate the TR. This is pretty straight forward. Methods
 * 2 and 3 are used when there is a gap or an inside day. A gap occurs when the previous close is
 * greater than the current high (signal for a potential gap down or limit move) or the previous
 * close is lower than the current low (signal for a potential gap up or limit move)
 * 
 * The average true range is simply the averaged TR.
 * 
 * @author CJ Hare
 */
public interface AverageTrueRangeIndicator extends SignalCalculator<AverageTrueRangeLine> {}