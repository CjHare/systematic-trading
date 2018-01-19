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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.systematic.trading.input.LaunchArgumentValidator;

/**
 * Verification of the LaunchArgumentValidator.
 * 
 * @author CJ Hare
 */
public class LaunchArgumentValidatorTest {

	@Test
	public void validate() {

		new LaunchArgumentValidator().validate("", "Not expected message");
	}

	@Test
	public void validateException() {

		try {
			new LaunchArgumentValidator().validate(null, "Expected error message");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected error message", e.getMessage());
		}
	}

	@Test
	public void validateMessage() {

		try {
			new LaunchArgumentValidator().validate(null, "Expected error message, %s, %s, %s", "one", "two", "three");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected error message, one, two, three", e.getMessage());
		}
	}

	@Test
	public void invalidLocalDateFormat() {

		try {
			new LaunchArgumentValidator().validateDateFormat("invalid date format",
			        "Expected date format error message");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected date format error message", e.getMessage());
		}
	}

	@Test
	public void missingLocalDateValue() {

		try {
			new LaunchArgumentValidator().validateDateFormat(null, "Expected date format error message");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected date format error message", e.getMessage());
		}
	}

	@Test
	public void missingNotEmptyValue() {

		try {
			new LaunchArgumentValidator().validateNotEmpty(null, "Expected not empty error message");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected not empty error message", e.getMessage());
		}
	}

	@Test
	public void notEmptyValue() {

		try {
			new LaunchArgumentValidator().validateNotEmpty("", "Expected not empty error message");
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected not empty error message", e.getMessage());
		}
	}

	@Test
	public void presentValue() {

		new LaunchArgumentValidator().validateNotEmpty("not empty", "Expected not empty error message");
	}

	@Test
	public void validLocalDateFormat() {

		new LaunchArgumentValidator().validateDateFormat("2017-06-06", "Not expected message");
	}
}