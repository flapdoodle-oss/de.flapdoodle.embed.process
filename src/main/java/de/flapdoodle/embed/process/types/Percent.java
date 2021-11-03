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
package de.flapdoodle.embed.process.types;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import de.flapdoodle.checks.Preconditions;

@Immutable
public abstract class Percent {

	private static final int MIN = 0;
	private static final int MAX = 100;

	@Parameter
	public abstract int value();
	
	@Auxiliary
	public boolean isMax() {
		return value()==MAX;
	}
	
	@Auxiliary
	public boolean isMin() {
		return value()==MIN;
	}
	
	@Check
	protected void check() {
		Preconditions.checkArgument(value()>=MIN, "%s < 0",value());
		Preconditions.checkArgument(value()<=MAX, "%s > 100",value());
	}
	
	public static Percent of(int value) {
		return ImmutablePercent.of(value);
	}

	public static Percent max() {
		return of(MAX);
	}
	
	public static Percent min() {
		return of(MIN);
	}
}
