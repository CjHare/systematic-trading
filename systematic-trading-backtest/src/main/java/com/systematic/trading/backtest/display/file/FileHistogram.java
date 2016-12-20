package com.systematic.trading.backtest.display.file;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class FileHistogram {

	private static final BigDecimal TEN = BigDecimal.valueOf(10);
	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	public void addHistogram( final Map<BigDecimal, BigInteger> events, final StringBuilder output ) {

		BigDecimal smallestKey = BigDecimal.valueOf(Double.MAX_VALUE);
		BigDecimal largestKey = BigDecimal.ZERO;
		for (final BigDecimal equities : events.keySet()) {
			if (smallestKey.compareTo(equities) > 0) {
				smallestKey = equities;
			}
			if (largestKey.compareTo(equities) < 0) {
				largestKey = equities;
			}
		}

		final Map<String, BigInteger> binnedBuyEvents = new TreeMap<>();
		for (final BigDecimal equities : events.keySet()) {
			final String bin = getBin(smallestKey, largestKey, equities);
			final BigInteger count = events.get(equities);
			binnedBuyEvents.put(bin, binnedBuyEvents.get(bin) == null ? count : binnedBuyEvents.get(bin).add(count));
		}

		for (final String bin : binnedBuyEvents.keySet()) {
			output.append(String.format("%s : %s%n", bin, binnedBuyEvents.get(bin)));
		}
	}

	private String getBin( final BigDecimal smallest, final BigDecimal largest, final BigDecimal value ) {

		// bins covering the whole range of values
		final BigDecimal binSize = largest.subtract(smallest).divide(TEN);

		int bin = 1;
		while (binSize.multiply(BigDecimal.valueOf(bin)).add(smallest).compareTo(value) <= 0) {
			bin++;
		}

		return String.format("%s to %s",
		        TWO_DECIMAL_PLACES.format(binSize.multiply(BigDecimal.valueOf(bin - 1)).add(smallest)),
		        TWO_DECIMAL_PLACES.format(binSize.multiply(BigDecimal.valueOf(bin)).add(smallest)));
	}
}
