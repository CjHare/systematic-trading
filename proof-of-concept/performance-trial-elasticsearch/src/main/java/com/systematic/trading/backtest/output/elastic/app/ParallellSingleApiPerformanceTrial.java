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
package com.systematic.trading.backtest.output.elastic.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.time.StopWatch;

import com.systematic.trading.backtest.output.elastic.app.configuration.ElasticSearchConfiguration;
import com.systematic.trading.backtest.output.elastic.app.resource.ElasticSearchPerformanceTrialRequestResource;

/**
 * Performance trial with each calls to elastic search being concurrently.
 * 
 * @author CJ Hare
 */
public class ParallellSingleApiPerformanceTrial extends ParallellPerformanceTrial {

	public ParallellSingleApiPerformanceTrial( final int numberOfRecords, final int numberOfThreads,
	        final ElasticSearchConfiguration elasticConfig ) {
		super(numberOfRecords, numberOfThreads, elasticConfig);
	}

	protected StopWatch sendData() {
		final int numberOfRecords = getNumberOfRecords();
		final ElasticSearchFacade elastic = getFacade();
		final ExecutorService pool = getPool();
		final CountDownLatch countDown = new CountDownLatch(numberOfRecords);

		final StopWatch timer = new StopWatch();
		timer.start();

		for (int i = 0; i < numberOfRecords; i++) {
			final ElasticSearchPerformanceTrialRequestResource record = createRecord(i);
			pool.submit(() -> {
				elastic.postType(record);
				countDown.countDown();
			});
		}

		wait(countDown);
		pool.shutdown();
		timer.stop();

		return timer;
	}
}