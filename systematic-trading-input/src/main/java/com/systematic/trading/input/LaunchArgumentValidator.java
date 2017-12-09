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
package com.systematic.trading.input;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

/**
 * Common validation checks and exception responses for LaunchArguments. 
 * 
 * @author CJ Hare
 */
public class LaunchArgumentValidator {

	public void validate( final Object value, final String errorMessage, final Object... errorMessageArguments ) {
		if (isInvalidArgument(value)) {
			incorrectArguments(errorMessage, errorMessageArguments);
		}
	}

	public void validateDateFormat( final String value, final String errorMessage,
	        final Object... errorMessageArguments ) {
		if (isInvalidArgument(value) || isInvalidFormat(value)) {
			incorrectArguments(errorMessage, errorMessageArguments);
		}
	}

	public void validateNotEmpty( final String value, final String errorMessage,
	        final Object... errorMessageArguments ) {
		if (isInvalidArgument(value) || isEmpty(value)) {
			incorrectArguments(errorMessage, errorMessageArguments);
		}
	}

	private boolean isEmpty( final String value ) {
		return StringUtils.isEmpty(value);
	}

	private boolean isInvalidArgument( final Object value ) {
		return value == null;
	}

	private void incorrectArguments( final String message, final Object... arguments ) {
		throw new IllegalArgumentException(String.format(message, arguments));
	}

	private boolean isInvalidFormat( final String value ) {
		try {
			// Just making sure the date can be parsed, giving back an actual object
			return LocalDate.parse(value) == null;

		} catch (final DateTimeParseException e) {
			return true;
		}
	}
}