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
package de.flapdoodle.embed.process.io.progress;

import org.slf4j.Logger;

public class Slf4jProgressListener implements IProgressListener {

    private final Logger logger;
    private int lastPercent = -1;


    public Slf4jProgressListener(Logger logger) {
        this.logger = logger;
    }


    @Override
    public void progress(String label, int percent) {
        if (percent != lastPercent && percent % 10 == 0) {
            logger.info("{} : {} %", label, percent);
        }
        lastPercent = percent;
    }

    @Override
    public void done(String label) {
        logger.info("{} : finished", label);
    }

    @Override
    public void start(String label) {
        logger.info("{} : starting...", label);
    }

    @Override
    public void info(String label, String message) {
        logger.info("{} : {}", label, message);
    }
}
