/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of [project] nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.input;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.input.LaunchArgument.ArgumentKey;

/**
 * Parses the launch arguments, converting them into a typed map.
 * 
 * @author CJ Hare
 */
public class CommandLineLaunchArgumentsParser implements LaunchArgumentsParser {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger(CommandLineLaunchArgumentsParser.class);

	@Override
	public Map<ArgumentKey, String> parse( final String[] args ) {

		final Map<ArgumentKey, String> argumentPairs = new EnumMap<>(ArgumentKey.class);

		for (int i = 0; i < args.length; i += 2) {

			final Optional<ArgumentKey> key = ArgumentKey.get(args[i]);

			if (key.isPresent()) {
				if (hasInsufficuentArgumentCount(i + 1, args)) {
					missingArgument(args[i]);
				}

				argumentPairs.put(key.get(), args[i + 1]);

			} else {
				unmappedArgument(args[i]);
			}
		}

		return argumentPairs;
	}

	private boolean hasInsufficuentArgumentCount( final int index, final String... args ) {

		return index >= args.length;
	}

	/**
	 * Argument expecting a matching value.
	 */
	private void missingArgument( final String argumentKey ) {

		throw new IllegalArgumentException(String.format("Missing value for argument key %s", argumentKey));
	}

	/**
	 * Argument value that is not paired with an expected key.
	 */
	private void unmappedArgument( final String argument ) {

		LOG.warn("Unknown / unused argument {}", () -> argument);
	}
}