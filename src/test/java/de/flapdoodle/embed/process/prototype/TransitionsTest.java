package de.flapdoodle.embed.process.prototype;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TransitionsTest {

	@Test
	public void sample() {
		System system = SystemBuilder.builderOf("test")
			.transitionInto(String.class)
				.with(() -> State.of("foo"))
			.transitionInto("dep",String.class)
				.with(String.class, s -> s.map(t -> "-> "+t))
			.build();
		
		system.withStateOf(NamedType.of(String.class), s -> {
			assertEquals("foo", s);
		});
		
		system.withStateOf(NamedType.of("dep", String.class), s -> {
			assertEquals("-> foo", s);
		});
	}
}
