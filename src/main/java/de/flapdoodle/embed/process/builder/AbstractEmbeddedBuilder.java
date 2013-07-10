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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;


public class AbstractEmbeddedBuilder<B> {

	Map<Pair<String, Class<?>>, Object> propertyMap = new HashMap<Pair<String, Class<?>>, Object>();
	boolean _override = false;

	protected void setOverride(boolean override) {
		_override=override;
	}

	protected <T> T set(Class<T> type, T value) {
		return set(null,type,value);
	}

	protected <T> T set(String label, Class<T> type, T value) {
		T old = (T) propertyMap.put(new ImmutablePair(label, type), value);
		if ((!_override) && (old!=null)) {
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " allready set to " + old);
		}
		return old;
	}

	private <T> String labelOrTypeAsString(String label, Class<T> type) {
		return label!=null ? label : ""+type;
	}

	protected <T> T get(Class<T> type) {
		return get((String) null,type);
	}

	protected <T> T get(String label, Class<T> type) {
		T ret = (T) propertyMap.get(new ImmutablePair(label, type));
		if (ret == null)
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " not set");
		return ret;
	}

	protected <T> T getOrDefault(String label, Class<T> type, T defaultValue) {
			T ret = (T) propertyMap.get(new ImmutablePair(label, type));
			if (ret == null)
				ret = defaultValue;
			return ret;
		}

	protected <T> T get(Class<T> type, T defaultValue) {
		T ret = (T) propertyMap.get(type);
		return ret != null
				? ret
				: defaultValue;
	}
}
