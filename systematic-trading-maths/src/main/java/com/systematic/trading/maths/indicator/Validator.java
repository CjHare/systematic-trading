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
package com.systematic.trading.maths.indicator;

import java.util.Collection;

/**
 * Validation of arrays an list contents.
 * 
 * @author CJ Hare
 */
public interface Validator {

	/**
	 * Verify the value meets the minimum expected.
	 * 
	 * @param minimum
	 *            actual must be larger then this value.
	 * @param actual
	 *            value being evaluated.
	 */
	void verifyGreaterThan( int minimum, int actual );

	/**
	 * Verifies the instance refers to an actual object, not the null reference.
	 * 
	 * @param instnace
	 *            what is being verified as not null.
	 */
	void verifyNotNull( Object instance );

	/**
	 * Verifies there are no <code>null</code> entries in the list.
	 * 
	 * @param values
	 *            list of values to parse for the existence of <code>null</code>.
	 */
	<T> void verifyZeroNullEntries( Collection<T> values );

	/**
	 * Verifies there are no <code>null</code> entries in the array.
	 * 
	 * @param values
	 *            array of values to parse for the existence of <code>null</code>.
	 */
	<T> void verifyZeroNullEntries( T[] values );

	/**
	 * Verifies that there are the expected number of non <code>null</code> sequential entries in
	 * the list.
	 * 
	 * @param values
	 *            list of values to check for a run of non <code>null</code> entries of the desired
	 *            size.
	 * @param numberOfValues
	 *            minimum number of values expected.
	 */
	<T> void verifyEnoughValues( Collection<T> values, int numberOfValues );

	/**
	 * Verifies that there are the expected number of non <code>null</code> sequential entries in
	 * the array.
	 * 
	 * @param values
	 *            array of values to check for a run of non <code>null</code> entries of the desired
	 *            size.
	 * @param numberOfValues
	 *            minimum number of values expected.
	 */
	<T> void verifyEnoughValues( T[] values, int numberOfValues );
}
