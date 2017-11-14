/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.configuration.equity;

import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;

/**
 * Supported configurations of equities for back testing.
 * 
 * @author CJ Hare
 */
public class EquityConfiguration {

	private static final int SCALE = 4;

	private final EquityDataset equityDataset;
	private final TickerSymbol tickerSymbol;
	private final EquityClass equityType;
	private final EquityManagementFeeConfiguration managementFee;

	public EquityConfiguration( final EquityDataset dataset, final TickerSymbol tickerSymbol,
	        final EquityClass equityType ) {
		this.equityDataset = dataset;
		this.tickerSymbol = tickerSymbol;
		this.equityType = equityType;
		this.managementFee = EquityManagementFeeConfiguration.NONE;
	}

	public EquityIdentity getEquityIdentity() {
		return new EquityIdentity(tickerSymbol.getSymbol(), equityType, SCALE);
	}

	public EquityManagementFeeConfiguration getManagementFee() {
		return managementFee;
	}

	public String getEquityDataset() {
		return equityDataset == null ? null : equityDataset.getDataset();
	}
}