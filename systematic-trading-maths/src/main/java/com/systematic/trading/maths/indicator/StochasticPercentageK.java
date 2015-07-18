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
package com.systematic.trading.maths.indicator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.systematic.trading.data.DataPoint;

/**
 * %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
 * 
 * @author CJ Hare
 */
public class StochasticPercentageK {

	/** Number of decimal places for scaling. */
	private static final int ROUNDING_SCALE = 2;

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );
	private static final BigDecimal ONE_HUNDREDTH = BigDecimal.valueOf( 0.01 );

	/** Number of days to read the ranges on. */
	private final int lookback;

	public StochasticPercentageK( final int lookback ) {
		this.lookback = lookback;
	}

	public BigDecimal[] percentageK( final DataPoint[] data ) {
		final BigDecimal[] pK = new BigDecimal[data.length];
		BigDecimal lowestLow, highestHigh, currentClose, lowestHighestDifference;

		for (int i = lookback; i < data.length; i++) {
			currentClose = data[i].getClosingPrice();
			lowestLow = lowestLow( data, i );
			highestHigh = highestHigh( data, i );
			lowestHighestDifference = differenceBetweenHighestHighAndLowestLow( lowestLow, highestHigh );

			// %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100
			pK[i] = ((currentClose.subtract( lowestLow )).divide( lowestHighestDifference, ROUNDING_SCALE,
					RoundingMode.HALF_UP )).multiply( ONE_HUNDRED );
		}

		return pK;
	}

	private BigDecimal differenceBetweenHighestHighAndLowestLow( final BigDecimal lowestLow,
			final BigDecimal highestHigh ) {
		if (lowestLow.compareTo( highestHigh ) == 0) {
			return ONE_HUNDREDTH;
		}

		return highestHigh.subtract( lowestLow );
	}

	private BigDecimal lowestLow( final DataPoint[] data, final int inclusiveStart ) {
		BigDecimal contender, lowest = data[inclusiveStart].getLowestPrice();

		for (int i = inclusiveStart - lookback; i < inclusiveStart; i++) {
			contender = data[i].getLowestPrice();
			if (contender.compareTo( lowest ) < 0) {
				lowest = contender;
			}
		}

		return lowest;
	}

	private BigDecimal highestHigh( final DataPoint[] data, final int inclusiveStart ) {
		BigDecimal contender, highest = data[inclusiveStart].getHighestPrice();

		for (int i = inclusiveStart - lookback; i < inclusiveStart; i++) {
			contender = data[i].getHighestPrice();
			if (contender.compareTo( highest ) > 0) {
				highest = contender;
			}
		}

		return highest;
	}
}
