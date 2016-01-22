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
package com.systematic.trading.simulation.equity.fee.management;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Period;

import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;

/**
 * Management fee calculation where the fee changes depending on the value of the equities held.
 * 
 * @author CJ Hare
 */
public class LadderedEquityManagementFeeCalculator implements EquityManagementFeeCalculator {

	/** Upper range for each of the fee bands. */
	private final BigDecimal[] range;

	/** Percentage fee to apply to the matching index in the band array. */
	private final BigDecimal[] percentageFee;

	/** All the mathematical operation works within the context. */
	private final MathContext mathContext;

	public LadderedEquityManagementFeeCalculator( final BigDecimal[] range, final BigDecimal[] percentageFee,
			final MathContext mathContext ) {
		this.percentageFee = percentageFee;
		this.mathContext = mathContext;
		this.range = range;

		if (range.length + 1 != percentageFee.length) {
			throw new IllegalArgumentException(
					String.format( "Expecting one less range enrtry to fee, but given: %s and: %s", range.length,
							percentageFee.length ) );
		}
	}

	@Override
	public BigDecimal calculateFee( final BigDecimal numberOfEquities, final ClosingPrice singleEquityValue,
			final Period durationToCalculate ) {

		final BigDecimal holdingsValue = numberOfEquities.multiply( singleEquityValue.getPrice(), mathContext );
		BigDecimal fee = BigDecimal.ZERO;
		BigDecimal topEndOfLastRange = BigDecimal.ZERO;

		for (int i = 0; i < range.length; i++) {

			final BigDecimal spread;

			if (holdingsValue.compareTo( range[i] ) < 0) {
				// The the holdings is below the top end of this range
				spread = holdingsValue.subtract( topEndOfLastRange, mathContext );
				i = range.length;
			} else {
				// Holdings cover all this range (preceeding ones too)
				spread = range[i].subtract( topEndOfLastRange, mathContext );
			}

			fee = fee.add( spread.multiply( percentageFee[i], mathContext ), mathContext );
			topEndOfLastRange = range[i];
		}

		// Cater for the open ended flat fee section
		if (range[range.length - 1].compareTo( holdingsValue ) < 0) {
			final BigDecimal flatRate = holdingsValue.subtract( range[range.length - 1], mathContext )
					.multiply( percentageFee[range.length], mathContext );
			fee = fee.add( flatRate, mathContext );
		}

		return fee;
	}
}
