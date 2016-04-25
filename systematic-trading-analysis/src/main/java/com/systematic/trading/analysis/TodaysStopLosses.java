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
package com.systematic.trading.analysis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.Validator;
import com.systematic.trading.maths.indicator.atr.AverageTrueRange;
import com.systematic.trading.maths.indicator.atr.AverageTrueRangeCalculator;
import com.systematic.trading.signals.model.TradingDayPricesDateOrder;

public class TodaysStopLosses {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final int ONE = 1;

	/** Calendar days in a week.*/
	private static final int DAYS_IN_WEEK = 7;

	/** Business days in a week.*/
	private static final int DAYS_IN_WORKING_WEEK = 5;

	/** Days of look back used by the average true range indicator.*/
	private static final int DAYS_ATR_LOOKBACK = 10;

	/** Interested in signals from the past few days.*/
	private static final int DAYS_OF_INTEREST = 5;

	public static void main( final String... args ) throws ServiceException {

		final int tradingDaysRequired = DAYS_ATR_LOOKBACK + DAYS_OF_INTEREST;

		updateEquities(tradingDaysRequired);

		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = getStartDate(endDate, tradingDaysRequired);

		averageTrueRange(startDate, endDate);

		HibernateUtil.getSessionFactory().close();
	}

	private static void updateEquities( final int tradingDaysRequired ) throws ServiceException {
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = getStartDate(endDate, tradingDaysRequired);

		for (final EquityHeld equity : EquityHeld.values()) {
			updateService.get(equity.getSymbol(), startDate, endDate);
		}
	}

	private static void averageTrueRange( final LocalDate startDate, final LocalDate endDate ) {
		final DataService service = HibernateDataService.getInstance();
		final Validator validator = new IllegalArgumentThrowingValidator();
		final BigDecimal threeQuaterMultiplier = BigDecimal.valueOf(3.25);
		final BigDecimal threeHalfMultiplier = BigDecimal.valueOf(3.5);

		final DecimalFormat twoDecimalPlaces = new DecimalFormat();
		twoDecimalPlaces.setMaximumFractionDigits(2);
		twoDecimalPlaces.setMinimumFractionDigits(0);
		twoDecimalPlaces.setGroupingUsed(false);

		for (final EquityHeld equity : EquityHeld.values()) {
			final String symbol = equity.getSymbol();
			final TradingDayPrices[] data = service.get(symbol, startDate, endDate);
			final AverageTrueRange atr = new AverageTrueRangeCalculator(DAYS_ATR_LOOKBACK, validator, MATH_CONTEXT);

			// Correct the ordering from earliest to latest
			Arrays.sort(data, new TradingDayPricesDateOrder());

			final List<BigDecimal> averageTrueRanges = atr.atr(data);
			final int lastAtrValue = averageTrueRanges.size() - 1;
			final BigDecimal averageTrueRange = averageTrueRanges.get(lastAtrValue);

			System.out.println("--- Stop Loss Prices on " + endDate);
			System.out.println(symbol);
			System.out.println(twoDecimalPlaces.format(data[lastAtrValue].getClosingPrice().getPrice()
			        .subtract(averageTrueRange.multiply(threeQuaterMultiplier, MATH_CONTEXT), MATH_CONTEXT)));
			System.out.println(twoDecimalPlaces.format(data[lastAtrValue].getClosingPrice().getPrice()
			        .subtract(averageTrueRange.multiply(threeHalfMultiplier, MATH_CONTEXT), MATH_CONTEXT)));
			System.out.println("--- ");
		}
	}

	/**
	 * @param endDate the inclusive final date, what will be tradingDaysRequired after the start date.
	 */
	private static LocalDate getStartDate( final LocalDate endDate, final int tradingDaysRequired ) {

		// At least one week, account for bank holidays / integer rounding.
		final int numberOfWeeks = ONE + tradingDaysRequired / DAYS_IN_WORKING_WEEK;
		final int tradingDaysIncludingWeekends = DAYS_IN_WEEK * numberOfWeeks;
		final LocalDate startDate = endDate.minus(tradingDaysIncludingWeekends, ChronoUnit.DAYS);

		return startDate;
	}
}
