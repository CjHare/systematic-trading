package com.systematic.trading.signals.indicator.macd;

import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceIndicator;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signals.indicator.GenericSignalsTestBase;

/**
 * Verify the behaviour of the Moving Average Convergence Divergence.
 * 
 * @author CJ Hare
 */
public class MovingAveragingConvergeDivergenceSignalsTest
        extends GenericSignalsTestBase<MovingAverageConvergenceDivergenceLines, MovingAverageConvergenceDivergenceIndicator> {

	private static final int REQUIRED_TRADING_DAYS = 34;

	@Override
	protected int requiredNumberOfTradingDays() {
		return REQUIRED_TRADING_DAYS;
	}
}