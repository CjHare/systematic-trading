/**
 * Copyright (c) 2015-2017, CJ Hare
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
package com.systematic.trading.backtest.output.elastic.app.trial;

import com.systematic.trading.backtest.output.elastic.app.SerialBulkApiPerformanceTrial;
import com.systematic.trading.backtest.output.elastic.app.configuration.ElasticSearchConfigurationBuilder;
import com.systematic.trading.backtest.output.elastic.app.trial.input.ElasticSearchPerformanceTrialArguments;
import com.systematic.trading.exception.ServiceException;

/**
 * Stand alone application for clocking the time in performing posting of records to Elastic Search.
 * 
 * Investigating:
 * Effect of disabling Elastic index refreshing during update.
 * 
 * Trial Configuration:
 * 1,000 records
 * Serial execution
 * Single record API
 * 
 * Elastic Index Configuration (default):
 * 5 Shards
 * 1 Replica
 * 
 * Optional input:
 * args[0] == number of records
 * args[0] == output file
 * 
 * @author CJ Hare
 */
public class ElasticSearchSerialPerformanceTrialBulkApiRefreshDisabled {

	private static final String TRIAL_ID = ElasticSearchSerialPerformanceTrialBulkApiRefreshDisabled.class
	        .getSimpleName();

	public static void main( final String... args ) throws ServiceException {

		ElasticSearchPerformanceTrialArguments.output(TRIAL_ID, args)
		        .display(new SerialBulkApiPerformanceTrial(ElasticSearchPerformanceTrialArguments.numberOfRecords(args),
		                new ElasticSearchConfigurationBuilder().withDisableIndexRefresh(true).build()).execute());
	}
}