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
package com.systematic.trading.backtest.output.elastic.app.output;

import java.time.Duration;

import com.systematic.trading.backtest.output.elastic.app.model.PerformanceTrialSummary;

/**
 * Generic format for the ouptut of the summary display.
 * 
 * @author CJ Hare
 */
public class PerformanceTrialOutputFormatter {

	private static final String NEGATIVE_SIGN = "-";
	private static final String EMPTY_STRING = "";
	private final int SECONDS_PER_HOUR = 3600;
	private final int SECONDS_PER_MINUTE = 60;

	public String format( final String trialId, final PerformanceTrialSummary summary ) {

		return String.format("%s, %d, %s, %.2f, %s", trialId, summary.numberOfRecords(), format(summary.elapsed()),
		        summary.recordsPerSecond(), System.lineSeparator());
	}

	/**
	 * @param duration
	 *            in the format of: hours:minutes:seconds
	 */
	private String format( final Duration duration ) {

		return String.format("%s%d:%02d:%02d", isNegative(duration) ? NEGATIVE_SIGN : EMPTY_STRING, hours(duration),
		        minutes(duration), seconds(duration));
	}

	private boolean isNegative( final Duration duration ) {

		return duration.getSeconds() < 0;
	}

	private long hours( final Duration duration ) {

		return Math.abs(duration.getSeconds()) / SECONDS_PER_HOUR;
	}

	private long minutes( final Duration duration ) {

		return (Math.abs(duration.getSeconds()) % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
	}

	private long seconds( final Duration duration ) {

		return Math.abs(duration.getSeconds()) % SECONDS_PER_MINUTE;
	}
}