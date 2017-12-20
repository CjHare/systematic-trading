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
package com.systematic.trading.data.dao.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

import com.systematic.trading.data.dao.ApiKeyDao;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;

/**
 * Implementation of ApiKeyDao accessing a file from the classpath.
 * 
 * @author CJ Hare
 */
public class FileApiKeyDao implements ApiKeyDao {

	private static final String ERROR_MISSING_KEY_FILE = "Missing api key file, sign up for your desired service and put the key into a file named %s on the classpath";
	private static final String ERROR_EMPTY_KEY_FILE = "Problem reading API key from file at %s";

	private static final ClassLoader CLASSPATH = ApiKeyDao.class.getClassLoader();

	@Override
	public String apiKey( final String keyFileLocation ) throws CannotRetrieveConfigurationException {

		try {

			final URL location = CLASSPATH.getResource(keyFileLocation);

			if (location == null) {
				throw new CannotRetrieveConfigurationException(String.format(ERROR_MISSING_KEY_FILE, keyFileLocation));
			}

			List<String> lines = Files.readAllLines(Paths.get(location.toURI()));

			if (lines.size() != 1) {
				throw new CannotRetrieveConfigurationException(String.format(ERROR_EMPTY_KEY_FILE, keyFileLocation));
			}

			return lines.get(0);
		} catch (final NoSuchFileException | URISyntaxException e) {
			throw new CannotRetrieveConfigurationException(
			        String.format(String.format(ERROR_MISSING_KEY_FILE, keyFileLocation), keyFileLocation));

		} catch (final IOException e) {
			throw new CannotRetrieveConfigurationException(String.format(ERROR_EMPTY_KEY_FILE, keyFileLocation), e);
		}
	}
}