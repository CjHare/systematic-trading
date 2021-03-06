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
package com.systematic.trading.maths.indicator.rsi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthIndicator;
import com.systematic.trading.maths.indicator.rs.RelativeStrengthLine;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * Relative Strength Index - RSI
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexCalculator implements RelativeStrengthIndexIndicator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Constant for the value of 100. */
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	/** Number of prices required to evaluate the RSI movement. */
	private static final int MINIMUM_NUMBER_OF_PRICES = 2;

	/** Responsible for parsing and validating the input. */
	private final Validator validator;

	/** Creates the relative strength values to convert into RSI values. */
	private final RelativeStrengthIndicator rs;

	/**
	 * @param validator
	 *            validates and parses input.
	 */
	public RelativeStrengthIndexCalculator( final RelativeStrengthIndicator rs, final Validator validator ) {

		this.validator = validator;
		this.rs = rs;
	}

	@Override
	public int minimumNumberOfPrices() {

		return MINIMUM_NUMBER_OF_PRICES + rs.minimumNumberOfPrices();
	}

	@Override
	public RelativeStrengthIndexLine calculate( final TradingDayPrices[] data ) {

		validator.verifyNotNull(data);
		validator.verifyZeroNullEntries(data);
		validator.verifyEnoughValues(data, rs.minimumNumberOfPrices());

		final RelativeStrengthLine rsLine = rs.calculate(data);
		final SortedMap<LocalDate, BigDecimal> rsi = new TreeMap<>();

		for (final Map.Entry<LocalDate, BigDecimal> entry : rsLine.rs().entrySet()) {
			rsi.put(entry.getKey(), rsi(entry));
		}

		return new RelativeStrengthIndexLine(rsi);
	}

	/*
	 * RSI = 100 - 100 /( 1 + RS )
	 */
	private BigDecimal rsi( final Map.Entry<LocalDate, BigDecimal> entry ) {

		return ONE_HUNDRED.subtract(ONE_HUNDRED.divide(BigDecimal.ONE.add(entry.getValue()), MATH_CONTEXT));
	}
}
