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
package de.flapdoodle.embed.process.io.progress;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingProgressListener implements IProgressListener {

	private final Logger _logger;
	private final Level _level;

	public LoggingProgressListener(Logger logger, Level level) {
		_logger = logger;
		_level=level;
	}

	@Override
	public void start(String label) {
		_logger.log(_level,label + " starting...");
	}

	@Override
	public void progress(String label, int percent) {
		_logger.log(_level,label + ": " + percent + "% achieved.");
	}

	@Override
	public void info(String label, String message) {
		_logger.log(_level,label + ": " + message);
	}

	@Override
	public void done(String label) {
		_logger.log(_level,label + " achieved successfully.");
	}
}
