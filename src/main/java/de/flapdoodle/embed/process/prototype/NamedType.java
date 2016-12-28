package de.flapdoodle.embed.process.prototype;

import org.immutables.value.Value;
import org.immutables.value.Value.Parameter;

@Value.Immutable
public interface NamedType<T> {
	@Parameter
	String name();
	@Parameter
	Class<T> type();
	
	public static <T> NamedType<T> of(String name, Class<T> type) {
		return ImmutableNamedType.of(name, type);
	}
	
	public static <T> NamedType<T> of(Class<T> type) {
		return of("",type);
	}
}
