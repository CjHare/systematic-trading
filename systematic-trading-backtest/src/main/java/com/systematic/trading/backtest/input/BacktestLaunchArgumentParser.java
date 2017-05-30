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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.BacktestApplication.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;

/**
 * Parses the arguments given on launch, validating and converting them.
 * 
 * @author CJ Hare
 */
public class BacktestLaunchArgumentParser {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(BacktestLaunchArgumentParser.class);

	private enum ArgumentKey {
		OUTPUT_TYPE("-output");

		private String key;

		private ArgumentKey( final String key ) {
			this.key = key;
		}

		private static Optional<ArgumentKey> get( final String arg ) {

			for (final ArgumentKey candidate : ArgumentKey.values()) {
				if (candidate.key.equals(arg)) {
					return Optional.of(candidate);
				}
			}

			return Optional.empty();

		}
	}

	//TODO pass in - alternative batch for file output
	private static final String BASE_OUTPUT_DIRECTORY = "../../simulations/%s/";

	private static final Map<String, OutputType> outputTypeMapping = new HashMap<>();

	static {
		outputTypeMapping.put("elastic_search", OutputType.ELASTIC_SEARCH);
		outputTypeMapping.put("file_complete", OutputType.FILE_COMPLETE);
		outputTypeMapping.put("file_minimum", OutputType.FILE_MINIMUM);
		outputTypeMapping.put("no_display", OutputType.NO_DISPLAY);
	}

	private final OutputType outputType;

	//TODO fail on missing mandatory keys, validator pattern

	public BacktestLaunchArgumentParser( final String... args ) {

		final Map<ArgumentKey, String> arguments = parseArguments(args);

		this.outputType = outputTypeMapping.get(arguments.get(ArgumentKey.OUTPUT_TYPE));

		if (hasNoOutputType()) {
			incorrectArguments("Output argument is not in the set of supported OutputTypes: %s",
			        arguments.get(ArgumentKey.OUTPUT_TYPE));
		}
	}

	public String getBaseOutputDirectory( final DepositConfiguration depositAmount ) {
		return String.format(BASE_OUTPUT_DIRECTORY, depositAmount);
	}

	public OutputType getOutputType() {
		return outputType;
	}

	private boolean hasNoOutputType() {
		return this.outputType == null;
	}

	private void incorrectArguments( final String message, final Object... arguments ) {
		throw new IllegalArgumentException(String.format(message, arguments));
	}

	private Map<ArgumentKey, String> parseArguments( final String... args ) {

		final Map<ArgumentKey, String> argumentPairs = new EnumMap<>(ArgumentKey.class);

		for (int i = 0; i < args.length; i++) {

			final Optional<ArgumentKey> key = ArgumentKey.get(args[i]);

			if (key.isPresent()) {
				if (hasInsufficuentArgumentCount(i + 1, args)) {
					incorrectArguments("Missing value for argument key %w", args[i]);
				}

				argumentPairs.put(key.get(), args[++i]);

			} else {

				LOG.warn(String.format("Unknown / unused argument %s", args[i]));
			}
		}

		return argumentPairs;
	}

	private boolean hasInsufficuentArgumentCount( final int index, final String... args ) {
		return index >= args.length;
	}
}