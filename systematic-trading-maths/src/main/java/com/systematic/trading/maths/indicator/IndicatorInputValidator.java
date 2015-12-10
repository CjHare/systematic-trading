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

import com.systematic.trading.data.TradingDayPrices;

/**
 * Validates the input given to an indicator.
 * 
 * @author CJ Hare
 */
public class IndicatorInputValidator {

	// TODO abstract & condense logic, lots of shared

	// TODO test

	public int getLastNonNullIndex( final TradingDayPrices[] data ) {
		int lastNonNullItem = data.length - 1;

		while (isNullEntryWithinArray( data, lastNonNullItem )) {
			lastNonNullItem--;
		}

		return lastNonNullItem;
	}

	public int getLastNonNullIndex( final BigDecimal[] data ) {
		int lastNonNullItem = data.length - 1;

		while (isNullEntryWithinArray( data, lastNonNullItem )) {
			lastNonNullItem--;
		}

		return lastNonNullItem;
	}

	/**
	 * Retrieves the first non-null entry in the given data array, with the additional constraint
	 * the index must be within the store size.
	 * 
	 * @param data array to find the first non-null index within.
	 * @param storeSize length of the storage array.
	 * @param minimumNumberOfPrices the minimum number of consecutive items required in the data
	 *            set.
	 * @return maximumIndex of the first non-null entry.
	 * @throws IllegalArgumentException when the there are no non-null items or the index exceeds
	 *             the store size.
	 */
	public int getFirstNonNullIndex( final TradingDayPrices[] data, final int maximumIndex,
			final int minimumNumberOfPrices ) {

		// Expecting the same number of input data points as outputs
		if (data.length > maximumIndex) {
			throw new IllegalArgumentException(
					String.format( "The number of data points given: %s exceeds the size of the store: %s", data.length,
							maximumIndex ) );
		}

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		if (firstNonNullItem >= maximumIndex) {
			throw new IllegalArgumentException( String.format(
					"The index of the first non-null item index of: %s exceeds the maximum allowed index of: %s",
					firstNonNullItem, maximumIndex ) );
		}

		// Find last non-null index
		final int lastNonNullItem = getLastNonNullIndex( data );

		// Number of items, accounting for zero indexed array
		final int numberOfItems = lastNonNullItem - firstNonNullItem + 1;

		// Enough data to calculate indicator?
		if (numberOfItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s non null data points are needed, only %s given", minimumNumberOfPrices,
							numberOfItems ) );
		}

		// Are the items consecutively populated (as expected)
		int numberOfConsecutiveItems = firstNonNullItem;
		while (numberOfConsecutiveItems < lastNonNullItem && !isNullEntry( data, numberOfConsecutiveItems )) {
			numberOfConsecutiveItems++;
		}

		// Account for zero index of array
		numberOfConsecutiveItems++;

		if (numberOfConsecutiveItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s consecutive non null data points are needed, only %s given",
							minimumNumberOfPrices, numberOfConsecutiveItems ) );
		}

		return firstNonNullItem;
	}

	/**
	 * Retrieves the first non-null entry in the given data array, with the additional constraint
	 * the index must be within the store size.
	 * 
	 * @param data array to find the first non-null index within.
	 * @param storeSize length of the storage array.
	 * @param minimumNumberOfPrices the minimum number of consecutive items required in the data
	 *            set.
	 * @return maximumIndex of the first non-null entry.
	 * @throws IllegalArgumentException when the there are no non-null items or the index exceeds
	 *             the store size.
	 */
	public int getFirstNonNullIndex( final BigDecimal[] data, final int maximumIndex,
			final int minimumNumberOfPrices ) {

		// Expecting the same number of input data points as outputs
		if (data.length > maximumIndex) {
			throw new IllegalArgumentException(
					String.format( "The number of data points given: %s exceeds the size of the store: %s", data.length,
							maximumIndex ) );
		}

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		if (firstNonNullItem >= maximumIndex) {
			throw new IllegalArgumentException( String.format(
					"The index of the first non-null item index of: %s exceeds the maximum allowed index of: %s",
					firstNonNullItem, maximumIndex ) );
		}

		// Find last non-null index
		final int lastNonNullItem = getLastNonNullIndex( data );

		// Number of items, accounting for zero indexed array
		final int numberOfItems = lastNonNullItem - firstNonNullItem + 1;

		// Enough data to calculate indicator?
		if (numberOfItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s non null data points are needed, only %s given", minimumNumberOfPrices,
							numberOfItems ) );
		}

		// Are the items consecutively populated (as expected)
		int numberOfConsecutiveItems = firstNonNullItem;
		while (numberOfConsecutiveItems < lastNonNullItem && !isNullEntry( data, numberOfConsecutiveItems )) {
			numberOfConsecutiveItems++;
		}

		// Account for zero index of array
		numberOfConsecutiveItems++;

		if (numberOfConsecutiveItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s consecutive non null data points are needed, only %s given",
							minimumNumberOfPrices, numberOfConsecutiveItems ) );
		}

		return firstNonNullItem;
	}

	private boolean isNullEntryWithinArray( final BigDecimal[] data, final int index ) {
		return isWithinArray( data, index ) && isNullEntry( data, index );
	}

	private boolean isNullEntryWithinArray( final TradingDayPrices[] data, final int index ) {
		return isWithinArray( data, index ) && isNullEntry( data, index );
	}

	private boolean isNullEntry( final TradingDayPrices[] data, final int index ) {
		return (data[index] == null || data[index].getClosingPrice() == null);
	}

	private boolean isNullEntry( final BigDecimal[] data, final int index ) {
		return data[index] == null;
	}

	private boolean isWithinArray( final Object[] data, final int index ) {
		return (index >= 0) && (index < data.length);
	}
}
