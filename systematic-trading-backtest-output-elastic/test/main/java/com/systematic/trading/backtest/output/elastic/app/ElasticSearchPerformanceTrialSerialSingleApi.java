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

import java.time.LocalDate;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Stand alone application for clocking the time in performing posting of records to Elastic Search using the single record API.
 * 
 * @author CJ Hare
 */
public class ElasticSearchPerformanceTrialSerialSingleApi {

	/** Number of records to post to elastic search. */
	private static final int NUMBER_OF_RECORDS = 1000;

	/** The same text used for every record. */
	private static final String TEXT = "Sample_text";

	/** The same date used for every record. */
	private static final LocalDate DATE = LocalDate.now();

	private final ElasticSearchFacade elastic = new ElasticSearchFacade();

	public static void main( final String... args ) {
		new ElasticSearchPerformanceTrialSerialSingleApi().execute();
	}

	public void execute() {
		clear();
		setUp();
		summarise(sendData());
	}

	private void clear() {
		elastic.delete();
	}

	private void setUp() {
		elastic.putIndex();
		elastic.putMapping();
	}

	private StopWatch sendData() {
		final StopWatch timer = new StopWatch();
		timer.start();

		for (int i = 0; i < NUMBER_OF_RECORDS; i++) {
			elastic.postType(createRecord(i));
		}

		timer.stop();
		return timer;
	}

	/**
	 * @param value the only difference between each record.
	 */
	private ElasticSearchPerformanceTrialResource createRecord( final int value ) {
		return new ElasticSearchPerformanceTrialResource(TEXT, value, DATE);
	}

	private void summarise( final StopWatch timer ) {
		System.out.println(String.format("%,d Records executed in: %.2f seconds, %.2f records per second",
		        NUMBER_OF_RECORDS, getSeconds(timer), getRecordsInsertedPerSecond(timer)));
	}

	private float getSeconds( final StopWatch timer ) {
		return timer.getTime() / 1000f;
	}

	private float getRecordsInsertedPerSecond( final StopWatch timer ) {
		return NUMBER_OF_RECORDS / getSeconds(timer);
	}
}