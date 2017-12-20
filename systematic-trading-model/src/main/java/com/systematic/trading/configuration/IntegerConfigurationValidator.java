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
package com.systematic.trading.configuration;

import com.systematic.trading.exception.ConfigurationValidationException;

/**
 * Validates input as a integer.
 * 
 * @author CJ Hare
 */
public class IntegerConfigurationValidator implements ConfigurationValidator<Integer> {

	private final int minimumInclusive;
	private final int maximumInclusive;

	/**
	 * Input must be within the given bounds,
	 */
	public IntegerConfigurationValidator( final int minimumInclusive, final int maximumInclusive ) {
		this.minimumInclusive = minimumInclusive;
		this.maximumInclusive = maximumInclusive;

		verifyMinimumLessThenMaximum();
	}

	@Override
	public Integer validate( final String input ) throws ConfigurationValidationException {

		final Integer value;

		try {
			value = Integer.parseInt((String) input);
		} catch (final NumberFormatException e) {
			throw new ConfigurationValidationException(String.format(
			        "Expecting either an String value containing an Integer to parse, but given: \"%s\"", input));
		}

		if (isOutsideRange(value)) {
			throw new ConfigurationValidationException(
			        String.format("Expecting an Integer within inclusive range of: %s to %s, but given: %s",
			                minimumInclusive, maximumInclusive, input));

		}

		return value;
	}

	private boolean isOutsideRange( final int value ) {

		return value < minimumInclusive || value > maximumInclusive;
	}

	private void verifyMinimumLessThenMaximum() {

		if (minimumInclusive > maximumInclusive) {
			throw new IllegalArgumentException(
			        String.format("Minimum is expected to be less then the maximum, minumum: %s, maximum: %s",
			                minimumInclusive, maximumInclusive));
		}
	}
}