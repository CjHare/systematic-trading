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
package com.systematic.trading.backtest.output.elastic.model.index;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.model.ElasticCommonIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldName;
import com.systematic.trading.backtest.output.elastic.model.ElasticFieldType;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndex;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;

/**
 * Elastic Search index for signal analysis events.
 * 
 * @author CJ Hare
 */
public class ElasticSignalAnalysisIndex extends ElasticCommonIndex {

	public void event( final SignalAnalysisEvent event ) {
		// TODO Auto-generated method stub
		System.out.println("code ElasticSignalAnalysisIndex");

	}

	@Override
	protected ElasticIndex getIndex( final BacktestBatchId id ) {

		final ElasticFieldName fieldName = ElasticFieldName.EVENT;
		final ElasticFieldType fieldType = ElasticFieldType.TEXT;

		//TODO create the index appropriate for the event bean

		final List<Pair<ElasticFieldName, ElasticFieldType>> fields = Arrays
		        .asList(new ImmutablePair<ElasticFieldName, ElasticFieldType>(fieldName, fieldType));

		return new ElasticIndex(id.getName(), fields);
	}

	@Override
	protected ElasticIndexName getIndexName() {
		return ElasticIndexName.SIGNAL_ANALYSIS;
	}
}