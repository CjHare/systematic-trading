/**
 * Copyright (c) 2015-2018, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import java.time.Duration;
import java.time.LocalDate;

import org.apache.commons.lang3.time.StopWatch;

import com.systematic.trading.backtest.output.elastic.app.configuration.ElasticSearchConfiguration;
import com.systematic.trading.backtest.output.elastic.app.model.PerformanceTrialSummary;
import com.systematic.trading.backtest.output.elastic.app.resource.ElasticSearchPerformanceTrialRequestResource;
import com.systematic.trading.backtest.output.elastic.resource.ElasticBulkApiMetaDataRequestResource;

/**
 * Behaviour for evaluating Elastic search performance.
 * 
 * @author CJ Hare
 */
public abstract class PerformanceTrial {

	/** Bulk API action for creating document and generating it's ID. */
	private static final String ACTION_CREATE_GENERATE_DOCUMENT_ID = "index";

	/** The same text used for every record. */
	private static final String TEXT = "Sample_text";

	/** The same date used for every record. */
	private static final LocalDate DATE = LocalDate.now();

	private final ElasticSearchFacade elastic;
	private final int numberOfRecords;
	private final boolean disableIndexRefresh;
	private final boolean bulkApiMetaContainsIndex;
	private final boolean bulkApiMetaContainsType;

	public PerformanceTrial( final int numberOfRecords, final int numberOfThreads,
	        final ElasticSearchConfiguration elasticConfig ) {
		this.numberOfRecords = numberOfRecords;
		this.elastic = new ElasticSearchFacade(elasticConfig);
		this.disableIndexRefresh = elasticConfig.isDisableIndexRefresh();
		this.bulkApiMetaContainsIndex = elasticConfig.isBulkApiMetaContainsIndex();
		this.bulkApiMetaContainsType = elasticConfig.isBulkApiMetaContainsType();
	}

	public PerformanceTrial( final int numberOfRecords, final ElasticSearchConfiguration elasticConfig ) {
		this(numberOfRecords, 1, elasticConfig);
	}

	public PerformanceTrialSummary execute() {

		clear();
		setUp();
		final PerformanceTrialSummary summary = summarise(sendData());
		tearDown();
		return summary;
	}

	protected abstract StopWatch sendData();

	/**
	 * @param value
	 *            the only difference between each record.
	 */
	protected ElasticSearchPerformanceTrialRequestResource createRecord( final int value ) {

		return new ElasticSearchPerformanceTrialRequestResource(TEXT, value, DATE);
	}

	protected ElasticBulkApiMetaDataRequestResource bulkApiMeta() {

		return new ElasticBulkApiMetaDataRequestResource(ACTION_CREATE_GENERATE_DOCUMENT_ID,
		        bulkApiMetaContainsIndex ? PerformanceTrialFields.INDEX_NAME : null,
		        bulkApiMetaContainsType ? PerformanceTrialFields.MAPPING_NAME : null, null);
	}

	protected ElasticSearchFacade facade() {

		return elastic;
	}

	protected int numberOfRecords() {

		return numberOfRecords;
	}

	private void clear() {

		elastic.delete();
	}

	private void setUp() {

		elastic.putIndex();
		elastic.putMapping();

		if (disableIndexRefresh) {
			elastic.disableIndexRefresh();
		}
	}

	private void tearDown() {

		if (disableIndexRefresh) {
			elastic.enableIndexRefresh();
		}
	}

	private PerformanceTrialSummary summarise( final StopWatch timer ) {

		return new PerformanceTrialSummary(numberOfRecords, Duration.ofSeconds(seconds(timer)),
		        recordsInsertedPerSecond(timer));
	}

	private long seconds( final StopWatch timer ) {

		return timer.getTime() / 1000l;
	}

	private float recordsInsertedPerSecond( final StopWatch timer ) {

		return numberOfRecords / (float) seconds(timer);
	}
}