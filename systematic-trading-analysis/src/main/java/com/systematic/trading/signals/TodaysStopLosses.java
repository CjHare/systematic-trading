/**
 * Copyright (c) 2015, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.signals;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceImpl;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.maths.exception.TooFewDataPoints;
import com.systematic.trading.maths.indicator.AverageTrueRange;
import com.systematic.trading.signals.model.DataPointComparator;

public class TodaysStopLosses {

	private static final Logger LOG = LogManager.getLogger( TodaysStopLosses.class );

	/* Days data needed - 20 + 20 for the MACD part EMA(20), Weekend and bank holidays */
	private static final int HISTORY_REQUIRED = 50 + 16 + 5;

	public static void main( final String... args ) {

		updateEquities();

		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus( HISTORY_REQUIRED, ChronoUnit.DAYS );

		averageTrueRange( startDate, endDate );

		HibernateUtil.getSessionFactory().close();
	}

	private static void updateEquities() {
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		for (final Equity equity : Equity.values()) {
			updateService.get( equity.getSymbol() );
		}
	}

	private static void averageTrueRange( final LocalDate startDate, final LocalDate endDate ) {
		final DataService service = DataServiceImpl.getInstance();
		final AverageTrueRange atr = new AverageTrueRange( 14 );
		final BigDecimal threeQuaterMultiplier = BigDecimal.valueOf( 3.25 );
		final BigDecimal threeHalfMultiplier = BigDecimal.valueOf( 3.5 );

		final DecimalFormat twoDecimalPlaces = new DecimalFormat();
		twoDecimalPlaces.setMaximumFractionDigits( 2 );
		twoDecimalPlaces.setMinimumFractionDigits( 0 );
		twoDecimalPlaces.setGroupingUsed( false );

		for (final EquityHeld equity : EquityHeld.values()) {
			final String symbol = equity.getSymbol();
			final DataPoint[] data = service.get( symbol, startDate, endDate );

			// Correct the ordering from earliest to latest
			Arrays.sort( data, new DataPointComparator() );

			try {
				final BigDecimal[] averageTrueRanges = atr.atr( data );
				final int lastDate = averageTrueRanges.length - 1;
				final BigDecimal averageTrueRange = averageTrueRanges[lastDate];

				// TODO 2dp
				System.out.println( "--- " );
				System.out.println( symbol );
				System.out.println( twoDecimalPlaces.format( data[lastDate].getClosingPrice().subtract(
						averageTrueRange.multiply( threeQuaterMultiplier ) ) ) );
				System.out.println( twoDecimalPlaces.format( data[lastDate].getClosingPrice().subtract(
						averageTrueRange.multiply( threeHalfMultiplier ) ) ) );
				System.out.println( "--- " );

			} catch (final TooFewDataPoints e) {
				LOG.error( String.format( "Too few data points for: %s", equity.getSymbol() ), e );
			}
		}
	}
}
