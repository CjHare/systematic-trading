/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.display.file;

import java.io.File;

/**
 * Empties the destination directory of all files and folders.
 * 
 * @author CJ Hare
 */
public class FileClearDestination {

	private final String outputDirectory;

	public FileClearDestination(final String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void clear() {

		// Ensure the directory exists
		final File outputDirectoryFile = new File(outputDirectory);

		if (outputDirectoryFile.exists())

		{
			deleteSubDirectories(outputDirectoryFile);
		} else

		{
			if (!outputDirectoryFile.mkdirs()) {
				throw new IllegalArgumentException(
				        String.format("Failed to create / access directory parent directory: %s", outputDirectory));
			}
		}

		verifyDirectoryIsEmpty(outputDirectory);
	}

	private void verifyDirectoryIsEmpty( final String outputDirectory ) {
		final File outputDirectoryFile = new File(outputDirectory);

		if (outputDirectoryFile.listFiles().length != 0) {
			throw new IllegalArgumentException(String.format("%s was not successfully emptied, still contains: %s",
			        outputDirectory, outputDirectoryFile.listFiles()));
		}
	}

	private void deleteSubDirectories( final File directory ) {

		for (final File file : directory.listFiles()) {

			if (file.isDirectory()) {
				deleteSubDirectories(file);
			}

			if (!file.delete()) {
				throw new IllegalArgumentException(String.format("Failed to delete: %s", directory));
			}
		}
	}

}
