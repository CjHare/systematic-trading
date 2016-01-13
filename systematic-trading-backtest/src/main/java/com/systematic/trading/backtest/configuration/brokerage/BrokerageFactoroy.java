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
package com.systematic.trading.backtest.configuration.brokerage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.MathContext;
import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.SingleEquityClassBroker;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeStructure;

/**
 * Creates the brokerage instances for use in simulation.
 * 
 * @author CJ Hare
 */
public class BrokerageFactoroy {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( BrokerageFactoroy.class );

	/**
	 * Create an instance of the fee structure.
	 */
	private static BrokerageTransactionFeeStructure createFeeStructure( final BrokerageFeesConfiguration fee,
			final MathContext mathContext ) {

		try {
			Constructor<?> cons = fee.getType().getConstructor( MathContext.class );
			return (BrokerageTransactionFeeStructure) cons.newInstance( mathContext );
		} catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error( e );
		}

		throw new IllegalArgumentException( String.format( "Could not create the desired fee structure: %s", fee ) );
	}

	public static Brokerage create( final EquityConfiguration equity, final BrokerageFeesConfiguration fees,
			final LocalDate startDate, final MathContext mathContext ) {

		final BrokerageTransactionFeeStructure tradingFeeStructure = createFeeStructure( fees, mathContext );
		final EquityManagementFeeStructure equityManagementFee = equity.getManagementFee();
		final EquityClass equityType = equity.getIdentity().getType();

		return new SingleEquityClassBroker( tradingFeeStructure, equityManagementFee, equityType, startDate,
				mathContext );
	}
}
