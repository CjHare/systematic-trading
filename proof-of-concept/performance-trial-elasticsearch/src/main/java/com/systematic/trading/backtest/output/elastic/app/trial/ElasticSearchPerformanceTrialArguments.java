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
package com.systematic.trading.backtest.output.elastic.app.trial;

import com.systematic.trading.backtest.output.elastic.app.output.PerformanceTrialFileAppenderOutput;
import com.systematic.trading.backtest.output.elastic.app.output.PerformanceTrialOutput;
import com.systematic.trading.backtest.output.elastic.app.output.PerformanceTrialOverwriteFileOutput;

/**
 * Static utility class for inspecting input, providing default values when absent.
 * 
 * @author CJ Hare
 */
public class ElasticSearchPerformanceTrialArguments {

	private static final int ARGUMENT_INDEX_NUMBER_OF_RECORDS = 0;
	private static final int ARGUMENT_INDEX_OUTPUT_FILE = 1;

	/** Number of records to post to elastic search. */
	private static final int DEFAULT_NUMBER_OF_RECORDS = 1000;

	public static PerformanceTrialOutput getOutput( final String trialId, final String... args ) {
		return hasOutputFileArgument(args)
		        ? new PerformanceTrialFileAppenderOutput(geOutputFile(trialId, args), trialId)
		        : new PerformanceTrialOverwriteFileOutput(geOutputFile(trialId, args), trialId);
	}

	public static int getNumberOfRecords( final String... args ) {
		return hasNumberOfRecordsArgument(args) ? Integer.parseInt(args[ARGUMENT_INDEX_NUMBER_OF_RECORDS])
		        : DEFAULT_NUMBER_OF_RECORDS;
	}

	public static String geOutputFile( final String trialId, final String... args ) {
		return hasOutputFileArgument(args) ? args[ARGUMENT_INDEX_OUTPUT_FILE]
		        : String.format("results/%s.csv", trialId);
	}

	private static boolean hasOutputFileArgument( final String... args ) {
		return args.length > ARGUMENT_INDEX_OUTPUT_FILE;
	}

	private static boolean hasNumberOfRecordsArgument( final String... args ) {
		return args.length > ARGUMENT_INDEX_NUMBER_OF_RECORDS;
	}
}