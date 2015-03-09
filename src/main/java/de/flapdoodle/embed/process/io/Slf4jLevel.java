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

import org.slf4j.Logger;

public enum Slf4jLevel {

    TRACE {
        public void log(Logger logger, String message, Object... arguments) {
            logger.trace(message, arguments);
        }
    },
    DEBUG {
        public void log(Logger logger, String message, Object... arguments) {
            logger.debug(message, arguments);
        }
    },
    INFO {
        public void log(Logger logger, String message, Object... arguments) {
            logger.info(message, arguments);
        }
    },
    WARN {
        public void log(Logger logger, String message, Object... arguments) {
            logger.warn(message, arguments);
        }
    },
    ERROR {
        public void log(Logger logger, String message, Object... arguments) {
            logger.error(message, arguments);
        }
    };

    public abstract void log(Logger logger, String message, Object... arguments);
}
