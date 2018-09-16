/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import com.systematic.trading.backtest.input.OutputType;

/**
 * Launch argument parser and validation for the output type key value pairing.
 * 
 * @author CJ Hare
 */
public class OutputLaunchArgument implements LaunchArgument<OutputType> {

	private static final Map<String, OutputType> OUTPUT_TYPE_MAPPING = new HashMap<>();

	static {
		OUTPUT_TYPE_MAPPING.put("elastic_search", OutputType.ELASTIC_SEARCH);
		OUTPUT_TYPE_MAPPING.put("file_complete", OutputType.FILE_COMPLETE);
		OUTPUT_TYPE_MAPPING.put("file_minimum", OutputType.FILE_MINIMUM);
		OUTPUT_TYPE_MAPPING.put("no_display", OutputType.NO_DISPLAY);
	}

	/** Provides validation for the launch argument value. */
	private final LaunchArgumentValidator validator;

	public OutputLaunchArgument( final LaunchArgumentValidator validator ) {

		this.validator = validator;
	}

	@Override
	public OutputType get( final Map<LaunchArgumentKey, String> arguments ) {

		final OutputType outputType = OUTPUT_TYPE_MAPPING.get(arguments.get(LaunchArgumentKey.OUTPUT_TYPE));

		validator.validate(
		        outputType,
		        "%s argument is not in the set of supported OutputTypes: %s",
		        LaunchArgumentKey.OUTPUT_TYPE,
		        arguments.get(LaunchArgumentKey.OUTPUT_TYPE));

		return outputType;
	}
}
