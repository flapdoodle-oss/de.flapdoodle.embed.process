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

	Map<ImmutablePair<String, Class<?>>, Object> propertyMap = new HashMap<ImmutablePair<String, Class<?>>, Object>();
	Set<ImmutablePair<String, Class<?>>> propertyHadDefaultValueMap = new HashSet<ImmutablePair<String, Class<?>>>();

	boolean _override = false;

	protected void setOverride(boolean override) {
		_override = override;
	}

	protected <T> T set(Class<T> type, T value) {
		return set(null, type, value);
	}

	protected <T> T setDefault(String label, Class<T> type, T value) {
		T old = set(label, type, value);
		propertyHadDefaultValueMap.add(new ImmutablePair(label, type));
		return old;
	}

	protected <T> void markDefaultValue(String label, Class<T> type) {
		if (!propertyHadDefaultValueMap.add(new ImmutablePair(label, type))) {
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " allready marked as default value");
		}
	}

	protected <T> T set(String label, Class<T> type, T value) {
		T old = (T) propertyMap.put(new ImmutablePair(label, type), value);
		boolean onlyDefaultValueWasSet = propertyHadDefaultValueMap.remove(new ImmutablePair(label, type));

		if ((!_override) && (old != null) && (!onlyDefaultValueWasSet)) {
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " allready set to " + old);
		}
		return old;
	}

	private <T> String labelOrTypeAsString(String label, Class<T> type) {
		return label != null ? label : "" + type;
	}

	protected <T> T get(Class<T> type) {
		return get((String) null, type);
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
		T ret = (T) propertyMap.get(new ImmutablePair(type.getName(), type));
		return ret != null
				? ret
				: defaultValue;
	}

	private static class ImmutablePair<L, R> {
		private final L left;
		private final R right;

		public ImmutablePair(L lhs, R rhs) {
			left = lhs;
			right = rhs;
		}

		private L getLeft() {
			return left;
		}

		private R getRight() {
			return right;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ImmutablePair)) return false;

			ImmutablePair that = (ImmutablePair) o;

			if (left != null ? !left.equals(that.left) : that.left != null) return false;
			if (right != null ? !right.equals(that.right) : that.right != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = left != null ? left.hashCode() : 0;
			result = 31 * result + (right != null ? right.hashCode() : 0);
			return result;
		}
	}

}
