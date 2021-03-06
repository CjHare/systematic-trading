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
package com.systematic.trading.simulation.order;

import java.math.BigDecimal;

/**
 * The number of equities.
 * 
 * @author CJ Hare
 */
public class EquityOrderVolume {

	/** Prime number to help keep the hash code unique. */
	private static final int PRIME_VALUE = 31;

	private final BigDecimal volume;

	private EquityOrderVolume( final BigDecimal volume ) {

		this.volume = volume;
	}

	/**
	 * Creates a volume of an order from a decimal.
	 * 
	 * @param volume
	 *            decimal to create the volume from, cannot be <code>null</code>.
	 * @return equivalent volume.
	 */
	public static EquityOrderVolume valueOf( final BigDecimal volume ) {

		if (volume == null) { throw new IllegalArgumentException("null is not accepted by OrderVolume.valueOf()"); }

		return new EquityOrderVolume(volume);
	}

	/**
	 * Retrieves the number of equities.
	 * 
	 * @return volume of the order, never <code>null</code>.
	 */
	public BigDecimal volume() {

		return volume;
	}

	@Override
	public String toString() {

		return String.valueOf(volume);
	}

	@Override
	public boolean equals( final Object obj ) {

		return obj instanceof EquityOrderVolume && volume().equals(((EquityOrderVolume) obj).volume());
	}

	@Override
	public int hashCode() {

		int result = 1;
		result = PRIME_VALUE * result + ((volume == null) ? 0 : volume.hashCode());
		return result;
	}
}
