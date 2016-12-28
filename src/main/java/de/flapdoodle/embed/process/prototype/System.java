package de.flapdoodle.embed.process.prototype;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface System {
	<T> Supplier<State<T>> transitionInto(NamedType<T> type);
	
	default <T> void withStateOf(NamedType<T> type, Consumer<T> consumer) {
		try (State<T> state = transitionInto(type).get()) {
			consumer.accept(state.current());
		}
	}
}
