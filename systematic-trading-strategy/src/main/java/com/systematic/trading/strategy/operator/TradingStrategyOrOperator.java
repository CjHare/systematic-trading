/**
 * Copyright (c) 2015-2017, CJ Hare
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
package com.systematic.trading.strategy.operator;

import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.signal.model.DatedSignal;

/**
 * Trading strategy logical OR operator is used to combine exits and entries.
 * 
 * @author CJ Hare
 */
public class TradingStrategyOrOperator implements Operator {

	@Override
	public List<DatedSignal> conjoin( final List<DatedSignal> left, final List<DatedSignal> right ) {

		final List<DatedSignal> either = new ArrayList<>(left.size() + right.size());
		either.addAll(left);

		for (final DatedSignal conteder : right) {

			// Only one copy of the signal
			if (doesNotContain(left, conteder)) {
				either.add(conteder);
			}
		}

		return either;
	}

	//TODO natrual ordering to DatedSignal & replace with set add
	private boolean doesNotContain( final List<DatedSignal> left, final DatedSignal contender ) {

		for (final DatedSignal ds : left) {
			if (ds.getDate().equals(contender.getDate()) && ds.getType() == contender.getType()) {
				return false;
			}
		}

		return true;
	}
}