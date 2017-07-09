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
package com.systematic.trading.signals.quandl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.systematic.trading.data.stock.api.exception.CannotRetrieveDataException;

/**
 * Invocation of the JSON HTTP GET Quandl endpoint.
 * 
 * @author CJ Hare
 */
public class QuandlAPI {

	//TODO check if up
	//TODO test call

	//TODO code

	//TODO use Jackson provider to connect - need to pass query string parameters.
	
	public String get( final String url ) throws CannotRetrieveDataException {
		final HttpResponse response = sendRequest(url);
		checkStatus(url, response);
		return parseResponse(url, response);
	}

	private HttpResponse sendRequest( final String url ) throws CannotRetrieveDataException {
		
		
		//TODO inject the HttpClientBuilder & test (as the injection would allow mocking)
		try {
			return HttpClientBuilder.create().build().execute(jsonGetRequest(url));
		} catch (ClientProtocolException e) {
			throw new CannotRetrieveDataException(String.format("Protocol issue retrieving URL: %s", url), e);
		} catch (IOException e) {
			throw new CannotRetrieveDataException(String.format("Failed retrieving URL: %s", url), e);
		}
	}

	private void checkStatus( final String url, final HttpResponse response ) throws CannotRetrieveDataException {
		if (hasHttpIssue(response)) {
			throw new CannotRetrieveDataException(String.format("Failed retrieving URL: %s, HTTP error code : %s", url,
			        response.getStatusLine().getStatusCode()));
		}
	}

	private boolean hasHttpIssue( final HttpResponse response ) {
		return response.getStatusLine().getStatusCode() != HttpStatus.SC_OK;
	}

	private String parseResponse( final String url, final HttpResponse response ) throws CannotRetrieveDataException {
		final StringBuilder result = new StringBuilder();

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String output;
			while ((output = br.readLine()) != null) {
				result.append(output);
			}

		} catch (final IOException e) {
			throw new CannotRetrieveDataException(String.format("Problem parsing response for URL: %s", url), e);
		}

		return result.toString();
	}

	private HttpGet jsonGetRequest( final String url ) {
		final HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("accept", "application/json");
		return getRequest;
	}
}