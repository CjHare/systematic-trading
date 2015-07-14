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
package com.systematic.trading.analysis;

/**
 * The equities of interest.
 * 
 * @author CJ Hare
 */
public enum Equity {

	ETF_OIL( "OOO.AX" ),
	ETF_GOLD( "GOLD.AX" ),
	ETF_SILVER( "ETPMAG.AX" ),
	ETF_USD( "USD.AX" ),
	ETF_CHINA( "IZZ.AX" ),
	ETF_JAPAN( "IJP.AX" ),
	ETF_HONG_KONG( "IHK.AX" ),
	ETF_SINGAPORE( "ISG.AX" ),
	ETF_SOUTH_KOREA( "IKO.AX" ),
	ETF_AUSTRALIA( "VAS.AX" ),
	ETF_USA( "VTS.AX" ),
	ETF_AUSTRALIA_SHORT( "BEAR.AX" ),
	ETF_AUSTRALIAN_BONDS( "VAF.AX" ),
    ETF_GLOBAL( "VGS.AX" ),
;

	Equity( final String symbol ) {
		this.symbol = symbol;
	}

	private final String symbol;

	public String getSymbol() {
		return symbol;
	}
}
