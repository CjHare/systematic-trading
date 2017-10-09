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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the IllegalArgumentThrowingValidator behaviour.
 * 
 * @author CJ Hare
 */
public class IllegalArgumentThrowingValidatorTest {

	/** Validator instance being tested.*/
	private IllegalArgumentThrowingValidator validator;

	@Before
	public void setUp() {
		validator = new IllegalArgumentThrowingValidator();
	}

	@Test
	public void verifyEnoughValues() {
		final List<String> data = createList("one", "two");

		verifyEnoughValues(data, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyNotEnoughValues() {
		final List<String> data = createList("one");

		verifyEnoughValues(data, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEnoughValuesAllNull() {
		final List<String> data = createList(null, null);

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesIgnoringStartingNull() {
		final List<String> data = createList(null, "one", "two");

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesIgnoringLastNull() {
		final List<String> data = createList("one", "two", null);

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesThreeValues() {
		final List<String> data = createList("one", "two", "three");

		verifyEnoughValues(data, 3);
	}

	@Test
	public void verifyEnoughValuesArray() {
		final String[] data = createArray("one", "two");

		verifyEnoughValues(data, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyNotEnoughValuesArray() {
		final String[] data = createArray("one");

		verifyEnoughValues(data, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEnoughValuesArrayAllNull() {
		final String[] data = new String[2];

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesArrayIgnoringStartingNull() {
		final String[] data = createArray(null, "one", "two");

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesArrayIgnoringLastNull() {
		final String[] data = createArray("one", "two", null);

		verifyEnoughValues(data, 2);
	}

	@Test
	public void verifyEnoughValuesArrayThreeValues() {
		final String[] data = createArray("one", "two", "three");

		verifyEnoughValues(data, 3);
	}

	@Test
	public void verifyZeroNullEntriesEmpty() {
		final List<String> data = new ArrayList<String>();

		verifyZeroNullEntries(data);
	}

	@Test
	public void verifyZeroNullEntries() {
		final List<String> data = createList("one");

		verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesStartingNull() {
		final List<String> data = createList(null, "one", "two");

		verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesMidNull() {
		final List<String> data = createList("one", null, "two");

		verifyZeroNullEntries(data);
	}

	@Test
	public void verifyZeroNullEntriesArrayEmpty() {
		final String[] data = new String[0];

		verifyZeroNullEntries(data);
	}

	@Test
	public void verifyZeroNullEntriesArray() {
		final String[] data = createArray("one");

		verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesSArraytartingNull() {
		final String[] data = createArray(null, "one", "two");

		verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesArrayEndingNull() {
		final String[] data = createArray("one", "two", null);

		verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesArrayMidNull() {
		final String[] data = createArray("one", null, "two");

		verifyZeroNullEntries(data);
	}

	@Test
	public void verifyNotNull() {
		final String[] data = new String[0];

		verifyNotNull(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyNotNullWhenNull() {
		verifyNotNull(null);
	}

	private void verifyNotNull( final Object instance ) {
		validator.verifyNotNull(instance);
	}

	private void verifyZeroNullEntries( final String[] data ) {
		validator.verifyZeroNullEntries(data);
	}

	private void verifyZeroNullEntries( final List<String> data ) {
		validator.verifyZeroNullEntries(data);
	}

	private void verifyEnoughValues( final String[] data, final int expectedNumberOfValues ) {
		validator.verifyEnoughValues(data, expectedNumberOfValues);
	}

	private void verifyEnoughValues( final List<String> data, final int expectedNumberOfValues ) {
		validator.verifyEnoughValues(data, expectedNumberOfValues);
	}

	private List<String> createList( final String... values ) {
		final List<String> data = new ArrayList<String>();

		for (final String value : values) {
			data.add(value);
		}

		return data;
	}

	private String[] createArray( final String... values ) {
		return values;
	}
}