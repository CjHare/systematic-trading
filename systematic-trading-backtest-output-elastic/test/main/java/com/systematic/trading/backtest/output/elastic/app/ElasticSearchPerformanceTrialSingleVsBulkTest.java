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

/**
 * Stand alone application for comparing the performance between the single and bulk APIs.
 * 
 * @author CJ Hare
 */
public class ElasticSearchPerformanceTrialSingleVsBulkTest {

	private final ElasticSearchFacade elastic = new ElasticSearchFacade();

	public static void main( final String... args ) {
		new ElasticSearchPerformanceTrialSingleVsBulkTest().execute();
	}

	public void execute() {
		clear();
		setUp();
		sendData();
		summarise();
	}

	private void clear() {
		elastic.delete();
	}

	private void setUp() {
		elastic.putIndex();
		elastic.putMapping();
	}

	private void sendData() {
		final String text = "AAAaa";
		final float value = 101;
		final LocalDate date = LocalDate.now();

		elastic.postType(new ElasticSearchPerformanceTrialResource(text, value, date));
	}

	private void summarise() {
		System.out.println("DONE!");
	}
}