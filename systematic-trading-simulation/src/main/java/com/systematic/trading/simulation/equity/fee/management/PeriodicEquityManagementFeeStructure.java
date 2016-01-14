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
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.simulation.equity.fee.EquityManagementFeeStructure;

/**
 * Management cost associated with holding the equity applied periodically.
 * 
 * @author CJ Hare
 */
public class PeriodicEquityManagementFeeStructure implements EquityManagementFeeStructure {

	/** Context to apply to calculations. */
	private final MathContext mathContext;

	/** How often the management fee is applied. */
	private final Period frequency;

	/** The percentage of the fee, or the percentage of the holdings taken as the fee. */
	private final BigDecimal feePercentage;

	/**
	 * @param feePercentage percentage of the holdings taken as the fee.
	 * @param frequency how often the management fee is applied.
	 * @param mathContext context to apply to calculations.
	 */
	public PeriodicEquityManagementFeeStructure( final BigDecimal feePercentage, final Period frequency,
			final MathContext mathContext ) {
		this.feePercentage = feePercentage;
		this.mathContext = mathContext;
		this.frequency = frequency;
	}

	@Override
	public BigDecimal update( final BigDecimal numberOfEquities, final LocalDate lastManagementFeeDate,
			final LocalDate tradingDate ) {

		if (lastManagementFeeDate.plus( frequency ).isBefore( tradingDate )) {
			return numberOfEquities.multiply( feePercentage, mathContext );
		}

		return BigDecimal.ZERO;
	}

	@Override
	public LocalDate getLastManagementFeeDate( final LocalDate tradingDate ) {

		// TODO implement this correctly - use a template for the day & month & year

		return tradingDate;
	}
}
