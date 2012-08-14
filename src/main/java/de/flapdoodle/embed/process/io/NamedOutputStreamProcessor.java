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

/**
 *
 */
public class NamedOutputStreamProcessor implements IStreamProcessor {


	private final IStreamProcessor destination;
	private final String name;
	boolean firstBlock=true;

	public NamedOutputStreamProcessor(String name, IStreamProcessor destination) {
		this.name = name;
		this.destination = destination;
	}

	@Override
	public void process(String block) {
		String replaced = block.replace("\n", "\n" + name + " ");
		if (firstBlock) {
			replaced=name+replaced;
			firstBlock=false;
		}
		destination.process(replaced);
	}

	@Override
	public void onProcessed() {
		destination.onProcessed();

	}


}
