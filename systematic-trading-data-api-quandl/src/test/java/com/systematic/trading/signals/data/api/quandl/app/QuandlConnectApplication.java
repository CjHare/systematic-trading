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
package com.systematic.trading.signals.data.api.quandl.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Application for checking the connection and Jackson data structures used.
 * 
 * @author CJ Hare
 */
public class QuandlConnectApplication {

	private static final String EXPECTED_JSON = "{\"datatable\":{\"columns\":[{\"name\":\"date\",\"type\":\"Date\"},{\"name\":\"open\",\"type\":\"BigDecimal(34,12)\"},{\"name\":\"high\",\"type\":\"BigDecimal(34,12)\"},{\"name\":\"low\",\"type\":\"BigDecimal(34,12)\"},{\"name\":\"close\",\"type\":\"BigDecimal(34,12)\"}],\"data\":[[\"2015-01-15\",110.0,110.06,106.66,106.82],[\"2015-01-16\",107.03,107.58,105.2,105.99],[\"2015-01-20\",107.84,108.9667,106.5,108.72],[\"2015-01-21\",108.95,111.06,108.27,109.55],[\"2015-01-22\",110.26,112.47,109.72,112.4],[\"2015-01-23\",112.3,113.75,111.53,112.98],[\"2015-01-26\",113.74,114.3626,112.8,113.1],[\"2015-01-27\",112.42,112.48,109.03,109.14],[\"2015-01-28\",117.625,118.12,115.31,115.31],[\"2015-01-29\",116.32,119.19,115.56,118.9],[\"2015-01-30\",118.4,120.0,116.85,117.16],[\"2015-02-02\",118.05,119.17,116.08,118.63],[\"2015-02-03\",118.5,119.09,117.61,118.65],[\"2015-02-04\",118.5,120.51,118.309,119.56],[\"2015-02-05\",120.02,120.23,119.25,119.94],[\"2015-02-06\",120.02,120.25,118.45,118.93],[\"2015-02-09\",118.55,119.84,118.43,119.72],[\"2015-02-10\",120.17,122.15,120.16,122.02],[\"2015-02-11\",122.77,124.92,122.5,124.88],[\"2015-02-12\",126.06,127.48,125.57,126.46],[\"2015-02-13\",127.28,127.28,125.65,127.08],[\"2015-02-17\",127.49,128.88,126.92,127.83],[\"2015-02-18\",127.625,128.78,127.45,128.715],[\"2015-02-19\",128.48,129.03,128.33,128.45],[\"2015-02-20\",128.62,129.5,128.05,129.495],[\"2015-02-23\",130.02,133.0,129.66,133.0],[\"2015-02-24\",132.94,133.6,131.17,132.17],[\"2015-02-25\",131.56,131.6,128.15,128.79],[\"2015-02-26\",128.785,130.87,126.61,130.415],[\"2015-02-27\",130.0,130.57,128.24,128.46],[\"2015-03-02\",129.25,130.28,128.3,129.09],[\"2015-03-03\",128.96,129.52,128.09,129.36],[\"2015-03-04\",129.1,129.56,128.32,128.54],[\"2015-03-05\",128.58,128.75,125.76,126.41],[\"2015-03-06\",128.4,129.37,126.26,126.6],[\"2015-03-09\",127.96,129.57,125.06,127.14],[\"2015-03-10\",126.41,127.22,123.8,124.51],[\"2015-03-11\",124.75,124.77,122.11,122.24],[\"2015-03-12\",122.31,124.9,121.63,124.45],[\"2015-03-13\",124.4,125.3951,122.58,123.59],[\"2015-03-16\",123.88,124.95,122.87,124.95],[\"2015-03-17\",125.9,127.32,125.65,127.04],[\"2015-03-18\",127.0,129.16,126.37,128.47],[\"2015-03-19\",128.75,129.2451,127.4,127.495],[\"2015-03-20\",128.25,128.4,125.16,125.9],[\"2015-03-23\",127.12,127.85,126.52,127.21],[\"2015-03-24\",127.23,128.04,126.56,126.69],[\"2015-03-25\",126.54,126.82,123.38,123.38],[\"2015-03-26\",122.76,124.88,122.6,124.24],[\"2015-03-27\",124.57,124.7,122.91,123.25],[\"2015-03-30\",124.05,126.4,124.0,126.37],[\"2015-03-31\",126.09,126.49,124.355,124.43]]}}";

	private static final String QUANDL_ENDPOINT_URL = "https://www.quandl.com";

	public static void main( final String[] args ) throws JsonProcessingException {
		new QuandlConnectApplication().get();

		System.out.println("No AssertException or other RuntimeExceptions means the test connection succeeded!");
	}

	private final WebTarget root;

	public QuandlConnectApplication() {
		// Registering the provider for POJO -> JSON
		final ClientConfig clientConfig = new ClientConfig().register(JacksonJsonProvider.class);

		// End point target root
		root = ClientBuilder.newClient(clientConfig).target(QUANDL_ENDPOINT_URL);

	}

	public void get() throws JsonProcessingException {
		final WebTarget url = createUrl();
		final QuandlResponseResource inflatedJson = callQuandl(url);
		verifyResource(inflatedJson);
	}

	/**
	 * Invoke the Quandl service, parse the returned JSON as a QuandlResponseResource.
	 */
	private QuandlResponseResource callQuandl( final WebTarget url ) {
		return url.request(MediaType.APPLICATION_JSON).get(QuandlResponseResource.class);
	}

	/**
	 * Constructs the URL for the query of AAPL end of day prices between 15 Jan 2015 - 1 Apr 2015.
	 */
	private WebTarget createUrl() {
		final String path = "api/v3/datatables/WIKI/PRICES.json";
		return root.path(path).queryParam("qopts.columns", "date,open,high,low,close")
		        .queryParam("date.gte", "20150115").queryParam("date.lt", "20150401").queryParam("ticker", "AAPL")
		        .queryParam("api_key", getQuandlApiKey());
	}

	/**
	 * Retrieve the user specific API key for accessing Quandl.
	 */
	private String getQuandlApiKey() {

		try {
			List<String> lines = Files.readAllLines(Paths.get("api-key/quandl.key"));

			if (lines.size() != 1) {
				throw new AssertionError("Populate the quandl.key file with your Quandl API key");
			}

			return lines.get(0);
		} catch (final NoSuchFileException e) {
			throw new AssertionError(
			        "Sign up free for Quandl, then create a file for your API at /api-key/quandl.key");

		} catch (final IOException e) {
			throw new AssertionError("Problem reading API key from file", e);
		}
	}

	/**
	 * Verifies the parsed JSON object has the expected structure.
	 */
	private void verifyResource( final QuandlResponseResource inflatedJson ) throws JsonProcessingException {
		final String actualJson = new ObjectMapper().writeValueAsString(inflatedJson);

		if (!StringUtils.equals(EXPECTED_JSON, actualJson)) {
			throw new AssertionError(String.format("%s != %s", EXPECTED_JSON, actualJson));
		}
	}
}