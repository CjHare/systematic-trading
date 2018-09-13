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

import java.util.Map;

import com.systematic.trading.data.DataServiceType;

/**
 * Launch argument parser and validation for the data service type key value pairing.
 * 
 * @author CJ Hare
 */
public class DataServiceLaunchArgument implements LaunchArgument<DataServiceType> {

	/** Provides validation for the launch argument value. */
	private final LaunchArgumentValidator validator;

	public DataServiceLaunchArgument( final LaunchArgumentValidator validator ) {

		this.validator = validator;
	}

	@Override
	public DataServiceType get( final Map<ArgumentKey, String> arguments ) {

		final String dataService = arguments.get(ArgumentKey.DATA_SERVICE);

		validator.validate(dataService, "%s argument is not present", ArgumentKey.DATA_SERVICE);
		validator.validateNotEmpty(dataService, "%s argument cannot be empty", ArgumentKey.DATA_SERVICE);

		return new DataServiceType(dataService);
	}
}
