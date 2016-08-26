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
package com.systematic.trading.maths.indicator.rsi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import com.systematic.trading.collection.NonNullableArrayList;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.formula.rs.RelativeStrength;
import com.systematic.trading.maths.formula.rs.RelativeStrengthDataPoint;
import com.systematic.trading.maths.indicator.Validator;

/**
 * Relative Strength Index - RSI
 * 
 * A technical momentum indicator that compares the magnitude of recent gains to recent losses in an
 * attempt to determine over bought and over sold conditions of an asset.
 * 
 * RSI = 100 - 100/(1 + RS*)
 * 
 * Where RS = Average of x days' up closes / Average of x days' down closes.
 * 
 * Uses the EMA in calculation of the relative strength (J. Welles Wilder approach), not the SMA.
 * 
 * Taking the prior value plus the current value is a smoothing technique similar to that used in
 * exponential moving average calculation. This also means that RSI values become more accurate as
 * the calculation period extends.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexCalculator implements RelativeStrengthIndex {

	//TODO fix this class, test against, flat, v shape, n shape as well as both gradients

	/** Constant for the value of 100. */
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** Creates the relative strength values to convert into RSI values. */
	private final RelativeStrength rs;

	/**
	 * @param validator validates and parses input.
	 * @param mathContext the scale, precision and rounding to apply to mathematical operations.
	 */
	public RelativeStrengthIndexCalculator(final RelativeStrength rs, final Validator validator,
	        final MathContext mathContext) {
		this.mathContext = mathContext;
		this.validator = validator;
		this.rs = rs;
	}

	@Override
	public List<RelativeStrengthIndexDataPoint> rsi( final TradingDayPrices[] data ) {
		final List<RelativeStrengthDataPoint> relativeStrengthValues = rs.rs(data);
		validator.verifyZeroNullEntries(data);

		final List<RelativeStrengthIndexDataPoint> relativeStrengthIndexValues = new NonNullableArrayList<>(
		        relativeStrengthValues.size());

		/* RSI = 100 - 100 /( 1 + RS ) */
		for (final RelativeStrengthDataPoint dataPoint : relativeStrengthValues) {
			relativeStrengthIndexValues.add(new RelativeStrengthIndexDataPoint(dataPoint.getDate(),
			        ONE_HUNDRED.subtract(ONE_HUNDRED.divide(BigDecimal.ONE.add(dataPoint.getValue()), mathContext))));
		}

		return relativeStrengthIndexValues;
	}
}