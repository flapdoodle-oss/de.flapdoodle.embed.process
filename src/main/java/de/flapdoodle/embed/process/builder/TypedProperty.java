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
package de.flapdoodle.embed.process.builder;

@Deprecated
public final class TypedProperty<T> {

	private final String _name;
	private final Class<T> _type;

	public TypedProperty(String name, Class<T> type) {
		if (name==null) throw new IllegalArgumentException("name is null");
		if (type==null) throw new IllegalArgumentException("type is null");
		_name = name;
		_type = type;
	}

	public String name() {
		return _name;
	}

	public Class<T> type() {
		return _type;
	}
	
	@Override
	public String toString() {
		return _name+"("+_type+")";
	}

	public static <T> TypedProperty<T> with(String name, Class<T> type) {
		return new TypedProperty<T>(name, type);
	}

	public static <T> TypedProperty<T> with(Class<T> type) {
		return new TypedProperty<T>(type.getSimpleName(), type);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _name.hashCode();
		result = prime * result + _type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedProperty other = (TypedProperty) obj;
		return _name.equals(other._name) && _type.equals(other._type);
	}

}
