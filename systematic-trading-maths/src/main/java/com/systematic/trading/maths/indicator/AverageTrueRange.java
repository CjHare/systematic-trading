/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.maths.indicator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.exception.TooFewDataPoints;

/**
 * Developed by J. Welles Wilder, the Average True Range (ATR) is an indicator that measures
 * volatility.
 * <p/>
 * A volatility formula based only on the high-low range would fail to capture volatility from gap
 * or limit moves. ATR captures this �missing� volatility, where it is important to remember that
 * ATR does not provide an indication of price direction, merely volatility.
 * <p/>
 * Wilder started with a concept called True Range (TR), which is defined as the greatest of the
 * following:
 * <ul>
 * <li>Method 1: Current High less the current Low</li>
 * <li>Method 2: Current High less the previous Close (absolute value)</li>
 * <li>Method 3: Current Low less the previous Close (absolute value)</li>
 * </ul>
 * If the current period's high is above the prior period's high and the low is below the prior
 * period's low, then the current period's high-low range will be used as the True Range. This is an
 * outside day that would use Method 1 to calculate the TR. This is pretty straight forward. Methods
 * 2 and 3 are used when there is a gap or an inside day. A gap occurs when the previous close is
 * greater than the current high (signaling a potential gap down or limit move) or the previous
 * close is lower than the current low (signaling a potential gap up or limit move)
 * <p/>
 * The average true range is simple the averaged TR.
 * 
 * @author CJ Hare
 */
public class AverageTrueRange {

    /** Number of decimal places for scaling. */
    private static final int ROUNDING_SCALE = 2;

    /** The number of trading days to look back for calculation. */
    private final int lookback;

    private final BigDecimal priorMultiplier;
    private final BigDecimal lookbackDivider;

    /**
     * @param lookback the number of days to use when calculating the ATR, also the number of days
     *            prior to the averaging becoming correct.
     */
    public AverageTrueRange(final int lookback) {
        this.lookback = lookback;
        this.priorMultiplier = BigDecimal.valueOf(lookback - 1);
        this.lookbackDivider = BigDecimal.valueOf(lookback);
    }

    private BigDecimal trueRangeMethodOne(final TradingDayPrices today) {
        return today.getHighestPrice().subtract(today.getLowestPrice());
    }

    private BigDecimal trueRangeMethodTwo(final TradingDayPrices today, final TradingDayPrices yesterday) {
        return today.getHighestPrice().subtract(yesterday.getClosingPrice());
    }

    private BigDecimal trueRangeMethodThree(final TradingDayPrices today, final TradingDayPrices yesterday) {
        return today.getLowestPrice().subtract(yesterday.getClosingPrice());
    }

    /**
     * @return highest value of the three true range methods.
     */
    private BigDecimal getTrueRange(final TradingDayPrices today, final TradingDayPrices yesterday) {
        final BigDecimal one = trueRangeMethodOne(today);
        final BigDecimal two = trueRangeMethodTwo(today, yesterday);
        final BigDecimal three = trueRangeMethodThree(today, yesterday);

        if (one.compareTo(two) >= 0 && one.compareTo(three) >= 0) {
            return one;
        }

        if (two.compareTo(three) >= 0) {
            return two;
        }

        return three;
    }

    private BigDecimal average(final BigDecimal currentTrueRange, final BigDecimal priorAverageTrueRange) {
        /* For a look back of 14: Current ATR = [(Prior ATR x 13) + Current TR] / 14 - Multiply the
         * previous 14-day ATR by 13. - Add the most recent day's TR value. - Divide the total by 14 */
        return priorAverageTrueRange.multiply(priorMultiplier).add(currentTrueRange)
                .divide(lookbackDivider, ROUNDING_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @param closePrices ordered chronologically, from oldest to youngest (most recent first).
     * @throws TooFewDataPoints not enough closing prices to perform EMA calculations.
     */
    public BigDecimal[] atr(final TradingDayPrices[] data) throws TooFewDataPoints {

        // Need at least one RSI value
        if (data.length < lookback + 1) {
            throw new TooFewDataPoints(String.format("At least %s data points are needed, only %s given", lookback + 1,
                    data.length));
        }

        // Skip any null entries
        int startAtrIndex = 0;
        while (data[startAtrIndex] == null) {
            startAtrIndex++;
        }

        // For the first value just use the TR
        final BigDecimal[] atr = new BigDecimal[data.length];

        atr[startAtrIndex] = trueRangeMethodOne(data[startAtrIndex]);

        // Starting atr is just the first value
        BigDecimal priorAtr = atr[startAtrIndex];

        for (int i = startAtrIndex + 1; i < atr.length; i++) {
            atr[i] = average(getTrueRange(data[i], data[i - 1]), priorAtr);
            priorAtr = atr[i];
        }

        return atr;
    }
}
