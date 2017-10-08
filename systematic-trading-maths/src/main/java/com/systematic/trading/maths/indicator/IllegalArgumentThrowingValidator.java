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
package com.systematic.trading.maths.indicator;

import java.util.Collection;

/**
 * Validates the input given throwing an IllegalArgumentException when expectations are not met.
 * 
 * @author CJ Hare
 */
public class IllegalArgumentThrowingValidator implements Validator {

	private static final String MESSAGE_NULL_INPUT = "Unexpected null input has been given";
	private static final String MESSAGE_NULL_VALUE = "Unexpected null value contained in given input: %s";

	//TODO separate validation for null input
	
	@Override
	public <T> void verifyZeroNullEntries( final Collection<T> values ) {
		verifyNotNullInput(values);

		for (final T value : values) {
			verifyNotNullValue(value, values);
		}
	}

	@Override
	public <T> void verifyZeroNullEntries( final T[] values ) {
		verifyNotNullInput(values);

		for (final T value : values) {
			verifyNotNullValue(value, values);
		}
	}

	@Override
	public <T> void verifyEnoughValues( final Collection<T> data, final int requiredNumberOfPrices ) {
		validateNumberOfItems(getNumberOfNonNullItems(data), requiredNumberOfPrices);
	}

	@Override
	public <T> void verifyEnoughValues( final T[] data, final int requiredNumberOfPrices ) {

		// Skip any null entries
		int firstNonNullItem = 0;
		while (isNullEntryWithinArray(data, firstNonNullItem)) {
			firstNonNullItem++;
		}

		final int lastNonNullItem = getLastNonNullIndex(data);
		final int numberOfItems = getNumberOfItems(firstNonNullItem, lastNonNullItem);

		validateNumberOfItems(numberOfItems, requiredNumberOfPrices);
	}

	private void verifyNotNullValue( final Object maybe, final Object values ) {
		if (maybe == null) {
			throw new IllegalArgumentException(String.format(MESSAGE_NULL_VALUE, values));
		}
	}

	private void verifyNotNullInput( final Object maybe ) {
		if (maybe == null) {
			throw new IllegalArgumentException(MESSAGE_NULL_INPUT);
		}
	}

	private <T> int getLastNonNullIndex( final T[] data ) {
		// Find last non-null index
		int lastNonNullItem = data.length - 1;

		while (isNullEntryWithinArray(data, lastNonNullItem)) {
			lastNonNullItem--;
		}

		return lastNonNullItem;
	}

	private void validateNumberOfItems( final int numberOfItems, final int minimumNumberOfPrices ) {
		// Enough data to calculate indicator?
		if (numberOfItems < minimumNumberOfPrices) {
			throw new IllegalArgumentException(
			        String.format("At least %s non null data points are needed, only %s given", minimumNumberOfPrices,
			                numberOfItems));
		}
	}

	private int getNumberOfItems( final int firstNonNullItem, final int lastNonNullItem ) {
		// Number of items, accounting for zero indexed array
		return lastNonNullItem - firstNonNullItem + 1;
	}

	private <T> int getNumberOfNonNullItems( final Collection<T> data ) {

		// Are the items consecutively populated (as expected)
		int numberOfConsecutiveItems = 0;

		for (final T item : data) {
			if (item != null) {
				numberOfConsecutiveItems++;
			}
		}

		return numberOfConsecutiveItems;
	}

	private <T> boolean isNullEntryWithinArray( final T[] data, final int index ) {
		return isWithinArray(data.length, index) && isNullEntry(data, index);
	}

	private <T> boolean isNullEntry( final T[] data, final int index ) {
		return data[index] == null;
	}

	private boolean isWithinArray( final int size, final int index ) {
		return (index >= 0) && (index < size);
	}
}