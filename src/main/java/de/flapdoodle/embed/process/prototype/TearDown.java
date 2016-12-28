package de.flapdoodle.embed.process.prototype;

@FunctionalInterface
public interface TearDown<T> {
	void onTearDown(T current);
}
