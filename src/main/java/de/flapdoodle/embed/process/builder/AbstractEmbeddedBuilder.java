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
package de.flapdoodle.embed.process.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AbstractEmbeddedBuilder<B> {

	Map<TypedProperty<?>, Object> propertyMap = new HashMap<TypedProperty<?>, Object>();
	Set<TypedProperty<?>> propertyHadDefaultValueMap = new HashSet<TypedProperty<?>>();

	protected <T> T setDefault(TypedProperty<T> property, T value) {
		T old = set(property, value);
		if (!propertyHadDefaultValueMap.add(property)) {
			throw new RuntimeException("" + property + " is allready set with default value");
		}
		return old;
	}

	protected <T> T set(TypedProperty<T> property, T value) {
		T old = (T) propertyMap.put(property, value);
		boolean onlyDefaultValueWasSet = propertyHadDefaultValueMap.remove(property);

		if ((old != null) && (!onlyDefaultValueWasSet)) {
			throw new RuntimeException("" + property + " allready set to " + old);
		}
		return old;
	}

	protected <T> T get(TypedProperty<T> property) {
		T ret = (T) propertyMap.get(property);
		if (ret == null)
			throw new RuntimeException("" + property + " not set");
		return ret;
	}

	protected <T> T get(TypedProperty<T> property, T defaultValue) {
		T ret = (T) propertyMap.get(property);
		return ret != null
				? ret
				: defaultValue;
	}

}
