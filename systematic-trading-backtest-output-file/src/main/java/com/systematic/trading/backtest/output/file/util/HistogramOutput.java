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
package com.systematic.trading.backtest.output.file.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Summarises a set of events into a ASCII histogram.
 * 
 * @author CJ Hare
 */
public class HistogramOutput {

	private static final BigDecimal TEN = BigDecimal.valueOf(10);
	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	public void addHistogram( final Map<BigDecimal, BigInteger> events, final StringBuilder output ) {

		BigDecimal smallestKey = BigDecimal.valueOf(Double.MAX_VALUE);
		BigDecimal largestKey = BigDecimal.ZERO;
		for (final Map.Entry<BigDecimal, BigInteger> equities : events.entrySet()) {
			final BigDecimal frequency = equities.getKey();
			if (smallestKey.compareTo(frequency) > 0) {
				smallestKey = frequency;
			}
			if (largestKey.compareTo(frequency) < 0) {
				largestKey = frequency;
			}
		}

		final Map<String, BigInteger> binnedBuyEvents = new TreeMap<>();
		for (final Map.Entry<BigDecimal, BigInteger> equities : events.entrySet()) {
			final BigDecimal frequency = equities.getKey();
			final String bin = bin(smallestKey, largestKey, frequency);
			final BigInteger count = events.get(frequency);
			binnedBuyEvents.put(bin, binnedBuyEvents.get(bin) == null ? count : binnedBuyEvents.get(bin).add(count));
		}

		for (final Map.Entry<String, BigInteger> bin : binnedBuyEvents.entrySet()) {
			output.append(String.format("%s : %s%n", bin.getKey(), bin.getValue()));
		}
	}

	private String bin( final BigDecimal smallest, final BigDecimal largest, final BigDecimal value ) {

		// bins covering the whole range of values
		final BigDecimal binSize = largest.subtract(smallest).divide(TEN);

		long bin = 1;
		while (binSize.multiply(BigDecimal.valueOf(bin)).add(smallest).compareTo(value) <= 0) {
			bin++;
		}

		return String.format("%s to %s",
		        TWO_DECIMAL_PLACES.format(binSize.multiply(BigDecimal.valueOf(bin - 1)).add(smallest)),
		        TWO_DECIMAL_PLACES.format(binSize.multiply(BigDecimal.valueOf(bin)).add(smallest)));
	}
}