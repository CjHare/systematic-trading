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

import org.junit.Test;

/**
 * Verifies the IllegalArgumentThrowingValidator behaviour.
 * 
 * @author CJ Hare
 */
public class IllegalArgumentThrowingValidatorTest {

	@Test
	public void verifyEnoughValues() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add("one");
		data.add("two");

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyNotEnoughValues() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add("one");

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEnoughValuesAllNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add(null);
		data.add(null);

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyEnoughValuesIgnoringStartingNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add(null);
		data.add("one");
		data.add("two");

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyEnoughValuesIgnoringLastNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add("one");
		data.add("two");
		data.add(null);

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyEnoughValuesArray() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[2];
		data[0] = "one";
		data[1] = "two";

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyNotEnoughValuesArray() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[1];
		data[0] = "one";

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEnoughValuesArrayAllNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[2];

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyEnoughValuesArrayIgnoringStartingNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[3];
		data[0] = null;
		data[1] = "one";
		data[2] = "two";

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyEnoughValuesArrayIgnoringLastNull() {
		final int requiredNumberOfPrices = 2;
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[3];
		data[0] = "one";
		data[1] = "two";
		data[2] = null;

		validator.verifyEnoughValues(data, requiredNumberOfPrices);
	}

	@Test
	public void verifyZeroNullEntries() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[1];
		data[0] = "one";

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesStartingNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add(null);
		data.add("one");
		data.add("two");

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesEndingNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add("one");
		data.add("two");
		data.add(null);

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesMidNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final List<String> data = new ArrayList<String>();
		data.add("one");
		data.add(null);
		data.add("two");

		validator.verifyZeroNullEntries(data);
	}

	@Test
	public void verifyZeroNullEntriesArray() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[1];
		data[0] = "one";

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesSArraytartingNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[3];
		data[0] = null;
		data[1] = "one";
		data[2] = "two";

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesArrayEndingNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[3];
		data[0] = "one";
		data[1] = "two";
		data[2] = null;

		validator.verifyZeroNullEntries(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyZeroNullEntriesArrayMidNull() {
		final IllegalArgumentThrowingValidator validator = new IllegalArgumentThrowingValidator();

		final String[] data = new String[3];
		data[0] = "one";
		data[1] = null;
		data[2] = "two";

		validator.verifyZeroNullEntries(data);
	}

}
