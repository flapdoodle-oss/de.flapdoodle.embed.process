/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 */
public class LogWatchStreamProcessor implements IStreamProcessor {

	private static Logger logger = Logger.getLogger(LogWatchStreamProcessor.class.getName());

	//private final Reader _reader;
	private final StringBuilder output = new StringBuilder();
	private final String success;
	private final Set<String> failures;

	private boolean initWithSuccess = false;
	private String failureFound = null;

	private final IStreamProcessor destination;

	public LogWatchStreamProcessor(String success, Set<String> failures, IStreamProcessor destination) {
		this.success = success;
		this.failures = new HashSet<String>(failures);
		this.destination = destination;
	}

	@Override
	public void process(String block) {
		destination.process(block);

		CharSequence line = block;
		output.append(line);

		if (output.indexOf(success) != -1) {
			gotResult(true,null);
		} else {
			for (String failure : failures) {
				int failureIndex = output.indexOf(failure);
				if (failureIndex != -1) {
					gotResult(false,output.substring(failureIndex));
				}
			}
		}
	}

	@Override
	public void onProcessed() {
		gotResult(false,"<EOF>");
	}

	private synchronized void gotResult(boolean success, String message) {
		this.initWithSuccess=success;
		failureFound=message;
		notify();
	}

	public synchronized void waitForResult(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isInitWithSuccess() {
		return initWithSuccess;
	}
	
	public String getFailureFound() {
		return failureFound;
	}
	
	public String getOutput() {
		return output.toString();
	}


}
