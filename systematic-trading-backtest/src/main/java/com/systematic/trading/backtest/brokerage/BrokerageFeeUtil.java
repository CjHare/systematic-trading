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
package com.systematic.trading.backtest.brokerage;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Utility class for operations and constants related to fees.
 * 
 * @author CJ Hare
 */
public class BrokerageFeeUtil {

	public static final BigDecimal FIFTEEN = BigDecimal.valueOf( 15 );
	public static final BigDecimal THIRTEEN = BigDecimal.valueOf( 13 );
	public static final BigDecimal ELEVEN = BigDecimal.valueOf( 10 );
	public static final BigDecimal TEN = BigDecimal.valueOf( 10 );
	public static final BigDecimal NINE_NINTY = BigDecimal.valueOf( 9.9 );

	public static final BigDecimal TEN_BASIS_POINTS = BigDecimal.valueOf( .001 );
	public static final BigDecimal EIGHT_BASIS_POINTS = BigDecimal.valueOf( .0008 );
	public static final BigDecimal SEVENTY_FIVE_BASIS_POINTS = BigDecimal.valueOf( .00075 );

	/**
	 * Calculates and return the larger value from the absolute and percentage.
	 * 
	 * @param tradeValue sum of the equity trade whose brokerage is being calculated.
	 * @param absoluteFee absolute amount for brokerage.
	 * @param percentage relative amount based of the tradeValue.
	 * @param context math context defining the scale and precision to apply to operations.
	 * @return the larger between the absolute and relative given the specific trade value.
	 */
	public static BigDecimal applyLargest( final BigDecimal tradeValue, final BigDecimal absoluteFee,
			final BigDecimal percentage, final MathContext mathContext ) {
		final BigDecimal relativeFee = tradeValue.multiply( percentage, mathContext );
		return absoluteFee.compareTo( relativeFee ) > 0 ? absoluteFee : relativeFee;
	}
}
