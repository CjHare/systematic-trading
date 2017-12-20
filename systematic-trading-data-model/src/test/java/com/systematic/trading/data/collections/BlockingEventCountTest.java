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
package com.systematic.trading.data.collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author CJ Hare
 */
public class BlockingEventCountTest {

	private static final Duration EXPIRY = Duration.of(100, ChronoUnit.MILLIS);

	@Test
	public void add() {

		create(1).add();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addNoSpace() {

		create(0).add();
	}

	@Test
	public void addMore() throws InterruptedException {

		final BlockingEventCount events = create(1);

		addEvent(events);

		addExpectingBlocking(events);
	}

	@Test
	public void addTwiceWithClean() throws InterruptedException {

		final BlockingEventCount events = create(1);

		addEvent(events);

		cleanRingBuffer(events);

		addEvent(events);
	}

	private BlockingEventCount create( final int size ) {

		return new BlockingEventCountQueue(size, EXPIRY);
	}

	private void addEvent( final BlockingEventCount ringBuffer ) throws InterruptedException {

		final Thread expectedImmediateAdd = new Thread(() -> {
			ringBuffer.add();
		});

		expectedImmediateAdd.setDaemon(true);
		expectedImmediateAdd.start();

		TimeUnit.MILLISECONDS.sleep(50);

		assertFalse("add() should have succeeded and deamon thread closed", expectedImmediateAdd.isAlive());
	}

	/**
	 * Blocks until all the entries are expired then cleans the bufferRing.
	 */
	private void cleanRingBuffer( final BlockingEventCount ringBuffer ) throws InterruptedException {

		TimeUnit.MILLISECONDS.sleep(110);
		ringBuffer.clean();
	}

	private void addExpectingBlocking( final BlockingEventCount ringBuffer ) throws InterruptedException {

		final Thread expectedToWait = new Thread(() -> {
			ringBuffer.add();
		});

		expectedToWait.setDaemon(true);
		expectedToWait.start();

		TimeUnit.MILLISECONDS.sleep(50);

		assertTrue("Thread should still be waiting", expectedToWait.isAlive());
	}
}