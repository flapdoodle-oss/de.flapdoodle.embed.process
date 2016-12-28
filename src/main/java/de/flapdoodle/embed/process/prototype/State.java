package de.flapdoodle.embed.process.prototype;

import java.util.List;
import java.util.function.Function;

import org.immutables.builder.Builder.Parameter;
import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;

@Value.Immutable
public interface State<T> extends AutoCloseable {
	@Parameter
	T current();
	
	List<TearDown<T>> onTearDown();
	
	@Override
	default void close() throws RuntimeException {
		onTearDown().forEach(t -> t.onTearDown(current()));
	}
	
	@Auxiliary
	default <D> State<D> map(Function<T, D> map, TearDown<D> ... tearDowns) {
		return builder(map.apply(current())).addOnTearDown(tearDowns).build();
	}
	
	public static <T> ImmutableState.Builder<T> builder(T current) {
		return ImmutableState.builder(current);
	}
	
	public static <T> State<T> of(T current) {
		return builder(current).build();
	}
}
