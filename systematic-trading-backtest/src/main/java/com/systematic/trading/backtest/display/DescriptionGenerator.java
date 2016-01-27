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
package com.systematic.trading.backtest.display;

import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;

/**
 * Textually meaningful description of the configuration appropriate for display.
 * 
 * @author CJ Hare
 */
public class DescriptionGenerator {
	// TODO interface - one for file, another for console

	public String getWeeklyDescription( final BrokerageFeesConfiguration brokerage ) {
		return String.format( "%s_BuyWeekly_HoldForever", getBrokerageDescription( brokerage ) );
	}

	public String getMonthlyDescription( final BrokerageFeesConfiguration brokerage ) {
		return String.format( "%s_BuyMonthly_HoldForever", getBrokerageDescription( brokerage ) );
	}

	private String getBrokerageDescription( final BrokerageFeesConfiguration brokerage ) {
		switch (brokerage) {
			case CMC_MARKETS:
				return "CmcMarkets";
			case VANGUARD_RETAIL:
				return "VanguardRetail";
			default:
				throw new IllegalArgumentException(
						String.format( "Brokerage configurataion not catered for: %s", brokerage ) );
		}

	}

	public String getDescription( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade,
			final SignalConfiguration... configurations ) {

		switch (configurations.length) {
			case 1:
				return String.format( "%s_Minimum-%s_Maximum-%s_HoldForever", configurations[0],
						minimumTrade.getDescription(), maximumTrade.getDescription() );
			case 2:
				return String.format( "%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", configurations[0],
						configurations[1], minimumTrade.getDescription(), maximumTrade.getDescription() );
			case 3:
				return String.format( "%s-%s-%s_SameDay_Minimum-%s_Maximum-%s_HoldForever", configurations[0],
						configurations[1], configurations[2], minimumTrade.getDescription(),
						maximumTrade.getDescription() );
			default:
				throw new IllegalArgumentException(
						String.format( "Unexpected number of configurations: %s", configurations.length ) );
		}
	}
}
