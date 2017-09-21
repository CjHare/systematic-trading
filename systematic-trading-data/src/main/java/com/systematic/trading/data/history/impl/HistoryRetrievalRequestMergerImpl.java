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

import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

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
	 * Will only merge requests, no slicing or splitting will occur.
	 */
	public List<HistoryRetrievalRequest> merge( final List<HistoryRetrievalRequest> unsortedRequests,
	        final Period maximum ) {
		if (hasInsufficentRequestsToMerge(unsortedRequests) || isInvalid(maximum)) {
			return unsortedRequests;
		}

		return mergeRequests(sortByStartDate(unsortedRequests), maximum);
	}

	private List<HistoryRetrievalRequest> mergeRequests( final List<HistoryRetrievalRequest> sortedRequests,
	        final Period maximum ) {
		final List<HistoryRetrievalRequest> mergedRequests = new ArrayList<>(sortedRequests.size());
		HistoryRetrievalRequestBuilder mergingRequests = resetBuilder(sortedRequests.get(0));
		final int numberOfRequests = sortedRequests.size() - 1;
		Period remaining = maximum;

		for (int i = 0; i < numberOfRequests; i++) {
			final Optional<HistoryRetrievalRequest> lastRequest = getLastRequest(i, sortedRequests);
			final HistoryRetrievalRequest request = sortedRequests.get(i);
			final HistoryRetrievalRequest nextRequest = getNextRequest(i, sortedRequests);

			final Triple<Optional<HistoryRetrievalRequest>, Period, HistoryRetrievalRequestBuilder> result = mergeRequest(
			        maximum, lastRequest, request, nextRequest, remaining, mergingRequests);

			final Optional<HistoryRetrievalRequest> a = result.getLeft();
			if (a.isPresent()) {
				mergedRequests.add(a.get());
			}

			remaining = result.getMiddle();
			mergingRequests = result.getRight();
		}

		mergedRequests.add(createLastRequest(sortedRequests, mergingRequests));

		return mergedRequests;
	}

	private Triple<Optional<HistoryRetrievalRequest>, Period, HistoryRetrievalRequestBuilder> mergeRequest(
	        final Period maximum, final Optional<HistoryRetrievalRequest> lastRequest,
	        final HistoryRetrievalRequest request, final HistoryRetrievalRequest nextRequest, Period remaining,
	        final HistoryRetrievalRequestBuilder mergingRequests ) {
		final Period requestLength = getRequestLength(request);
		HistoryRetrievalRequestBuilder nextMergingRequests = mergingRequests;
		HistoryRetrievalRequest created = null;
		Period nextRequestRemaining;

		if (areConsecutive(request, nextRequest)) {
			if (hasEnoughTime(requestLength, remaining)) {

				// Decrement the remaining time by this request's
				nextRequestRemaining = remaining.minus(requestLength);

				if (nextRequestRemaining.isZero()) {
					created = createRequest(request, mergingRequests);

					// Reset the  time for the next request to merge, update the start date
					nextRequestRemaining = maximum;
					nextMergingRequests = resetBuilder(nextRequest);
				}
			} else {

				// Insufficient time to merge the entire next record
				if (lastRequest.isPresent()) {
					created = createRequest(lastRequest.get(), mergingRequests);
					nextMergingRequests = resetBuilder(request);
				}

				created = createRequest(request, mergingRequests);
				nextRequestRemaining = maximum;
				nextMergingRequests = resetBuilder(nextRequest);

			}
		} else {
			// Break in the consecutive chain
			created = createRequest(request, mergingRequests);
			nextRequestRemaining = maximum;
			nextMergingRequests = resetBuilder(nextRequest);
		}

		return new ImmutableTriple<>(created == null ? Optional.empty() : Optional.of(created), nextRequestRemaining,
		        nextMergingRequests);
	}

	private HistoryRetrievalRequest createRequest( final HistoryRetrievalRequest request,
	        final HistoryRetrievalRequestBuilder mergedRequest ) {
		return mergedRequest.withExclusiveEndDate(request.getExclusiveEndDate()).build();
	}

	private HistoryRetrievalRequest createLastRequest( final List<HistoryRetrievalRequest> sortedRequests,
	        final HistoryRetrievalRequestBuilder mergedRequest ) {
		return mergedRequest.withExclusiveEndDate(sortedRequests.get(sortedRequests.size() - 1).getExclusiveEndDate())
		        .build();
	}

	private boolean hasInsufficentRequestsToMerge( final List<HistoryRetrievalRequest> unsortedRequests ) {
		return unsortedRequests == null || unsortedRequests.isEmpty() || unsortedRequests.size() == 1;
	}

	private boolean isInvalid( final Period maximum ) {
		return maximum == null || maximum.isZero();
	}

	private Optional<HistoryRetrievalRequest> getLastRequest( final int i,
	        final List<HistoryRetrievalRequest> requests ) {
		return i > 0 ? Optional.of(requests.get(i - 1)) : Optional.empty();
	}

	private HistoryRetrievalRequest getNextRequest( final int i, final List<HistoryRetrievalRequest> requests ) {
		return requests.get(i + 1);
	}

	/**
	 * Inclusive period i.e. from the inclusive start to the day before the exclusive end.
	 */
	private Period getRequestLength( final HistoryRetrievalRequest request ) {
		return Period.between(request.getInclusiveStartDate().toLocalDate(),
		        request.getExclusiveEndDate().toLocalDate());
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
}