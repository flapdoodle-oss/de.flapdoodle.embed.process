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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created with IntelliJ IDEA.
 * User: m.joehren
 * Date: 13.07.13
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
public class AbstractEmbeddedBuilderTest {

	private AbstractEmbeddedBuilder<Integer> builder;

	@Before
	public void setUp() throws Exception {
		builder = new AbstractEmbeddedBuilder<Integer>();

	}

	@Test
	public void get_shouldReturnValueForExistingKey() {
		// Given
		final Long expectedValue = 1L;
		builder.set(TypedProperty.with("TEST_PROPERTY1", Long.class), expectedValue);
		builder.set(TypedProperty.with("TEST_PROPERTY2", Long.class), 2L);
		builder.set(TypedProperty.with("TEST_PROPERTY3", Long.class), 3L);
		// When
		final Long actualValue = builder.get(TypedProperty.with("TEST_PROPERTY1", Long.class));
		// Then
		assertEquals(expectedValue, actualValue);
	}

	@Test
	public void get_shouldReturnValueForOverriddenKey() {
		// Given
		final Long expectedValue = 1L;
		builder.setDefault(TypedProperty.with("TEST_PROPERTY1", Long.class), 2L);
		builder.set(TypedProperty.with("TEST_PROPERTY1", Long.class), expectedValue);

		// When
		final Long actualValue = builder.get(TypedProperty.with("TEST_PROPERTY1", Long.class));
		// Then
		assertEquals(expectedValue, actualValue);
	}

	@Test(expected = RuntimeException.class)
	public void get_shouldThrowExceptionForOverriddenKeyWithoutOverrideOption() {
		// Given
		final Long expectedValue = 1L;
		builder.set(TypedProperty.with("FU",Long.class), expectedValue);
		builder.set(TypedProperty.with("FU",Long.class), expectedValue);
		fail("RuntimeException should have been thrown");
	}

	@Test
	public void get_shouldReturnDefaultValueForNotExistingKeyWhenDefaultIsDefined() {
		// Given
		final Long expectedValue = 1L;
		builder.setDefault(TypedProperty.with("TEST_PROPERTY", Long.class), expectedValue);
		// When
		final Long actualValue = builder.get(TypedProperty.with("TEST_PROPERTY", Long.class));
		// Then
		assertEquals(expectedValue, actualValue);
	}


	@Test(expected = RuntimeException.class)
	public void get_shouldThrowExceptionForNotExistingKey() {
		// When
		final Long actualValue = builder.get(TypedProperty.with("TEST_PROPERTY", Long.class));
		fail("RuntimeException should have been thrown");
	}


	@Test
	public void getOrDefault_shouldReturnDefaultValueForNotExistingKey() {
		// Given
		final Long expectedValue = 1L;
		// When
		final Long actualValue = builder.get(TypedProperty.with("TEST_PROPERTY", Long.class), expectedValue);
		// Then
		assertEquals(expectedValue, actualValue);
	}
}
