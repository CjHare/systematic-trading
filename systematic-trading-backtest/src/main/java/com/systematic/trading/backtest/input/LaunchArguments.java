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

import java.util.Map;
import java.util.Optional;

import com.systematic.trading.backtest.configuration.OutputType;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;

/**
 * Parses the arguments given on launch, validating and converting them.
 * 
 * @author CJ Hare
 */
public class LaunchArguments {

	//TODO drop the backtest prefix

	enum ArgumentKey {
		OUTPUT_TYPE("-output");

		private final String key;

		private ArgumentKey( final String key ) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public static Optional<ArgumentKey> get( final String arg ) {

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

	private final OutputType outputType;

	public LaunchArguments( final LaunchArgumentsParser argumentParser,
	        final LaunchArgument<OutputType> outputArgument, final String... args ) {
		final Map<ArgumentKey, String> arguments = argumentParser.parse(args);
		this.outputType = outputArgument.get(arguments);
	}

	public String getBaseOutputDirectory( final DepositConfiguration depositAmount ) {
		return String.format(BASE_OUTPUT_DIRECTORY, depositAmount);
	}

	public OutputType getOutputType() {
		return outputType;
	}
}