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
package com.systematic.trading.backtest.input;

import java.util.HashMap;
import java.util.Map;

import com.systematic.trading.backtest.BacktestApplication.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;

/**
 * Parses the arguments given on launch, validating and converting them.
 * 
 * @author CJ Hare
 */
public class BacktestLaunchArgumentParser {

	private static final int EXPECTED_NUMBER_ARGUMENTS = 1;
	private static final String BASE_OUTPUT_DIRECTORY = "../../simulations/%s/";
	private static final Map<String, OutputType> outputTypeMapping = new HashMap<>();

	static {
		outputTypeMapping.put("elastic-search", OutputType.ELASTIC_SEARCH);
		outputTypeMapping.put("file-complete", OutputType.FILE_COMPLETE);
		outputTypeMapping.put("file-minimum", OutputType.FILE_MINIMUM);
		outputTypeMapping.put("no-display", OutputType.NO_DISPLAY);
	}

	private final OutputType outputType;

	public BacktestLaunchArgumentParser( final String... args ) {

		if (hasIncorrectArgumentCount(args)) {
			incorrectArguments("Expecting %s arguments, provided with: %s", EXPECTED_NUMBER_ARGUMENTS, args.length);
		}

		this.outputType = outputTypeMapping.get(args[0]);

		if (hasNoOutputType()) {
			incorrectArguments("First argument is not in the set of supported OutputTypes: %s", args[0]);
		}
	}

	public String getBaseOutputDirectory( final DepositConfiguration depositAmount ) {
		return String.format(BASE_OUTPUT_DIRECTORY, depositAmount);
	}

	public OutputType getOutputType() {
		return outputType;
	}

	private boolean hasIncorrectArgumentCount( final String... args ) {
		return args.length != EXPECTED_NUMBER_ARGUMENTS;
	}

	private boolean hasNoOutputType() {
		return this.outputType == null;
	}

	private void incorrectArguments( final String message, final Object... arguments ) {
		throw new IllegalArgumentException(String.format(message, arguments));
	}
}