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
package com.systematic.trading.backtest.output.elastic.model;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Behaviour  common for indexes put into Elastic Search
 * 
 * @author CJ Hare
 */
public abstract class ElasticCommonIndex {

	protected abstract ElasticIndex getIndex();

	protected abstract ElasticIndexName getName();

	public void init( final WebTarget root ) {

		if (isIndexMissing(root)) {
			createIndex(root);
		}
	}

	private boolean isIndexMissing( final WebTarget root ) {

		final String indexName = getName().getName();
		final Response response = root.path(indexName).request(MediaType.APPLICATION_JSON).get();

		System.out.println("Response code: " + response.getStatus());
		System.out.println("Response :" + response.readEntity(String.class));

		//TODO verify index has structure expected (index object is returned
		
		return response.getStatus() != 200;
	}

	private void createIndex( final WebTarget root ) {

		final ElasticIndex index = getIndex();
		final String indexName = getName().getName();
		final Response response = root.path(indexName).request(MediaType.APPLICATION_JSON).put(Entity.json(index));

		System.out.println("Response code: " + response.getStatus());
		System.out.println("Response :" + response.readEntity(String.class));

		//TODO get the index & only create if it does not exist

		//TODO parse the response

	}
}