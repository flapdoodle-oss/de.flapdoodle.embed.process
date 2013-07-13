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
		builder.set("TEST_PROPERTY1", Long.class, expectedValue);
		builder.set("TEST_PROPERTY2", Long.class, 2L);
		builder.set("TEST_PROPERTY3", Long.class, 3L);
		// When
		final Long actualValue = builder.get("TEST_PROPERTY1", Long.class);
		// Then
		assertEquals(expectedValue, actualValue);
	}

	@Test
	public void get_shouldReturnValueForOverriddenKey() {
		// Given
		final Long expectedValue = 1L;
		builder.setOverride(true);
		builder.set("TEST_PROPERTY1", Long.class, 2L);
		builder.set("TEST_PROPERTY1", Long.class, expectedValue);

		// When
		final Long actualValue = builder.get("TEST_PROPERTY1", Long.class);
		// Then
		assertEquals(expectedValue, actualValue);
	}

	@Test(expected = RuntimeException.class)
	public void get_shouldThrowExceptionForOverriddenKeyWithoutOverrideOption() {
		// Given
		final Long expectedValue = 1L;
		builder.set(Long.class, expectedValue);
		builder.set(Long.class, expectedValue);
		fail("RuntimeException should have been thrown");
	}

	@Test
	public void get_shouldReturnDefaultValueForNotExistingKeyWhenDefaultIsDefined() {
		// Given
		final Long expectedValue = 1L;
		builder.setDefault("TEST_PROPERTY", Long.class, expectedValue);
		// When
		final Long actualValue = builder.get("TEST_PROPERTY", Long.class);
		// Then
		assertEquals(expectedValue, actualValue);
	}


	@Test(expected = RuntimeException.class)
	public void get_shouldThrowExceptionForNotExistingKey() {
		// When
		final Long actualValue = builder.get("TEST_PROPERTY", Long.class);
		fail("RuntimeException should have been thrown");
	}

	@Test
	public void get_shouldReturnValueWithoutLabel() {
		// Given
		final Long expectedValue = 1L;
		builder.set(Long.class, expectedValue);
		// When
		final Long actualValue = builder.get(Long.class, expectedValue);
		// Then
		assertEquals(expectedValue, actualValue);
	}

	@Test
	public void get_shouldThrowExceptionForValueWithoutLabel() {
		// Given
		final Long expectedValue = 1L;
		builder.set(Long.class, expectedValue);
		builder.set("dummy", Long.class, 2L);
		// When
		final Long actualValue = builder.get(Long.class, expectedValue);
		// Then
		assertEquals(expectedValue, actualValue);
	}


	@Test
	public void getOrDefault_shouldReturnDefaultValueForNotExistingKey() {
		// Given
		final Long expectedValue = 1L;
		// When
		final Long actualValue = builder.getOrDefault("TEST_PROPERTY", Long.class, expectedValue);
		// Then
		assertEquals(expectedValue, actualValue);
	}
}
