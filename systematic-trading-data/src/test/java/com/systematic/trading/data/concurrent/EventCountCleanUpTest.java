/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.data.concurrent;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.collections.BlockingEventCount;

/**
 * Ensures the throttler class exhibits the correct behaviour.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class EventCountCleanUpTest {

	@Mock
	private BlockingEventCount ringBuffer;

	private Duration interval = Duration.of(50, ChronoUnit.MILLIS);

	@Test
	public void end() throws InterruptedException {
		final EventCountCleanUp throttlerCode = new EventCountCleanUp(ringBuffer, interval);
		final Thread throttler = start(throttlerCode);

		end(throttlerCode);

		verifyEnded(throttler);
	}

	@Test
	public void interval() throws InterruptedException {
		final EventCountCleanUp throttlerCode = new EventCountCleanUp(ringBuffer, interval);
		start(throttlerCode);

		waitForTwoDurations();

		verifyCleanEvents(2);
	}

	private void verifyCleanEvents( final int times ) {
		verify(ringBuffer, atLeast(times)).clean();
		verifyNoMoreInteractions(ringBuffer);
	}

	private void waitForTwoDurations() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(interval.toMillis() * 2 + (interval.toMillis() / 20));
	}

	private void verifyEnded( final Thread cleanUpThread ) {
		assertFalse("Wrapping thread should have finished", cleanUpThread.isAlive());
		verifyZeroInteractions(ringBuffer);
	}

	private void end( final EventCountCleanUp throttlerCleanUp ) throws InterruptedException {
		throttlerCleanUp.end();

		// Wait for at least one loop to pick up the end i.e. close off the run()
		TimeUnit.MILLISECONDS.sleep(8);
	}

	private Thread start( final EventCountCleanUp throttlerCleanUp ) {
		final Thread cleanUp = new Thread(throttlerCleanUp);
		cleanUp.setDaemon(true);
		cleanUp.start();
		return cleanUp;
	}
}