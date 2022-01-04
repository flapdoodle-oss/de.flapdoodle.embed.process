/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.io;

import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class ReaderProcessor extends Thread {

	private static final int CHAR_BUFFER_LENGTH = 512;
	private final Reader reader;
	private final StreamProcessor streamProcessor;

	protected ReaderProcessor(Reader reader, StreamProcessor streamProcessor) {
		this.reader = reader;
		this.streamProcessor = streamProcessor;

		setDaemon(true);
		start();
	}

	// TODO close

	@Override
	public void run() {
		try {
			int read;
			char[] buf = new char[CHAR_BUFFER_LENGTH];
			while ((read = reader.read(buf)) != -1) {
				streamProcessor.process(new String(buf, 0, read));
			}
			//CHECKSTYLE:OFF
		} catch (IOException iox) {
			// _logger.log(Level.SEVERE,"out",iox);
		} finally {
			streamProcessor.onProcessed();
		}
		//CHECKSTYLE:ON

	}

	public void abort() {
		try {
			interrupt();
			join(1000);
		}
		catch (InterruptedException ix) {
			interrupt();
		}
	}

	public static void abortAll(ReaderProcessor ... readerProcessors) {
		if (readerProcessors.length>0) {
			abortIndex(0, readerProcessors);
		}
	}

	private static void abortIndex(int idx, ReaderProcessor[] readerProcessors) {
		if (readerProcessors.length>idx) {
			try {
				abortIndex(idx+1,readerProcessors);
			} finally {
				readerProcessors[idx].abort();
			}
		}
	}
}
