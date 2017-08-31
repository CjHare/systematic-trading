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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.event.SignalAnalysisEvent;

/**
 * Purpose being to Verify the JSON messages to Elastic Search.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticSignalAnalysisIndexTest extends ElasticIndexTestBase {

	private static final String JSON_PUT_INDEX = "{\"settings\":{\"number_of_shards\":5,\"number_of_replicas\":1}}";
	private static final String JSON_PUT_INDEX_MAPPING = "{\"properties\":{\"signal_date\":{\"type\":\"date\"},\"signal_type\":{\"type\":\"text\"},\"direction_type\":{\"type\":\"text\"}}}";
	private static final String JSON_POST_INDEX_TYPE = "{\"signal_type\":\"STOCHASTIC\",\"direction_type\":\"BULLISH\",\"signal_date\":{";

	@Test
	public void initMissingIndex() {
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		index.init(id);

		verifyMissingIndexCalls(batchId);
	}

	@Test
	public void initPresentIndexMissingMapping() {
		setUpPresentIndex();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		index.init(id);

		verifyPresentIndexMissingMappingCalls(batchId);
	}

	@Test
	public void initPresentIndexPresentMapping() {
		setUpPresentIndex();
		setUpPresentMapping();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		try {
			index.init(id);
			fail("Expecting an Elastic Exception");
		} catch (final ElasticException e) {
			assertEquals(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", batchId),
			        e.getMessage());
		}

		verifyPresentIndexPresentMappingCalls(batchId);
	}

	@Test
	public void event() {
		setUpPresentIndex();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		final SignalAnalysisEvent event = getEvent();

		index.init(id);
		index.event(id, event);

		verifyEventCalls(batchId, event.getSignalDate());
	}

	@Test
	public void disableRefreshInterval() {
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		index.setRefreshInterval(false);

		verfiyRefreshInterval(false);
	}

	@Test
	public void enableRefreshInterval() {
		final ElasticSignalAnalysisIndex index = new ElasticSignalAnalysisIndex(getDao());

		index.setRefreshInterval(true);

		verfiyRefreshInterval(true);
	}

	@Override
	protected String getPostIndex() {
		return JSON_POST_INDEX_TYPE;
	}

	@Override
	protected String getJsonPutIndex() {
		return JSON_PUT_INDEX;
	}

	@Override
	protected String getJsonPutIndexMapping() {
		return JSON_PUT_INDEX_MAPPING;
	}

	@Override
	protected ElasticIndexName getIndexName() {
		return ElasticIndexName.SIGNAL_ANALYSIS;
	}

	private SignalAnalysisEvent getEvent() {
		final SignalAnalysisEvent event = mock(SignalAnalysisEvent.class);
		final IndicatorSignalType type = IndicatorSignalType.STOCHASTIC;
		final IndicatorDirectionType direction = IndicatorDirectionType.BULLISH;
		final LocalDate signalDate = LocalDate.now();

		when(event.getSignalType()).thenReturn(type);
		when(event.getDirectionType()).thenReturn(direction);
		when(event.getSignalDate()).thenReturn(signalDate);

		return event;
	}
}