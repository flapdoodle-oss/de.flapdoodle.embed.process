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
package de.flapdoodle.embed.mongo.config;

import de.flapdoodle.embed.process.config.ISupportConfig;


public class SupportConfig implements ISupportConfig {

	static SupportConfig _instance=new SupportConfig();
	
	@Override
	public String getName() {
		return "mongod";
	}

	@Override
	public String getSupportUrl() {
		return "https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de/issues\n";
	}
	
	public static ISupportConfig getInstance() {
		return _instance;
	}

}
