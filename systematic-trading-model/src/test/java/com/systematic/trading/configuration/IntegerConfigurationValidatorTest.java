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
package com.systematic.trading.configuration;

import java.util.Random;

import org.junit.Test;

import com.systematic.trading.configuration.IntegerConfigurationValidator;
import com.systematic.trading.exception.ConfigurationValidationException;

/**
 * IntegerConfigurationValidator needs to validate String parsing with an acceptable range.
 * 
 * @author CJ Hare
 */
public class IntegerConfigurationValidatorTest {

	private static final int MAX = Integer.MAX_VALUE - 1;
	private static final int MIN = Integer.MIN_VALUE + 1;

	private IntegerConfigurationValidator validator;

	@Test
	public void onMinimum() throws ConfigurationValidationException {
		final int minimumValue = random();

		setupValidator(minimumValue, MAX);

		validate(minimumValue);
	}

	@Test(expected = ConfigurationValidationException.class)
	public void belowMinimum() throws ConfigurationValidationException {
		final int minimumValue = random();

		setupValidator(minimumValue, MAX);

		validate(minimumValue - 1);
	}

	@Test
	public void onMaximum() throws ConfigurationValidationException {
		final int maximumValue = random();

		setupValidator(MIN, maximumValue);

		validate(maximumValue);
	}

	@Test(expected = ConfigurationValidationException.class)
	public void aboveMaximum() throws ConfigurationValidationException {
		final int maximumValue = random();

		setupValidator(MIN, maximumValue);

		validate(maximumValue + 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void minimumAboveMaximum() {
		setupValidator(MAX, MIN);
	}

	@Test(expected = ConfigurationValidationException.class)
	public void noValue() throws ConfigurationValidationException {
		setupValidator(MIN, MAX);

		validate(null);
	}

	private void validate( final Integer value ) throws ConfigurationValidationException {
		validator.validate(String.valueOf(value));
	}

	private int random() {
		return new Random().nextInt(MAX);
	}

	private void setupValidator( final int minimum, final int maximum ) {
		validator = new IntegerConfigurationValidator(minimum, maximum);
	}
}