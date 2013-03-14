package de.flapdoodle.embed.process.builder;

public abstract class ImmutableContainer<T> {

	private final T _value;

	public ImmutableContainer(T value) {
		_value = value;
	}

	public T value() {
		return _value;
	}
}
