package de.flapdoodle.embed.process.prototype;

public interface Transitions {

	interface StartingTransition<D> {
		State<D> transitInto();
	}
	
	interface DependingTransition<S,D> {
		State<D> transitFrom(State<S> source);
	}
	
	interface JoiningTransition<A,B,D> {
		State<D> transitFrom(State<A> firstState, State<B> secondState);
	}
}
