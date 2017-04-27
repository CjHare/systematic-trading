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
package com.systematic.trading.backtest.configuration.filter;

import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;

/**
 * Inclusive range of days relative to a signal date that a confirmation signal is required. 
 * 
 * @author CJ Hare
 */
public class ConfirmationSignalFilterConfiguration {

	public enum Type {
		NO_DELAY_RANGE_THREE_DAYS(0, 3),
		DELAY_ONE_DAY_RANGE_THREE_DAYS(1, 3),
		DELAY_ONE_DAY_RANGE_FOUR_DAYS(1, 4);

		private final int confirmationDayRange;
		private final int delayUntilConfirmationRange;

		Type( final int delayUntilConfirmationRange, final int confirmationDayRange ) {
			this.confirmationDayRange = confirmationDayRange;
			this.delayUntilConfirmationRange = delayUntilConfirmationRange;
		}

		public int getConfirmationDayRange() {
			return confirmationDayRange;
		}

		public int getDelayUntilConfirmationRange() {
			return delayUntilConfirmationRange;
		}
	}

	private final SignalConfiguration anchor;
	private final SignalConfiguration confirmation;
	private final Type type;

	public ConfirmationSignalFilterConfiguration( final Type type, final SignalConfiguration anchor,
	        SignalConfiguration confirmation ) {
		this.type = type;
		this.anchor = anchor;
		this.confirmation = confirmation;
	}

	public SignalConfiguration getAnchor() {
		return anchor;
	}

	public SignalConfiguration getConfirmation() {
		return confirmation;
	}

	public Type getType() {
		return type;
	}
}