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
package com.systematic.trading.backtest.output.elastic.app.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.systematic.trading.backtest.output.elastic.app.exception.PerformanceTrialException;
import com.systematic.trading.backtest.output.elastic.app.model.PerformanceTrialSummary;
import com.systematic.trading.exception.ServiceException;

/**
 * Output of the performance trial results to a file.
 * 
 * @author CJ Hare
 */
public class PerformanceTrialFileAppenderOutput implements PerformanceTrialOutput {

	private final PerformanceTrialOutputFormatter outputFormat = new PerformanceTrialOutputFormatter();
	private final String filename;
	private final String trialId;

	public PerformanceTrialFileAppenderOutput( final String filename, final String trialId ) {
		this.filename = filename;
		this.trialId = trialId;
	}

	@Override
	public void display( final PerformanceTrialSummary summary ) throws ServiceException {

		try {
			Files.write(Paths.get(filename), outputFormat.format(trialId, summary).getBytes(),
			        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (final IOException e) {
			throw new PerformanceTrialException(e);
		}
	}
}