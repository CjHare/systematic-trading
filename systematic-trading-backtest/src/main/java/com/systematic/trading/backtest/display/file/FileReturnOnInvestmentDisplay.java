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
package com.systematic.trading.backtest.display.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;

/**
 * Outputs the ROI to the console.
 * 
 * @author CJ Hare
 */
public class FileReturnOnInvestmentDisplay implements ReturnOnInvestmentEventListener {

	enum RETURN_ON_INVESTMENT_DISPLAY {
		DAILY,
		MONTHLY,
		YEARLY,
		ALL;
	}

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( FileReturnOnInvestmentDisplay.class );

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( "#.##" );

	private final RETURN_ON_INVESTMENT_DISPLAY roiType;

	private final String outputFilename;

	/** Pool of execution threads to delegate IO operations. */
	private final ExecutorService pool;

	public FileReturnOnInvestmentDisplay( final String outputFilename, final RETURN_ON_INVESTMENT_DISPLAY roiType,
			final ExecutorService pool ) {
		this.outputFilename = outputFilename;
		this.roiType = roiType;
		this.pool = pool;

		final File outputFile = new File( outputFilename );
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
	}

	public String createOutput( final ReturnOnInvestmentEvent event ) {

		final StringBuilder output = new StringBuilder();
		final BigDecimal percentageChange = event.getPercentageChange();
		final LocalDate startDateInclusive = event.getExclusiveStartDate();
		final LocalDate endDateExclusive = event.getInclusiveEndDate();

		final String formattedPercentageChange = TWO_DECIMAL_PLACES.format( percentageChange );
		final Period elapsed = Period.between( startDateInclusive, endDateExclusive );

		if (isDailyRoiOutput( elapsed )) {
			output.append( String.format( "Daily - ROI: %s percent over %s day(s), from %s to %s\n",
					formattedPercentageChange, elapsed.getDays(), startDateInclusive, endDateExclusive ) );
		}

		if (isMonthlyRoiOutput( elapsed )) {
			output.append( String.format( "Monthly - ROI: %s percent over %s month(s), from %s to %s\n",
					formattedPercentageChange, getRoundedMonths( elapsed ), startDateInclusive, endDateExclusive ) );
		}

		if (isYearlyRoiOutput( elapsed )) {
			output.append( String.format( "Yearly - ROI: %s percent over %s year(s), from %s to %s\n",
					formattedPercentageChange, getRoundedYears( elapsed ), startDateInclusive, endDateExclusive ) );
		}

		return output.toString();
	}

	private boolean isDailyRoiOutput( final Period elapsed ) {
		switch (roiType) {
			case ALL:
			case DAILY:
				return hasMostlyDays( elapsed );
			default:
				return false;
		}
	}

	private boolean hasMostlyDays( final Period elapsed ) {
		return elapsed.getDays() > 0 && getRoundedMonths( elapsed ) == 0 && getRoundedYears( elapsed ) == 0;
	}

	private boolean isMonthlyRoiOutput( final Period elapsed ) {
		switch (roiType) {
			case ALL:
			case MONTHLY:
				return hasMostlyMonths( elapsed );
			default:
				return false;
		}
	}

	private boolean hasMostlyMonths( final Period elapsed ) {
		return getRoundedMonths( elapsed ) > 0 && getRoundedYears( elapsed ) == 0;
	}

	private int getRoundedMonths( final Period elapsed ) {
		return elapsed.getDays() > 20 ? elapsed.getMonths() + 1 : elapsed.getMonths();
	}

	private boolean isYearlyRoiOutput( final Period elapsed ) {
		switch (roiType) {
			case ALL:
			case YEARLY:
				return getRoundedYears( elapsed ) > 0;
			default:
				return false;
		}
	}

	private int getRoundedYears( final Period elapsed ) {
		return elapsed.getDays() > 20 && elapsed.getMonths() == 11 ? elapsed.getYears() + 1 : elapsed.getYears();
	}

	@Override
	public void event( final ReturnOnInvestmentEvent event ) {

		final Runnable task = ( ) -> {
			try (final PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( outputFilename, true ) ) )) {
				out.print( createOutput( (ReturnOnInvestmentEvent) event ) );
			} catch (final IOException e) {
				LOG.error( e );
			}
		};

		pool.execute( task );
	}
}
