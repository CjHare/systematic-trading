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
package com.systematic.trading.backtest.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.systematic.trading.collection.LimitedSizeQueue;

/**
 * Tests the limited size implementation of LinkedList
 * 
 * @author CJ Hare
 */
public class LimitedQueueTest {

	@Test
	public void addUnderLimit() {
		final int limit = 5;
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, limit );
		final String one = "one";

		// Add the data to the list
		list.add( one );

		assertEquals( 1, list.size() );
		assertNotNull( list.get( 0 ) );
		assertEquals( one, list.get( 0 ) );
	}

	@Test
	public void addOnLimit() {
		final int limit = 2;
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, limit );
		final String one = "one";
		final String two = "two";

		// Add the data to the list
		list.add( one );
		list.add( two );

		assertEquals( 2, list.size() );
		assertNotNull( list.get( 0 ) );
		assertNotNull( list.get( 1 ) );
		assertEquals( one, list.get( 0 ) );
		assertEquals( two, list.get( 1 ) );
	}

	@Test
	public void addOverLimit() {
		final int limit = 2;
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, limit );
		final String one = "one";
		final String two = "two";
		final String three = "three";

		// Add the data to the list
		list.add( one );
		list.add( two );
		list.add( three );

		assertEquals( 2, list.size() );
		assertNotNull( list.get( 0 ) );
		assertNotNull( list.get( 1 ) );
		assertEquals( two, list.get( 0 ) );
		assertEquals( three, list.get( 1 ) );
	}

	@Test(expected = UnsupportedOperationException.class)
	public void toArrayException() {
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, 1 );

		list.toArray( new String[0] );

		fail( "Expecting exception" );
	}

	@Test
	public void toArraySizeOne() {
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, 1 );
		list.add( "first" );

		final String[] a = list.toArray();

		assertEquals( 1, a.length );
		assertEquals( "first", a[0] );
	}

	@Test
	public void toArraySizeTwoPopulationOne() {
		final LimitedSizeQueue<String> list = new LimitedSizeQueue<String>( String.class, 2 );
		list.add( "first" );

		final String[] a = list.toArray();

		assertEquals( 1, a.length );
		assertEquals( "first", a[0] );
	}

}
