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
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;

/**
 * Validates the input given to an indicator.
 * 
 * @author CJ Hare
 */
public class IndicatorInputValidator {

	public <T> void verifyZeroNullEntries( final List<T> values ) {
		for (final T value : values) {
			if (value == null) {
				throw new IllegalArgumentException(
						String.format( "Unexpected null value found in list: %s", values ) );
			}
		}
	}

	public <T> void verifyZeroNullEntries( final T[] values ) {
		for (final T value : values) {
			if (value == null) {
				throw new IllegalArgumentException(
						String.format( "Unexpected null value found in list: %s", values ) );
			}
		}
	}

	// TODO rename classes methods

	/**
	 * The last index in the given array that contains an element.
	 * 
	 * @param data array to find the last non-null item.
	 * @return index of the last item in the array, always < data.length.
	 */
	public <T> int getLastNonNullIndex( final T[] data ) {
		// Find last non-null index
		int lastNonNullItem = data.length - 1;

		while (isNullEntryWithinArray( data, lastNonNullItem )) {
			lastNonNullItem--;
		}

		return lastNonNullItem;
	}

	/**
	 * The last index in the given array that contains an element.
	 * 
	 * @param data array to find the last non-null item.
	 * @return index of the last item in the array, always < data.length.
	 */
	public <T> int getLastNonNullIndex( final List<T> data ) {
		// Find last non-null index
		int lastNonNullItem = data.size() - 1;

		while (isNullEntryWithinArray( data, lastNonNullItem )) {
			lastNonNullItem--;
		}

		return lastNonNullItem;
	}

	/**
	 * Retrieves the first non-null entry in the given data array.
	 * 
	 * @param data array to find the first non-null index within.
	 */
	public int getFirstNonNullIndex( final TradingDayPrices[] data ) {
		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		return firstNonNullItem;
	}

	/**
	 * Retrieves the first non-null entry in the given data array.
	 * 
	 * @param data array to find the first non-null index within.
	 */
	public int getFirstNonNullIndex( final List<BigDecimal> data ) {
		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		return firstNonNullItem;
	}

	public <T> void verifyEnoughValues( final List<T> data, final int requiredNumberOfPrices ) {

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		final int lastNonNullItem = getLastNonNullIndex( data );
		final int numberOfItems = getNumberOfItems( firstNonNullItem, lastNonNullItem );

		validateNumberOfItems( numberOfItems, requiredNumberOfPrices );

		final int numberOfConsecutiveItems = getNumberOfConsectiveItems( data, firstNonNullItem, lastNonNullItem );

		validateEnoughConsecutiveItems( numberOfConsecutiveItems, requiredNumberOfPrices );
	}

	public <T> void verifyEnoughValues( final T[] data, final int requiredNumberOfPrices ) {

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		final int lastNonNullItem = getLastNonNullIndex( data );
		final int numberOfItems = getNumberOfItems( firstNonNullItem, lastNonNullItem );

		validateNumberOfItems( numberOfItems, requiredNumberOfPrices );

		final int numberOfConsecutiveItems = getNumberOfConsectiveItems( data, firstNonNullItem, lastNonNullItem );

		validateEnoughConsecutiveItems( numberOfConsecutiveItems, requiredNumberOfPrices );
	}

	/**
	 * Retrieves the starting non-null entry in the given data array, with the additional constraint
	 * the index must be within the store size.
	 * 
	 * @param data array to find the first non-null index within.
	 * @param requiredNumberOfPrices the minimum number of consecutive items required in the data
	 *            set.
	 * @return maximumIndex of the first non-null entry.
	 * @throws IllegalArgumentException when the there are no non-null items or the index exceeds
	 *             the store size.
	 */
	public int getStartingNonNullIndex( final TradingDayPrices[] data, final int requiredNumberOfPrices ) {

		// TODO too few, null at end, null at beginning

		// TODO may not need maximum index

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		validateIndexInArray( data, firstNonNullItem, requiredNumberOfPrices );
		// validateFirstItemInMaximumIndex( firstNonNullItem, maximumIndex );

		final int lastNonNullItem = getLastNonNullIndex( data );
		final int numberOfItems = getNumberOfItems( firstNonNullItem, lastNonNullItem );

		validateNumberOfItems( numberOfItems, requiredNumberOfPrices );
		// validateSizeOfStore( numberOfItems, maximumIndex );

		final int numberOfConsecutiveItems = getNumberOfConsectiveItems( data, firstNonNullItem, lastNonNullItem );

		validateEnoughConsecutiveItems( numberOfConsecutiveItems, requiredNumberOfPrices );

		return firstNonNullItem;
	}

	/**
	 * Retrieves the first non-null entry in the given data array, with the additional constraint
	 * the index must be within the store size.
	 * 
	 * @param data array to find the first non-null index within.
	 * @param minimumNumberOfPrices the minimum number of consecutive items required in the data
	 *            set.
	 * @return maximumIndex of the first non-null entry.
	 * @throws IllegalArgumentException when the there are no non-null items or the index exceeds
	 *             the store size.
	 */
	public int getFirstNonNullIndex( final List<BigDecimal> data, final int minimumNumberOfPrices ) {

		// validateSizeOfStore( data.length, maximumIndex );

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray( data, firstNonNullItem )) {
			firstNonNullItem++;
		}

		// validateFirstItemInMaximumIndex( firstNonNullItem, maximumIndex );

		final int lastNonNullItem = getLastNonNullIndex( data );
		final int numberOfItems = getNumberOfItems( firstNonNullItem, lastNonNullItem );

		validateNumberOfItems( numberOfItems, minimumNumberOfPrices );

		final int numberOfConsecutiveItems = getNumberOfConsectiveItems( data, firstNonNullItem, lastNonNullItem );

		validateEnoughConsecutiveItems( numberOfConsecutiveItems, minimumNumberOfPrices );

		return firstNonNullItem;
	}

	private void validateEnoughConsecutiveItems( final int numberOfConsecutiveItems, final int minimumNumberOfPrices ) {
		if (numberOfConsecutiveItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s consecutive non null data points are needed, only %s given",
							minimumNumberOfPrices, numberOfConsecutiveItems ) );
		}
	}

	private void validateNumberOfItems( final int numberOfItems, final int minimumNumberOfPrices ) {
		// Enough data to calculate indicator?
		if (numberOfItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
					String.format( "At least %s non null data points are needed, only %s given", minimumNumberOfPrices,
							numberOfItems ) );
		}
	}

	// private void validateFirstItemInMaximumIndex( final int firstNonNullItem, final int
	// maximumIndex ) {
	// if (firstNonNullItem >= maximumIndex) {
	// throw new IllegalArgumentException( String.format(
	// "The index of the first non-null item index of: %s exceeds the maximum allowed index of: %s",
	// firstNonNullItem, maximumIndex ) );
	// }
	// }

	private void validateIndexInArray( final TradingDayPrices[] data, final int firstNonNullItem,
			final int requiredNumberOfPrices ) {
		if (firstNonNullItem < 0) {
			throw new IllegalArgumentException(
					String.format( "There are not enough non-null items in array of size: %s requiring: %s items",
							data.length - 1, requiredNumberOfPrices ) );
		}
	}

	// private void validateSizeOfStore( final int storeSize, final int maximumIndex ) {
	// if (storeSize > maximumIndex) {
	// throw new IllegalArgumentException(
	// String.format( "The number of data points given: %s exceeds the size of the store: %s",
	// storeSize,
	// maximumIndex ) );
	// }
	// }

	private int getNumberOfItems( final int firstNonNullItem, final int lastNonNullItem ) {
		// Number of items, accounting for zero indexed array
		return lastNonNullItem - firstNonNullItem + 1;
	}

	private <T> int getNumberOfConsectiveItems( final T[] data, final int firstNonNullItem,
			final int lastNonNullItem ) {

		// Are the items consecutively populated (as expected)
		int numberOfConsecutiveItems = firstNonNullItem;
		while (numberOfConsecutiveItems < lastNonNullItem && !isNullEntry( data, numberOfConsecutiveItems )) {
			numberOfConsecutiveItems++;
		}

		// Account for zero index of array
		numberOfConsecutiveItems++;

		return numberOfConsecutiveItems;
	}

	private <T> int getNumberOfConsectiveItems( final List<T> data, final int firstNonNullItem,
			final int lastNonNullItem ) {

		// Are the items consecutively populated (as expected)
		int numberOfConsecutiveItems = firstNonNullItem;
		while (numberOfConsecutiveItems < lastNonNullItem && !isNullEntry( data, numberOfConsecutiveItems )) {
			numberOfConsecutiveItems++;
		}

		// Account for zero index of array
		numberOfConsecutiveItems++;

		return numberOfConsecutiveItems;
	}

	private <T> boolean isNullEntryWithinArray( final List<T> data, final int index ) {
		return isWithinArray( data.size(), index ) && isNullEntry( data, index );
	}

	private <T> boolean isNullEntryWithinArray( final T[] data, final int index ) {
		return isWithinArray( data.length, index ) && isNullEntry( data, index );
	}

	private <T> boolean isNullEntry( final T[] data, final int index ) {
		return data[index] == null;
	}

	private <T> boolean isNullEntry( final List<T> data, final int index ) {
		return data.get( index ) == null;
	}

	private boolean isWithinArray( final int size, final int index ) {
		return (index >= 0) && (index < size);
	}
}
