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
package com.systematic.trading.data.history.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.systematic.trading.data.history.HistoryRetrievalRequestMerger;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.builder.HistoryRetrievalRequestBuilder;

/**
 * History retrieval request merger.
 * 
 * @author CJ Hare
 */
public class HistoryRetrievalRequestMergerImpl implements HistoryRetrievalRequestMerger {

	private final HistoryRetrievalRequestBuilder builder;

	public HistoryRetrievalRequestMergerImpl( final HistoryRetrievalRequestBuilder builder ) {
		this.builder = builder;
	}

	@Override
	/**
	 * Assumed all requests are for the same ticker symbol.
	 */
	public List<HistoryRetrievalRequest> merge( final List<HistoryRetrievalRequest> unsortedRequests,
	        final Period maximum ) {
		if (unsortedRequests == null || unsortedRequests.isEmpty() || unsortedRequests.size() == 1 || maximum == null) {
			return unsortedRequests;
		}

		final List<HistoryRetrievalRequest> merged = new ArrayList<HistoryRetrievalRequest>(unsortedRequests.size());
		final List<HistoryRetrievalRequest> sortedRequests = sortByStartDate(unsortedRequests);
		HistoryRetrievalRequestBuilder mergedRequest = resetBuilder(sortedRequests.get(0));
		Period remaining = maximum;

		for (int i = 0; i < sortedRequests.size() - 1; i++) {
			final HistoryRetrievalRequest request = sortedRequests.get(i);
			final HistoryRetrievalRequest nextRequest = sortedRequests.get(i + 1);
			final Period requestLength = getRequestLength(request);

			if (areConsecutive(request, nextRequest)) {
				if (hasEnoughTime(requestLength, remaining)) {

					// Decrement the remaining time by this request's
					remaining = remaining.minus(requestLength);

					if (remaining.isZero()) {
						merged.add(mergedRequest.withExclusiveEndDate(request.getExclusiveEndDate()).build());

						// Reset the  time for the next request to merge, update the start date
						remaining = maximum;
						mergedRequest = resetBuilder(nextRequest.getInclusiveStartDate());
					}
				} else {

					// Partial request covered
					final LocalDate exclusiveEndDate = getExclusiveEndDate(nextRequest, remaining);
					merged.add(mergedRequest.withExclusiveEndDate(exclusiveEndDate).build());

					// Reset the  time for the next request to merge, update the start date
					remaining = maximum;
					mergedRequest = resetBuilder(exclusiveEndDate);

				}
			} else {
				// Break in the consecutive chain
				merged.add(mergedRequest.withExclusiveEndDate(request.getExclusiveEndDate()).build());
				remaining = maximum;
				mergedRequest = resetBuilder(nextRequest.getInclusiveStartDate());
			}

		}
		
		// Create the final merged request
		merged.add(mergedRequest
		        .withExclusiveEndDate(sortedRequests.get(sortedRequests.size() - 1).getExclusiveEndDate()).build());

		return merged;
	}

	/**
	 * Inclusive period i.e. from the inclusive start to the day before the exclusive end.
	 */
	private Period getRequestLength( final HistoryRetrievalRequest request ) {
		return Period.between(request.getInclusiveStartDate().toLocalDate(),
		        request.getExclusiveEndDate().toLocalDate());
	}

	private LocalDate getExclusiveEndDate( final HistoryRetrievalRequest request, final Period remaining ) {
		return request.getInclusiveStartDate().toLocalDate().plusDays(1).plus(remaining);
	}

	private boolean areConsecutive( final HistoryRetrievalRequest first, final HistoryRetrievalRequest second ) {
		return first.getExclusiveEndDate().equals(second.getInclusiveStartDate());
	}

	private boolean hasEnoughTime( final Period requestLength, final Period remaining ) {
		return !remaining.minus(requestLength).isNegative();
	}

	private List<HistoryRetrievalRequest> sortByStartDate( final List<HistoryRetrievalRequest> requests ) {
		Collections.sort(requests, ( HistoryRetrievalRequest a, HistoryRetrievalRequest b ) -> a.getInclusiveStartDate()
		        .compareTo(b.getInclusiveStartDate()));
		return requests;
	}

	private HistoryRetrievalRequestBuilder resetBuilder( final HistoryRetrievalRequest request ) {
		return builder.withTickerSymbol(request.getTickerSymbol())
		        .withInclusiveStartDate(request.getInclusiveStartDate());
	}

	private HistoryRetrievalRequestBuilder resetBuilder( final Date inclsuiveStartDate ) {
		return builder.withInclusiveStartDate(inclsuiveStartDate);
	}

	private HistoryRetrievalRequestBuilder resetBuilder( final LocalDate inclsuiveStartDate ) {
		return builder.withInclusiveStartDate(inclsuiveStartDate);
	}
}