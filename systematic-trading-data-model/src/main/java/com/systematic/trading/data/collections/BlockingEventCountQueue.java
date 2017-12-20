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

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BlockingEventCount backed with a blocking queue.
 * 
 * @author CJ Hare
 */
public class BlockingEventCountQueue implements BlockingEventCount {

	private static final Logger LOG = LogManager.getLogger(BlockingEventCount.class);

	private final BlockingQueue<LocalTime> ringBuffer;
	private final Duration expiry;

	public BlockingEventCountQueue( final int eventsPerDuration, final Duration expiry ) {
		this.ringBuffer = new LinkedBlockingQueue<>(eventsPerDuration);
		this.expiry = expiry;
	}

	@Override
	public void add() {

		try {
			ringBuffer.put(LocalTime.now());

		} catch (final InterruptedException e) {
			LOG.warn("Interrupted when attempting ", e);

			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public synchronized void clean() {

		final LocalTime expired = LocalTime.now().minus(expiry);
		final int entries = ringBuffer.size();

		for (int i = 0; i < entries; i++) {
			final LocalTime candidate = ringBuffer.peek();

			if (expired.isAfter(candidate)) {
				final LocalTime removed = ringBuffer.remove();

				if (candidate != removed) {
					LOG.error(String.format("Attempted to remove: %s, but removed: %s", candidate, removed));
				}
			}
		}
	}
}