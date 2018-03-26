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
package com.systematic.trading.strategy.model.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.strategy.model.collection.LimitedSizeQueue;

/**
 * Tests the limited size implementation of LinkedList
 * 
 * @author CJ Hare
 */
public class LimitedSizeQueueTest {

	/** List instance being tested. */
	private LimitedSizeQueue<String> list;

	@Before
	public void setUp() {

		list = new LimitedSizeQueue<String>(String.class, 2);
	}

	@Test
	public void addUnderLimit() {

		final String one = "one";

		list.add(one);

		verifyContents(one);
	}

	@Test
	public void addOnLimit() {

		final String one = "one";
		final String two = "two";

		list.add(one);
		list.add(two);

		verifyContents(one, two);
	}

	@Test
	public void addOverLimit() {

		final String one = "one";
		final String two = "two";
		final String three = "three";

		list.add(one);
		list.add(two);
		list.add(three);

		verifyContents(two, three);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toArrayException() {

		list.toArray(new String[0]);
	}

	@Test
	public void toArray() {

		final String[] converted = list.toArray();

		verifyContents(converted);
	}

	@Test
	public void toArrayPartiallyPopulated() {

		list.add("first");

		final String[] converted = list.toArray();

		verifyContents(converted, "first");
	}

	@Test
	public void toArrayFullyPopulated() {

		list.add("first");
		list.add("second");

		final String[] converted = list.toArray();

		verifyContents(converted, "first", "second");
	}

	private void verifyContents( final String[] converted, final String... expectedContents ) {

		assertEquals(expectedContents.length, converted.length);

		for (int i = 0; i < expectedContents.length; i++) {
			assertNotNull(converted[i]);
			assertEquals(expectedContents[i], converted[i]);
		}
	}

	private void verifyContents( final String... expectedContents ) {

		assertEquals(expectedContents.length, list.size());

		for (int i = 0; i < expectedContents.length; i++) {
			assertNotNull(list.get(i));
			assertEquals(expectedContents[i], list.get(i));
		}
	}
}
