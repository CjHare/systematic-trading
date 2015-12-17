/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.maths.store;

import java.math.BigDecimal;

import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.exception.TooManyDataPoints;

/**
 * Restrictions on the input accepted by indicators, also deciding how the results store is
 * acquired.
 * 
 * @author CJ Hare
 */
public interface IndicatorOutputStore {

	//TODO delete me!!
	
	/**
	 * Obtains the array to store the results from the indicator.
	 * 
	 * @param size, the number of elements that require storing.
	 * @return an empty (<code>null</code> entries) array to store the indicator output.
	 * @throws TooFewDataPoints not enough closing prices to perform calculations.
	 * @throws TooManyDataPoints too many days of closing prices have been provided.
	 */
	BigDecimal[] getStore( int size ) throws TooFewDataPoints, TooManyDataPoints;
}
